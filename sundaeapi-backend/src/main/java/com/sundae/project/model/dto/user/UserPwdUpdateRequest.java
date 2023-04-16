package com.sundae.project.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新密码请求
 *
 * @author sundae
 */
@Data
public class UserPwdUpdateRequest implements Serializable {

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String checkPassword;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}