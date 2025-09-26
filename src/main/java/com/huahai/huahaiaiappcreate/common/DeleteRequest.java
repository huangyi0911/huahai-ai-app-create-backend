package com.huahai.huahaiaiappcreate.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求包装类
 *
 * @author huahai
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
