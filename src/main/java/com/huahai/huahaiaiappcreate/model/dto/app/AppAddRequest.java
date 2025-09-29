package com.huahai.huahaiaiappcreate.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建应用请求
 *
 * @author huahai
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}
