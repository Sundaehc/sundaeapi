package com.sundae.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sundae.sundaeapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author Uzi
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2022-11-26 15:24:29
* @Entity com.sundae.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




