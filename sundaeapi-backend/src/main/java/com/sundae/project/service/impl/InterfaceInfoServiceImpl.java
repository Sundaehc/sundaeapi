package com.sundae.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sundae.project.common.ErrorCode;
import com.sundae.project.exception.BusinessException;
import com.sundae.project.model.vo.InterfaceInfoVo;
import com.sundae.project.service.InterfaceInfoService;
import com.sundae.project.mapper.InterfaceInfoMapper;
import com.sundae.project.service.UserInterfaceInfoService;
import com.sundae.sundaeapicommon.model.entity.InterfaceInfo;
import com.sundae.sundaeapicommon.model.entity.User;
import com.sundae.sundaeapicommon.model.entity.UserInterfaceInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author Uzi
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2022-10-30 16:22:08
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService{


    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

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

    @Override
    public InterfaceInfoVo getInterfaceInfoById(long id, User loginUser) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = loginUser.getId();
        InterfaceInfo interfaceInfo = this.getById(id);
        InterfaceInfoVo interfaceInfoVo = new InterfaceInfoVo();
        BeanUtils.copyProperties(interfaceInfo, interfaceInfoVo);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(new QueryWrapper<UserInterfaceInfo>().eq("userId", userId).eq("interfaceInfoId", id));
        if (userInterfaceInfo == null) {
            UserInterfaceInfo newUserInterfaceInfo = new UserInterfaceInfo();
            newUserInterfaceInfo.setUserId(userId);
            newUserInterfaceInfo.setInterfaceInfoId(id);
            userInterfaceInfoService.save(newUserInterfaceInfo);
            interfaceInfoVo.setLeftNum(0);
        } else {
            interfaceInfoVo.setLeftNum(userInterfaceInfo.getLeftNum());
        }
        return interfaceInfoVo;
    }
}




