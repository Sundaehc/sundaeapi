package com.sundae.project.common;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class MobileSignature {

    public static final byte[] key = "SundaeKey-cz".getBytes();

    /**
     * 生成手机号签名
     * @param username
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String generateMobileSign(String username) throws NoSuchAlgorithmException {
        String data = username + ":" + "SundaeSign";
        MessageDigest messageDigest;
        messageDigest = MessageDigest.getInstance("MD5");
        return new String(Hex.encodeHex(messageDigest.digest(data.getBytes())));
    }

    /**
     * 对用户名和手机号进行加密
     * @param username
     * @param mobile
     * @return
     */
    public String generateEncryptHex(String username, String mobile) {
        String data = username + ":" + mobile;
        // 构建
        AES aes = SecureUtil.aes(key);
        // 加密
        return aes.encryptHex(data);
    }

    /**
     * 对手机号进行解密
     * @param encryptHex
     * @return
     */
    public String[] decodeHex(String encryptHex){
        // 构建
        AES aes = SecureUtil.aes(key);
        // 解密为字符串
        String decryptStr = aes.decryptStr(encryptHex, CharsetUtil.CHARSET_UTF_8);
        return decryptStr.split(":");
    }
}
