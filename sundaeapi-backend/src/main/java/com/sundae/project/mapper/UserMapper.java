package com.sundae.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sundae.sundaeapicommon.model.entity.User;

/**
 * @Entity com.sundae.project.model.domain.User
 */
public interface UserMapper extends BaseMapper<User> {

    String selectPhone(String mobile);
}




