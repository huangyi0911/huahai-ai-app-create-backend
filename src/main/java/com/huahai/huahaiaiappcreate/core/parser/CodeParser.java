package com.huahai.huahaiaiappcreate.core.parser;

/**
 * 代码解析器接口
 *
 * @author huahai
 * @param <T>
 */
public interface CodeParser<T> {

    /**
     * 代码解析的方法，交给子类实现
     *
     * @param codeContent 代码内容
     * @return 解析后的对象
     */
    T parseCode(String codeContent);
}
