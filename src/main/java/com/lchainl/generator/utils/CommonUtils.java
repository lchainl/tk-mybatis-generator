package com.lchainl.generator.utils;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.FileResourceLoader;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 通用工具类
 *
 * @author ethan
 * @email xuyangit@163.com
 * @date
 */
public class CommonUtils {

    /**
     * 根据模板生成文件内容
     * @param templateClassPath 类路径模板
     * @param bindAttrs 绑定变量表
     * @return 生成结果内容
     * @throws IOException
     */
    public static String generate(String templateClassPath, Map<String, Object> bindAttrs) throws IOException {
        ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader(getPath(templateClassPath));
        Configuration cfg = Configuration.defaultConfiguration();
        GroupTemplate groupTemplate = new GroupTemplate(classpathResourceLoader, cfg);
        Template template = groupTemplate.getTemplate(getFile(templateClassPath));
        template.fastBinding(bindAttrs);
        return template.render();
    }

    /**
     * 生成代码到本地
     * @param templateFilePath 模板绝对路径
     * @param outputFilePath 输入文件绝对路径
     * @param bindAttrs 绑定变量表
     * @throws IOException
     */
    public static void generate(String templateFilePath, String outputFilePath, Map<String, Object> bindAttrs) throws IOException {
        FileResourceLoader fileResourceLoader = new FileResourceLoader(getPath(templateFilePath));
        Configuration cfg = Configuration.defaultConfiguration();
        GroupTemplate groupTemplate = new GroupTemplate(fileResourceLoader, cfg);
        Template template = groupTemplate.getTemplate(getFile(templateFilePath));
        template.fastBinding(bindAttrs);
        File outputFile = new File(outputFilePath);
        FileWriterWithEncoding writer = new FileWriterWithEncoding(outputFile, "utf-8");
        template.renderTo(writer);
    }

    /**
     * 根据文件绝对路径获取目录
     * @param filePath
     * @return
     */
    public static String getPath(String filePath) {
        String path = "";
        if (StringUtils.isNotBlank(filePath)) {
            path = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        }
        return path;
    }

    /**
     * 根据文件绝对路径获取文件
     * @param filePath
     * @return
     */
    public static String getFile(String filePath) {
        String file = "";
        if (StringUtils.isNotBlank(filePath)) {
            file = filePath.substring(filePath.lastIndexOf("/") + 1);
        }
        return file;
    }

    /**
     * 对象是否为空
     * 
     * @param obj
     * @return
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        return false;
    }

    /**
     * 判断整数是否大于零
     * 
     * @param number
     * @return
     */
    public static boolean isIntThanZero(int number) {
        if (number > 0) {
            return true;
        }
        return false;
    }

}
