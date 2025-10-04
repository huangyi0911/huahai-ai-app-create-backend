package com.huahai.huahaiaiappcreate.service;

import com.huahai.huahaiaiappcreate.common.DeleteRequest;
import com.huahai.huahaiaiappcreate.model.dto.app.AppAddRequest;
import com.huahai.huahaiaiappcreate.model.dto.app.AppAdminUpdateRequest;
import com.huahai.huahaiaiappcreate.model.dto.app.AppQueryRequest;
import com.huahai.huahaiaiappcreate.model.dto.app.AppUpdateRequest;
import com.huahai.huahaiaiappcreate.model.entity.User;
import com.huahai.huahaiaiappcreate.model.vo.app.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.huahai.huahaiaiappcreate.model.entity.App;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/huangyi0911">花海</a>
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用参数
     * @param request       请求
     * @return 创建成功的应用 ID
     */
    Long addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 用户更新应用信息
     *
     * @param appUpdateRequest 更新参数
     * @param request          请求
     */
    Boolean updateApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    /**
     * 删除应用
     *
     * @param deleteRequest 删除参数
     * @param request       请求
     * @return 是否删除成功
     */
    Boolean deleteApp(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 分页获取当前用户的应用
     *
     * @param appQueryRequest 分页查询参数
     * @param request         请求
     * @return 分页结果
     */
    Page<AppVO> listMyAppVOByPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页获取精选应用
     *
     * @param appQueryRequest 分页查询参数
     * @return 分页结果
     */
    Page<AppVO> listGoodAppVOByPage(AppQueryRequest appQueryRequest);

    /**
     * 管理员更新应用信息
     *
     * @param appAdminUpdateRequest 删除参数
     * @return 是否更新成功
     */
    Boolean updateAppByAdmin(AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 管理员分页获取应用
     *
     * @param appQueryRequest 分页查询参数
     * @return 分页结果
     */
    Page<AppVO> listAppVOByPageByAdmin(AppQueryRequest appQueryRequest);

    /**
     * 用户和 AI 对话生成应用代码
     *
     * @param appId     应用 ID
     * @param message   用户输入的内容
     * @param loginUser 登录用户
     * @return Flux<String> 流式响应代码
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 用户应用部署
     * @param appId 应用 ID
     * @param loginUser 登录用户
     * @return 项目部署返回的 url
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 异步生成应用截图
     *
     * @param deployUrl 应用部署 URL
     * @param appId 应用 ID
     */
    void generateAndUploadScreenshotAsync(String deployUrl, Long appId);

    /**
     * App 对象转 AppVO封装类
     *
     * @param app App对象
     * @return AppVO 对象
     */
    AppVO getAppVO(App app);

    /**
     * App 对象列表转 AppVO 列表
     *
     * @param appList App 对象列表
     * @return AppVO 列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 封装查询条件
     *
     * @param appQueryRequest 分页查询参数
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 下载应用项目代码
     *
     * @param appId 应用 ID
     * @param request 请求
     * @param response 响应
     */
    void downloadAppCode(Long appId, HttpServletRequest request, HttpServletResponse response);
}
