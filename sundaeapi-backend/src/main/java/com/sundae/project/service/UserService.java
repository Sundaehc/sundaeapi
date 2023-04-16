package com.sundae.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sundae.project.common.BaseResponse;
import com.sundae.project.model.dto.user.UserRegisterRequest;
import com.sundae.sundaeapicommon.model.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户服务
 *
 * @author sundae
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterRequest
     * @param request
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 用户申请更换签名
     * @param loginUser
     * @return
     */
    boolean replaceSign(User loginUser);

    /**
     * 生成图形验证码
     * @param request
     * @param response
     */
    void getCaptcha(HttpServletRequest request, HttpServletResponse response);

    /**
     * 验证用户输出账号（忘记密码）
     * @param username
     * @return
     */
    Long verifyUserAccount(String username);

    /**
     * 用户修改密码
     * @param password
     * @param checkPassword
     * @return
     */
    BaseResponse updateUserPWd(String password, String checkPassword);

    /**
     * 发送短信验证码
     * @param mobile
     * @return
     */
    BaseResponse mobileCaptcha(String mobile);
}
