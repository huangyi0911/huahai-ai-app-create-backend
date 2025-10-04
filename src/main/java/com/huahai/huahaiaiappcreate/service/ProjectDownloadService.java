package com.huahai.huahaiaiappcreate.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目代码下载服务接口
 *
 * @author huahai
 */
public interface ProjectDownloadService {


    /**
     * 项目代码打包下载
     *
     * @param projectPath 项目跟目录路径
     * @param downloadFileName 下载文件名
     * @param response 请求头响应
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);

}
