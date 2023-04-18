package com.sundae.project.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sundae.project.common.*;
import com.sundae.project.constant.UserConstant;
import com.sundae.project.exception.BusinessException;
import com.sundae.project.mapper.UserMapper;
import com.sundae.project.model.dto.user.UserRegisterRequest;
import com.sundae.project.service.UserService;
import com.sundae.sundaeapicommon.AuthPhoneNumber;
import com.sundae.sundaeapicommon.common.SmsTo;
import com.sundae.sundaeapicommon.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * 用户服务实现类
 *
 * @author sundae
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private SmsLimiter smsLimiter;

    @Resource
    private RabbitUtils rabbitUtils;

    @Resource
    private MobileSignature mobileSignature;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "sundae";

    private static final String CAPTCHA_PREFIX = "api:captchaId:";

    private static final String UPDATE_PWD_ACCOUNT = "update_pwd_account";

    private static final String MOBILE_SIGNATURE = "user:mobile:signature";

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String captcha = userRegisterRequest.getCaptcha();
        String mobile = userRegisterRequest.getMobile();
        String code = userRegisterRequest.getCode();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        AuthPhoneNumber authPhoneNumber = new AuthPhoneNumber();
        if (!authPhoneNumber.isPhoneNum(mobile)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        // 手机号和验证码是否匹配
        boolean verify = smsLimiter.verifyCode(mobile, code);
        if (!verify) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "短信验证码错误");
        }
        // 图形验证码是否正确
//        String signature = request.getHeader("signature");
        String signature = (String) redisTemplate.opsForValue().get("signature");
        log.info("captchaId:" + signature);
        if (null == signature){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String picCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_PREFIX + signature);
        if (null == picCaptcha || !captcha.equals(picCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"图形验证码错误或已经过期，请重新刷新验证码");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 判断手机号是否已注册
            String phoneExist = userMapper.selectPhone(mobile);
            if ( phoneExist != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已被注册");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 分配accessKey, secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setMobile(mobile);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return user;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 用户申请更换ak/sk
     * @param loginUser
     * @return
     */
    @Override
    public boolean replaceSign(User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录");
        }
        Long id = loginUser.getId();
        String userAccount = loginUser.getUserAccount();
        // 生成新的ak/sk
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
        User user = new User();
        user.setId(id);
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);
        return this.updateById(user);
    }

    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        // 随机生成 4 位验证码
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        // 定义图片的显示大小
        LineCaptcha lineCaptcha = null;
        lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        // 在前端发送请求时携带captchaId，用于标识不同的用户。
        String signature = request.getHeader("signature");
        redisTemplate.opsForValue().set("signature", signature, 2, TimeUnit.MINUTES);
        if (null == signature){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        try {
            // 调用父类的 setGenerator() 方法，设置验证码的类型
            lineCaptcha.setGenerator(randomGenerator);
            // 输出到页面
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature ,lineCaptcha.getCode());
            // 关闭流
            response.getOutputStream().close();
            //将对应的验证码存入redis中去，2分钟后过期
            redisTemplate.opsForValue().set(CAPTCHA_PREFIX + signature,lineCaptcha.getCode(),2, TimeUnit.MINUTES);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long verifyUserAccount(String username) {
        if (username == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = this.getOne(new QueryWrapper<User>().eq("userAccount", username));
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        synchronized (username.intern()) {
            try {
                redisTemplate.opsForValue().set(UPDATE_PWD_ACCOUNT, username, 1, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("redis ----- 设置UPDATE_PWD_ACCOUNT key失败");
            }
        }
        return user.getId();
    }

    @Override
    public BaseResponse updateUserPWd(String password, String checkPassword) {
        if (StringUtils.isAnyBlank(password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String username = (String) redisTemplate.opsForValue().get(UPDATE_PWD_ACCOUNT);
        //加密
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        boolean res = this.update(new UpdateWrapper<User>().eq("userAccount", username).set("userPassword", encryptPassword));
        if (!res) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success("修改密码成功");
    }

    @Override
    public BaseResponse mobileCaptcha(String mobile) {
        if (mobile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AuthPhoneNumber authPhoneNumber = new AuthPhoneNumber();
        //验证手机号的合法性
        if(!authPhoneNumber.isPhoneNum(mobile)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "手机号格式错误");
        }
        int code = (int)((Math.random() * 9 + 1) * 10000);
        //尝试获取令牌
        boolean sendSmsAuth = smsLimiter.sendSmsAuth(mobile, String.valueOf(code));
        if (!sendSmsAuth) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求频率过高,请稍后重试");
        }
        SmsTo smsTo = new SmsTo(mobile, String.valueOf(code));
        try {
            // 实际调用第三方服务实现短信发送
            rabbitUtils.sendSms(smsTo);
        } catch (Exception e) {
            //发送失败，删除令牌桶
            redisTemplate.delete("sms:"+mobile+"_last_refill_time");
            redisTemplate.delete("sms:"+mobile+"_tokens");
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"发送验证码失败，请稍后再试");
        }
        log.info("发送验证码成功 -----> 手机号{}, 验证码{}", mobile, code);
        return ResultUtils.success("发送成功");
    }
}




