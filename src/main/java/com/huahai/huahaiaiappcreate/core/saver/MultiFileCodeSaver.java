package com.huahai.huahaiaiappcreate.core.saver;

import cn.hutool.core.util.StrUtil;
import com.huahai.huahaiaiappcreate.ai.model.MultiFileCodeResult;
import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码保存器
 *
 * @author huahai
 */
public class MultiFileCodeSaver extends CodeFileSaverTemplate<MultiFileCodeResult>{

    /**
     * 保存多文件代码
     * @return 保存的文件
     */
    @Override
    protected CodeGenTypeEnum getFileType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    /**
     * 保存多文件代码
     * @param result 解析文件的结果 MultiFileCodeResult
     * @param fileUrl 文件目录路径
     */
    @Override
    protected void saveFile(MultiFileCodeResult result, String fileUrl) {
        // 保存 HTML, CSS, JS 文件代码
        writeToFile(fileUrl, "index.html", result.getHtmlCode());
        writeToFile(fileUrl, "style.css", result.getCssCode());
        writeToFile(fileUrl, "script.js", result.getJsCode());
    }

    /**
     * 校验文件基础信息
     * @param result 解析文件结果
     */
    @Override
    protected void validFileInfo(MultiFileCodeResult result) {
        super.validFileInfo(result);
        // 校验参数
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML 代码不能为空");
        }
    }
}
