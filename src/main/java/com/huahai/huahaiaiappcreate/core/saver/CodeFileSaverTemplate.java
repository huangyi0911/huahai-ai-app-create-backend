package com.huahai.huahaiaiappcreate.core.saver;

import cn.hutool.core.io.FileUtil;
import com.huahai.huahaiaiappcreate.constants.AppConstant;
import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 代码文件保存器模版 - 模板设计模式
 *
 * @author huahai
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存的根路径
    private static final String FILE_ROOT_PATH = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 保存代码文件到本地
     *
     * @param result 解析文件的结果 （HtmlCodeResult 或者 MultiFileCodeResult）
     * @return 保存的文件
     */
    public final File saveCode(T result, Long appId){
        // 1. 校验文件信息
        validFileInfo(result);
        // 2. 构造文件唯一路径
        String fileUrl = buildUniqueFileName(appId);
        // 3. 保存文件
        saveFile(result, fileUrl);
        // 4. 返回文件信息
        return new File(fileUrl);
    }

    /**
     * 校验文件基础信息
     *
     * @param result 解析文件结果
     */
    protected void validFileInfo(T result) {
        if(result == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件参数不能为空");
        }
    }

    /**
     * 构造唯一文件路径 /tmp/code_output/filetype_雪花算法
     *
     * @return 文件路径
     */
    protected final String buildUniqueFileName(Long appId){
        // 获取文件类型 - 由子类实现
        String fileType = getFileType().getValue();
        // 构造唯一文件路径
        String uniqueFileName = String.format("%s_%s", fileType, appId);
        // 拼接文件路径
        String fileUrl = FILE_ROOT_PATH + File.separator + uniqueFileName;
        // 创建目录
        FileUtil.mkdir(fileUrl);
        return fileUrl;
    }

    /**
     * 保存单个文件的通用方法
     *
     * @param fileUrl 文件根路径
     * @param fileName 文件名
     * @param content 文件内容
     * @return 保存后的文件
     */
    protected static File writeToFile(String fileUrl, String fileName, String content){
        // 拼接文件保存的根路径
        String filePath = fileUrl + File.separator + fileName;
        // 保存文件并返回
        return FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 获取文件类型 - 由子类实现
     *
     * @return 文件类型枚举
     */
    protected abstract CodeGenTypeEnum getFileType();

    /**
     * 保存文件到本地
     *
     * @param result 解析文件的结果 （HtmlCodeResult 或者 MultiFileCodeResult）
     * @param fileUrl 文件目录路径
     */
    protected abstract void saveFile(T result, String fileUrl);

}
