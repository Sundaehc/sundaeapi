package com.sundae.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sundae.project.common.ErrorCode;
import com.sundae.project.exception.BusinessException;
import com.sundae.project.service.InterfaceInfoService;
import com.sundae.project.mapper.InterfaceInfoMapper;
import com.sundae.sundaeapicommon.model.entity.InterfaceInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author Uzi
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2022-10-30 16:22:08
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService{

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        Long userId = interfaceInfo.getUserId();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name, url, method)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名称过长");
        }
    }
}




