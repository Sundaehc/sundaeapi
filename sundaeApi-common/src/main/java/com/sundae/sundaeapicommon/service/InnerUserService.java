package com.sundae.sundaeapicommon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sundae.sundaeapicommon.model.entity.User;

public interface InnerUserService {

    /**
     * 从数据库中查询是否已分配给用户密钥
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);
}
