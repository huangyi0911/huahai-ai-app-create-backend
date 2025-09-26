package com.huahai.huahaiaiappcreate.controller;

import com.huahai.huahaiaiappcreate.common.BaseResponse;
import com.huahai.huahaiaiappcreate.untils.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口
 *
 * @author huahai
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public BaseResponse<String> healthCheck(){
        return ResultUtils.success("ok");
    }
}
