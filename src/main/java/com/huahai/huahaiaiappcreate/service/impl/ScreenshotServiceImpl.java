package com.huahai.huahaiaiappcreate.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.manager.CosManager;
import com.huahai.huahaiaiappcreate.service.ScreenshotService;
import com.huahai.huahaiaiappcreate.untils.ThrowUtils;
import com.huahai.huahaiaiappcreate.untils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 截图服务接口实现类
 *
 * @author huahai
 */
@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页地址不能为空");
        // 生成本地截图
        log.info("开始生成截图: URL: {}", webUrl);
        String localImagePath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localImagePath), ErrorCode.OPERATION_ERROR, "本地生成截图失败");
        try {
            // 上传到 COS
            String cosUrl = uploadImageToCos(localImagePath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "上传图片到对象存储失败");
            log.info("网页截图生成并上传成功: {} -> {}", localImagePath, cosUrl);
            // 返回能访问的截图的 URL
            return cosUrl;
        } finally {
            // 清理本地临时文件
            cleanupLocalFile(localImagePath);
        }
    }

    /**
     * 上传本地截图到 COS
     *
     * @param localImagePath 本地图片路径
     * @return 可访问的 COS 图片的 URL
     */
    private String uploadImageToCos(String localImagePath) {
        // 校验参数
        if(StrUtil.isBlank(localImagePath)){
            return null;
        }
        File localFile = new File(localImagePath);
        if(!localFile.exists()){
            log.error("本地图片不存在: {}", localImagePath);
            return null;
        }
        // 生成 Cos 键
        String fileName = RandomUtil.randomString(8) + "_compress.jpg";
        String cosKey = generateScreenshotKey(fileName);
        // 上传到 COS 对象存储
        return cosManager.uploadFile(cosKey, localFile);
    }

    /**
     * 生成截图的对象存储键 cosKey
     *
     * @param fileName Cos 键
     * @return cosKey 生成的 Cos 键
     */
    private String generateScreenshotKey(String fileName) {
        // 用本地时间用于区分和管理对象存储中的截图
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots%s/%s", datePath, fileName);
    }

    /**
     * 清理本地临时文件
     *
     * @param localImagePath 本地图片路径
     */
    private void cleanupLocalFile(String localImagePath) {
        File localFile = new File(localImagePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("清理本地临时文件成功: {}", localImagePath);
        }
    }
}
