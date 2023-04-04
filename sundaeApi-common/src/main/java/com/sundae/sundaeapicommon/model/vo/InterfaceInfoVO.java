package com.sundae.sundaeapicommon.model.vo;

import com.sundae.sundaeapicommon.model.entity.InterfaceInfo;
import lombok.Data;

@Data
public class InterfaceInfoVO extends InterfaceInfo {

    /**
     * 调用次数
     */
    private Integer totalNum;

    private static final long serialVersionUID = 1L;
}
