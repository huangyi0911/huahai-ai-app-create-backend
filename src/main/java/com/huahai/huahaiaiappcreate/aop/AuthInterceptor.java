package com.huahai.huahaiaiappcreate.aop;

import com.huahai.huahaiaiappcreate.annotation.AuthCheck;
import com.huahai.huahaiaiappcreate.constants.UserConstant;
import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.entity.User;
import com.huahai.huahaiaiappcreate.model.enums.UserRoleEnum;
import com.huahai.huahaiaiappcreate.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 权限校验拦截器切面
 *
 * @author huahai
 */
@Aspect
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;

    /**
     * 权限校验切面（在使用该注解时生效）
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     * @throws Throwable 抛出异常
     * @return 切面结果
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable{
        // 获取当前需要的角色
        String mustRole = authCheck.mustRole();
        // 获取当前发送的请求信息
        RequestAttributes currentRequestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) currentRequestAttributes).getRequest();
        // 获取当前登录用户
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User loginUser = (User) userObj;
        // 判断当前请求是否需要权限，不需要直接放行
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if(mustRoleEnum == null){
            joinPoint.proceed();
        }
        // 需要权限则判断当前登录是否具有该权限
        UserRoleEnum loginUserEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if(loginUserEnum == null){
            // 没有角色，直接拒绝
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(loginUserEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 权限校验成功，放行
        return joinPoint.proceed();
    }

}
