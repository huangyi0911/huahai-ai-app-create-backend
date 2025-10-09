package com.huahai.huahaiaiappcreate.untils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存 Key 工具类
 *
 * @author huahai
 */
public class CacheKeyUtils {

    /**
     * 生成缓存 Key
     *
     * @param obj 对象
     * @return MD5 加密后返回的缓存 key
     */
    public static String generateKey(Object obj){
        if(obj == null){
            return DigestUtil.md5Hex("null");
        }
        // 先将 obj 对象转为 JSON 字符串
        String jsonObj = JSONUtil.toJsonStr(obj);
        // 返回 MD5 加密后的字符串
        return DigestUtil.md5Hex(jsonObj);
    }
}
