package com.sundae.sundaeinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.sundae.sundaeclientsdk.model.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/dygirl")
    public String getdyGirl(){
        Map<String,String> map= new HashMap<>();
        map.put("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.44");
        HttpResponse httpResponse = HttpRequest.get("https://zj.v.api.aa1.cn/api/video_dyv2")
                .addHeaders(map)
                .execute();
        String location = httpResponse.header("Location");
        HttpResponse httpResponse1 = HttpRequest.get(location).execute();
        return httpResponse1.body();
    }

    @GetMapping("/comfort/words")
    public String getComfortWords() {
        Map<String,String> map= new HashMap<>();
        map.put("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.44");
        HttpResponse httpResponse = HttpRequest.get("https://v.api.aa1.cn/api/api-wenan-anwei/index.php?type=json")
                .addHeaders(map)
                .execute();
        return httpResponse.body();
    }

    @GetMapping("/hot/comment")
    public String getHotComment() {
        Map<String,String> map= new HashMap<>();
        map.put("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.44");
        HttpResponse httpResponse = HttpRequest.get("https://v.api.aa1.cn/api/api-wenan-wangyiyunreping/index.php?aa1=json")
                .addHeaders(map)
                .execute();
        return httpResponse.body();
    }

}
