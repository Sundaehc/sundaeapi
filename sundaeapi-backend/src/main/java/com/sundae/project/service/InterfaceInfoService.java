package com.sundae.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sundae.sundaeapicommon.model.entity.InterfaceInfo;

/**
* @author Uzi
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2022-10-30 16:22:08
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add 是否为创建校验
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
