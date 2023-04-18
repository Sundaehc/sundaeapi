package com.sundae.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sundae.sundaeapicommon.model.entity.User;
import com.sundae.sundaeapicommon.model.entity.UserInterfaceInfo;

/**
* @author Uzi
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2022-11-26 15:24:29
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 统计调用接口次数
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

}
