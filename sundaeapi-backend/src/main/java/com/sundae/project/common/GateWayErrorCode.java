package com.sundae.project.common;

/**
 * @author sundae
 */
public enum GateWayErrorCode {

    /**
     * 接口调用次数用尽网关返回状态码
     */
    FORBIDDEN("Error request, response status: 403");

    /**
     * 状态码
     */
    private final String code;

    GateWayErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
