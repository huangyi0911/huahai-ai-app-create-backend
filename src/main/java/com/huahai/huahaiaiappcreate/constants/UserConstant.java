package com.huahai.huahaiaiappcreate.constants;

import cn.hutool.core.util.RandomUtil;

/**
 * 用户相关常量
 *
 * @author huahai
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    /**
     * 默认用户名
     */
    String USER_DEFAULT_NAME = "用户 " + RandomUtil.randomNumbers(6);

    /**
     * 默认用户简介
     */
    String USER_DEFAULT_PROFILE = "这个人很懒，什么都没有写...";

    /**
     * 默认用户密码
     */
    String DEFAULT_USER_PASSWORD = "12345678";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    // endregion
}
