package com.huahai.huahaiaiappcreate.core.saver;

import cn.hutool.core.util.StrUtil;
import com.huahai.huahaiaiappcreate.ai.model.HtmlCodeResult;
import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;

/**
 * HTML 代码文件保存器
 *
 * @author huahai
 */
public class HtmlCodeFileSaver extends CodeFileSaverTemplate<HtmlCodeResult>{

    /**
     * 保存文件到本地
     *
     * @param result 解析文件的结果 HtmlCodeResult
     * @param fileUrl 文件目录路径
     */
    @Override
    protected void saveFile(HtmlCodeResult result, String fileUrl) {
        // 保存 HTML 文件代码
        writeToFile(fileUrl, "index.html", result.getHtmlCode());
    }

    /**
     * 获取文件类型枚举
     *
     * @return 文件类型枚举
     */
    @Override
    protected CodeGenTypeEnum getFileType() {
        return CodeGenTypeEnum.HTML;
    }

    /**
     * 校验文件基础信息
     *
     * @param result 解析文件结果
     */
    @Override
    protected void validFileInfo(HtmlCodeResult result) {
        // 校验参数
        super.validFileInfo(result);
        // 校验 HTML 代码不能为空
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML 代码不能为空");
        }
    }
}
