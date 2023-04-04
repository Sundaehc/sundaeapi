package com.sundae.sundaeinterface.controller;

import com.sundae.sundaeclientsdk.model.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getNameByGet(String name) {
        return "GET 你的名字是:" + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是:" + name;
    }

    @PostMapping("/user")
    public String getUsernameByPost(@RequestBody User user, HttpServletRequest request) throws UnsupportedEncodingException {
//        String accessKey = request.getHeader("accessKey");
//        String nonce = request.getHeader("nonce");
//        String timeStamp = request.getHeader("timeStamp");
//        String body = request.getHeader("body");
//        String sign = request.getHeader("sign");
//        if (!accessKey.equals("sundae")) {
//            throw new RuntimeException("无权限");
//        }
//        if (Long.parseLong(nonce) > 10000L) {
//            throw new RuntimeException("无权限");
//        }
//        long currentTime = System.currentTimeMillis() / 1000;
//        long FIVE_MINUTES = 60 * 5L;
//        if ( (currentTime - Long.parseLong(timeStamp)) > FIVE_MINUTES) {
//            throw new RuntimeException("时间戳无效");
//        }
//        String serverSign = SignUtils.getSign(body, "abcdefgh");
//        if (!sign.equals(serverSign)) {
//            throw new RuntimeException("无权限");
//        }
        String result = "POST 用户名是:" + user.getUsername();
        return result;
    }
}
