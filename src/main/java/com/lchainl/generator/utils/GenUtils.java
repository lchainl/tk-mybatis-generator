package com.lchainl.generator.utils;

import com.lchainl.generator.domain.ColumnDO;
import com.lchainl.generator.domain.TableDO;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *  代码生成器   工具类
 *
 * @author ethan
 * @email xuyangit@163.com
 * @date
 */
public class GenUtils {


    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();

        templates.add("templates/Controller.java.vm");
        templates.add("templates/Entity.java.vm");
        templates.add("templates/Mapper.java.vm");
        templates.add("templates/Mapper.xml.vm");
        templates.add("templates/Service.java.vm");
        templates.add("templates/ServiceImpl.java.vm");
        templates.add("templates/Vo.java.vm");
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns, ZipOutputStream zip) {
        //配置信息
        Configuration config = getConfig();
        //表信息
        TableDO tableDO = new TableDO();
        tableDO.setTableName(table.get("tableName"));
        tableDO.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String className = tableToJava(tableDO.getTableName(), config.getString("tablePrefix"), config.getString("autoRemovePre"));
        tableDO.setClassName(className);
        tableDO.setClassname(StringUtils.uncapitalize(className));
        boolean hasBigDecimal = false;
        //列信息
        List<ColumnDO> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnDO columnDO = new ColumnDO();
            columnDO.setColumnName(column.get("columnName"));
            columnDO.setDataType(column.get("dataType"));
            columnDO.setComments(column.get("columnComment"));
            columnDO.setExtra(column.get("extra"));

            if("decimal".equals(column.get("dataType"))){
                hasBigDecimal = true;
            }
            //列名转换成Java属性名
            String attrName = columnToJava(columnDO.getColumnName());
            columnDO.setAttrName(attrName);
            columnDO.setAttrname(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnDO.getDataType(), "unknowType");
            columnDO.setAttrType(attrType);

            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableDO.getPk() == null) {
                tableDO.setPk(columnDO);
            }

            columsList.add(columnDO);
        }
        tableDO.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (tableDO.getPk() == null) {
            tableDO.setPk(tableDO.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        //封装模板数据
        Map<String, Object> map = new HashMap<>(16);
        map.put("tableName", tableDO.getTableName());
        map.put("comments", tableDO.getComments());
        map.put("pk", tableDO.getPk());
        map.put("className", tableDO.getClassName());
        map.put("classname", tableDO.getClassname());
        map.put("pathName", config.getString("package").substring(config.getString("package").lastIndexOf(".") + 1));
        map.put("apiPathName", config.getString("apiPackage").substring(config.getString("apiPackage").lastIndexOf(".") + 1));
        map.put("columns", tableDO.getColumns());
        map.put("package", config.getString("package"));
        map.put("apiPackage", config.getString("apiPackage"));
        map.put("corePackage", config.getString("corePackage"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));

        if(hasBigDecimal){
            map.put("hasBigDecimal",true);
        }
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(
                        new ZipEntry(
                                getFileName(
                                        template,
                                        tableDO.getClassname(),
                                        tableDO.getClassName(),
                                        config.getString("package"),
                                        config.getString("corePackage"),
                                        config.getString("apiPackage"),
                                        config.getString("package").substring(config.getString("package").lastIndexOf(".") + 1),
                                        config.getString("corePackage").substring(config.getString("corePackage").lastIndexOf(".") + 1)
                                )));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix, String autoRemovePre) {
        if (Constant.AUTO_REOMVE_PRE.equals(autoRemovePre)) {
            tableName = tableName.substring(tableName.indexOf("_") + 1);
        }
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replaceFirst(tablePrefix, "");
        }

        return columnToJava(tableName);
    }

    /**
     * 权限标识
     *
     * @param url
     * @return
     */
    public static String urlToAuthKey(String url) {
        return url.replace("/", ":");
    }


    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        int beginIndex = tableName.indexOf(tablePrefix);
        if (beginIndex > -1) {
            tableName = tableName.substring(tablePrefix.length());
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("templates/generator.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String classname, String className, String packageName, String corePackageName, String apiPackageName, String mapperPackageName, String mapperCorePackageName) {
        /**
         * API
         */
        String corePackagePath = "main" + File.separator + "java" + File.separator;
        //String modulesname=config.getString("packageName");
        if (StringUtils.isNotBlank(corePackageName)) {
            corePackagePath += corePackageName.replace(".", File.separator) + File.separator;
        }

        if (template.contains(GenConstant.JAVA_ENTITY)) {
            return corePackagePath  + File.separator + "entity" + File.separator + className + ".java";
        }

        if (template.contains(GenConstant.JAVA_VO)) {
            return corePackagePath  + File.separator + "vo" + File.separator + className + "Vo.java";
        }

        if (template.contains(GenConstant.JAVA_MAPPER)) {
            return corePackagePath  + File.separator + "dao" + File.separator + className + "Mapper.java";
        }

        if (template.contains(GenConstant.JAVA_SERVICE)) {
            return corePackagePath  + File.separator + "service" + File.separator + className + "Service.java";
        }

        if (template.contains(GenConstant.JAVA_SERVICE_IMPL)) {
            return corePackagePath  + File.separator + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }

        if (template.contains(GenConstant.XML_MAPPER)) {
            return "main" + File.separator + "resources" + File.separator + "mybatis" + File.separator + mapperCorePackageName + File.separator + className + "Mapper.xml";
        }

        /**
         * api
         */
        String apiPackagePath = "main" + File.separator + "java" + File.separator;

        if (StringUtils.isNotBlank(apiPackageName)) {
            apiPackagePath += apiPackageName.replace(".", File.separator) + File.separator;
        }

        if (template.contains(GenConstant.JAVA_CONTROLLER)) {
            return apiPackagePath + "controller" + File.separator + className + "Controller.java";
        }

        /**
         * system
         */
        String packagePath = "main" + File.separator + "java" + File.separator;

        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator;
        }

        return "success";
    }
}
