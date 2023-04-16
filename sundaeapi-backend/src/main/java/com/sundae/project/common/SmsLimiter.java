package com.sundae.project.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SmsLimiter {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RedisTokenBucket redisTokenBucket;

    private final String SMS_PREFIX = "sms:";
    private final String CODE_PREFIX = "code:";
    private final long CODE_EXPIRE_TIME = 300;

    /**
     * 尝试获取一个令牌，如果成功了，那么返回true ，失败返回false，表示限流
     * @param phoneNumber
     * @param code
     * @return
     */
    public boolean sendSmsAuth(String phoneNumber, String code) {
        if (redisTokenBucket.tryAcquire(SMS_PREFIX + phoneNumber)) {
            // 获取到令牌后向redis写入数据
            String key = CODE_PREFIX + phoneNumber;
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_TIME, TimeUnit.SECONDS);
            return true;
        } else {
            log.error("send sms to" + phoneNumber + "reject due to rate limiting");
            return false;
        }
    }

    /**
     * 验证手机号对应的验证码是否正确
     */
    public boolean verifyCode(String phoneNumber, String code) {
        String key = CODE_PREFIX + phoneNumber;
        String redisCode = redisTemplate.opsForValue().get(key);
        // 验证码一致删除redis中对应的key
        if (!StringUtils.isEmpty(redisCode) && redisCode.equals(code)) {
            redisTemplate.delete(redisCode);
            return true;
        }
        return false;
    }
}
