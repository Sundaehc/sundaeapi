package com.sundae.sundaeapicommon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sundae.sundaeapicommon.model.entity.InterfaceInfo;

public interface InnerInterfaceInfoService {

    /**
     * 从数据库中查询接口是否存在
     * @param url
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfo(String url, String method);
}
