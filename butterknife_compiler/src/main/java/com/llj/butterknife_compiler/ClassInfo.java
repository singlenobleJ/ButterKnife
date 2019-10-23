package com.llj.butterknife_compiler;

import java.util.List;

/**
 * @author: lilinjie
 * @date: 2019-10-22 16:21
 * @description: 类信息封装
 */
class ClassInfo {
    /**
     * 包名
     */
    String packageName;
    /**
     * 完整类名
     */
    String fullClassName;
    /**
     * 简单类名
     */
    String simpleClassName;
    /**
     * 类中注解字段
     */
    List<FieldInfo> fields;

}
