package com.huahai.huahaiaiappcreate.untils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

/**
 * 网页自动截图工具类
 *
 * @author huahai
 */
@Slf4j
public class WebScreenshotUtils {

    // 定义 webDriver 浏览器驱动
    private static final WebDriver webDriver;

    // 初始化 webDriver，确保驱动只能初始化一次
    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeWebDriver(DEFAULT_HEIGHT, DEFAULT_WIDTH);
    }

    // 在 JVM 关闭时销毁 webDriver
    @PreDestroy
    public void destroy() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    /**
     * 初始化 chrome 浏览器驱动
     *
     * @param height 截图的高度
     * @param width  截图的宽度
     * @return WebDriver
     */
    private static WebDriver initChromeWebDriver(int height, int width) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 获取网页截图
     *
     * @param webUrl 要截图的网页地址
     * @return 网页截图的本地保存路径
     */
    public static String saveWebPageScreenshot(String webUrl) {
        // 1. 参数校验
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页地址不能为空");
            return null;
        }
        try {
            // 2. 创建保存文件的临时目录
            String rootPath = System.getProperty("user.dir") + "/tmp/screenshots" + File.separator + RandomUtil.randomString(8);
            FileUtil.mkdir(rootPath);
            // 3. 创建截图保存路径
            // 3.1 定义文件名后缀
            final String FILE_SUFFIX = ".png";
            String imagePath = rootPath + File.separator + RandomUtil.randomNumbers(6) + FILE_SUFFIX;
            // 4. 访问网页
            webDriver.get(webUrl);
            // 5. 等待页面加载完成
            waitForPageLoad(webDriver);
            // 6. 截图
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            // 7. 保存原始文件
            saveImage(screenshotBytes, imagePath);
            log.info("网页截图原始图片保存成功: {}", imagePath);
            // 8. 压缩原始文件
            // 8.1 构造压缩文件的路径
            final String COMPRESS_FILE_SUFFIX = "_compress.jpg";
            String compressImagePath = rootPath + File.separator + RandomUtil.randomNumbers(6) + COMPRESS_FILE_SUFFIX;
            compressImage(imagePath, compressImagePath);
            log.info("网页截图压缩图片保存成功: {}", compressImagePath);
            // 9. 清理原始文件，保留压缩后的图片
            FileUtil.del(imagePath);
            // 10. 返回压缩文件路径
            return compressImagePath;
        } catch (Exception e) {
            log.error("网页截图失败", e);
            return null;
        }
    }

    /**
     * 等待页面加载完成
     *
     * @param driver 驱动
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            // 创建等待页面加载对象, 设置超时时间为10秒
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // 等待 document.readyState 为 complete
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                            .equals("complete")
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }

    /**
     * 保存图片到文件
     *
     * @param imageBytes 图片字节数组
     * @param imagePath  图片保存路径
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     *
     * @param originalImagePath   原图片路径
     * @param compressedImagePath 压缩图片保存路径
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        // 压缩图片质量（0.1 = 10% 质量）
        final float COMPRESSION_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }


}
