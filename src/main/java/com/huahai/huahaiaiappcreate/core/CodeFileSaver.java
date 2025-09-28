package com.huahai.huahaiaiappcreate.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.huahai.huahaiaiappcreate.ai.model.HtmlCodeResult;
import com.huahai.huahaiaiappcreate.ai.model.MultiFileCodeResult;
import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 代码文件保存器
 *
 * @author huahai
 */
@Deprecated
public class CodeFileSaver {

    // 文件保存的根路径
    private static final String FILE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存 HTML 网页文件代码
     * @param htmlCodeResult HTML 网页文件代码结果对象
     * @return 保存后的文件
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult){
        // 校验参数
        if(htmlCodeResult == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件参数不能为空");
        }
        // 构造唯一文件路径
        String fileUrl = buildUniqueFileName(CodeGenTypeEnum.HTML.getValue());
        // 保存文件并返回
        writeToFile(fileUrl, "index.html", htmlCodeResult.getHtmlCode());
        return new File(fileUrl);
    }

    /**
     * 保存多文件网页代码
     * @param multiFileCodeResult 多文件网页代码结果对象
     * @return 保存后的文件
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        // 校验参数
        if(multiFileCodeResult == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件参数不能为空");
        }
        // 构造唯一文件路径
        String baseDirPath = buildUniqueFileName(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 保存文件并返回
        writeToFile(baseDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(baseDirPath, "style.css", multiFileCodeResult.getCssCode());
        writeToFile(baseDirPath, "script.js", multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 构造唯一文件路径 /tmp/code_output/filetype_雪花算法
     * @param fileType 文件类型
     * @return 文件路径
     */
    private static String buildUniqueFileName(String fileType){
        // 构造唯一文件路径
        String uniqueFileName = String.format("%s_%s", fileType, IdUtil.getSnowflakeNextIdStr());
        // 拼接文件路径
        String fileUrl = FILE_ROOT_PATH + File.separator + uniqueFileName;
        // 创建目录
        FileUtil.mkdir(fileUrl);
        return fileUrl;
    }

    /**
     * 保存单个文件的通用方法
     * @param fileUrl 文件根路径
     * @param fileName 文件名
     * @param content 文件内容
     * @return 保存后的文件
     */
    private static File writeToFile(String fileUrl, String fileName, String content){
        // 拼接文件保存的根路径
        String filePath = fileUrl + File.separator + fileName;
        // 保存文件并返回
        return FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

}
