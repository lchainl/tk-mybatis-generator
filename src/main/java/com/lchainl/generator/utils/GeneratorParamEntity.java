package com.lchainl.generator.utils;

import lombok.Data;

/**
 * 代码生成器请求参数
 *
 * @author ethan
 * @email xuyangit@163.com
 * @date
 */

@Data
public class GeneratorParamEntity {

    private String[] tables;

    /**
     * 系统模块
     */
    private String   module;

    /**
     * 功能编码
     */
    private String   functionCode;

    /**
     * 后台请求地址
     */
    private String   requestMapping;

    /**
     * 页面路径
     */
    private String   viewPath;

    /**
     * 生成类型，1：生成包结构，2：只生成源代码
     */
    private String   type;
}
