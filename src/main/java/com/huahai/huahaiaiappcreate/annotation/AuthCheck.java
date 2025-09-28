package com.huahai.huahaiaiappcreate.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 *
 * @author huahai
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须拥有某个角色
     *
     * @return 角色权限
     */
    String mustRole() default "";
}
