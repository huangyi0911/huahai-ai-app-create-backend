package com.huahai.huahaiaiappcreate.langgraph4j.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.huahai.huahaiaiappcreate.langgraph4j.model.ImageResource;
import com.huahai.huahaiaiappcreate.langgraph4j.model.enums.ImageCategoryEnum;
import com.huahai.huahaiaiappcreate.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logo 阿里云AI 文生图工具
 *
 * @author huahai
 */
@Slf4j
@Component
public class LogoGeneratorTool {

    @Value("${dashscope.api-key:}")
    private String dashScopeApiKey;

    @Value("${dashscope.image-model:wan2.2-t2i-flash}")
    private String imageModel;

    @Resource
    private CosManager cosManager;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        try {
            // 构建 Logo 设计提示词
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(imageModel)
                    .prompt(logoPrompt)
                    .size("512*512")
                    // 生成 1 张足够，因为 AI 不知道哪张最好
                    .n(1)
                    .build();
            // 调用 imageSynthesis, 生成图片
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(param);
            // 定义一个数组来存储保存到 COS 对象存储的图片
            if (result != null && result.getOutput() != null && result.getOutput().getResults() != null) {
                List<Map<String, String>> results = result.getOutput().getResults();
                for (Map<String, String> imageResult : results) {
                    String imageUrl = imageResult.get("url");
                    if (StrUtil.isNotBlank(imageUrl)) {
                        // 把 logo 保存到本地并上传图片到 COS，并清理临时文件
                        String url = downloadAndUploadImage(imageUrl, ImageCategoryEnum.LOGO);
                        logoList.add(ImageResource.builder()
                                .category(ImageCategoryEnum.LOGO)
                                .description(description)
                                .url(url)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }

    /**
     * 下载图片并上传到 COS
     *
     * @param imageUrl 图片URL
     * @param category 图片分类
     * @return 上传到COS后的URL
     */
    public String downloadAndUploadImage(String imageUrl, ImageCategoryEnum category) {
        if (StrUtil.isBlank(imageUrl)) {
            return null;
        }

        // 下载图片到本地临时文件
        File tempFile = null;
        try {
            // 创建临时文件
            String fileExtension = getFileExtension(imageUrl);
            tempFile = FileUtil.createTempFile("image_", "." + fileExtension, true);
            // 下载图片
            HttpUtil.downloadFile(imageUrl, tempFile);
            // 生成COS键
            String cosKey = generateCosKey(category, fileExtension);
            // 上传到COS
            String cosUrl = cosManager.uploadFile(cosKey, tempFile);
            log.info("图片上传COS成功: {} -> {}", tempFile.getName(), cosUrl);
            return cosUrl;
        } catch (Exception e) {
            log.error("下载或上传图片失败: {}", e.getMessage(), e);
            return null;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
            }
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param imageUrl 图片URL
     * @return 文件扩展名
     */
    private String getFileExtension(String imageUrl) {
        String extension = "jpg";
        try {
            String path = new java.net.URL(imageUrl).getPath();
            int lastDotIndex = path.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = path.substring(lastDotIndex + 1);
            }
        } catch (Exception e) {
            log.warn("解析文件扩展名失败，使用默认jpg: {}", e.getMessage());
        }
        return extension;
    }

    /**
     * 生成COS键
     *
     * @param category 图片分类
     * @param extension 文件扩展名
     * @return COS键
     */
    private String generateCosKey(ImageCategoryEnum category, String extension) {
        // 使用日期作为路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        // 生成唯一文件名
        String fileName = IdUtil.fastSimpleUUID() + "." + extension;
        return String.format("/images/%s/%s/%s", category.getValue().toLowerCase(), datePath, fileName);
    }
}
