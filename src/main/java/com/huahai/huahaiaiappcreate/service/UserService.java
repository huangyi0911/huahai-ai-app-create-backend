package com.huahai.huahaiaiappcreate.service;

import com.huahai.huahaiaiappcreate.model.dto.user.UserAddRequest;
import com.huahai.huahaiaiappcreate.model.dto.user.UserQueryRequest;
import com.huahai.huahaiaiappcreate.model.vo.user.LoginUserVO;
import com.huahai.huahaiaiappcreate.model.vo.user.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.huahai.huahaiaiappcreate.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/huangyi0911">花海</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 密码校验
     * @return 新用户 id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 登录用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 管理员添加用户
     * @param userAddRequest 添加用户请求
     * @return 添加的用户 ID
     */
    Long addUser(UserAddRequest userAddRequest);

    /**
     * 获取脱敏后的用户信息
     * @param user  用户信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户列表
     * @param userList 用户列表
     * @return 脱敏后的用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取封装后的查询条件
     * @param userQueryRequest 用户查询条件
     * @return 数据库查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * loginUser 脱敏
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取加密密码
     *
     * @param userPassword 用户密码
     * @return 加密密码
     */
    String getEncryptPassword(String userPassword);


}
