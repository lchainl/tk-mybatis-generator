package com.lchainl.generator.utils;

import com.lchainl.generator.entity.ColumnEntity;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.util.*;

/**
 * 代码生成工具类（使用jdbc生成本地代码）
 *
 * @author ethan
 * @email xuyangit@163.com
 * @date
 */
public class JdbcGenUtils {

    public static final String tablePrefix = "t_";
    public static final String mongoPrefix = "c_";
    public static final String COLUMN_CREATE = "create_time";//数据库表中的创建时间字段名
    public static final String COLUMN_NAME = "name";//数据库表中的名称字段名

    public static void generatorCode(String jdbcDriver, String jdbcUrl, String jdbcUsername, String jdbcPassword,
                                     String tblName, String javaModule, String webModule, String mode) throws Exception {
        String rootPath = "";
        String osName = "os.name";
        String osWindows = "win";
        if (!System.getProperty(osName).toLowerCase().startsWith(osWindows)) {
            rootPath = "/";
        }

        String tableSql = "SELECT table_name, table_comment FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = (SELECT DATABASE()) AND table_name = '"
                          + tblName + "';";

        JdbcUtils jdbcUtils = new JdbcUtils(jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword);
        List<Map> tableList = jdbcUtils.selectByParams(tableSql, null);

        TableEntity table = null;
        List<TableEntity> tables = new ArrayList();
        String classPrefix = "";
        if (mode.equals("biz")) {
            classPrefix = "Admin";//设置前缀避免组件名字重名冲突
        }else if (mode.equals("api")) {
            classPrefix = "Api";//设置前缀避免组件名字重名冲突
        }

        Iterator<Map> tableIterator = tableList.iterator();
        while (tableIterator.hasNext()) {
            Map<String, String> currTable = tableIterator.next();
            table = new TableEntity();
            String tableName = currTable.get("TABLE_NAME");
            String className = GenUtils.tableToJava(tableName, "t_");
            table.setTableName(tableName);
            table.setInitalClassName(className);
            table.setClassName(classPrefix + className);// 添加前缀
            table.setObjName(StringUtils.uncapitalize(classPrefix + className));// 添加前缀
            table.setTableComment(currTable.get("TABLE_COMMENT"));

            String columnSql = "SELECT column_name,data_type,column_comment,column_key,extra FROM information_schema.columns WHERE TABLE_NAME = '"
                               + tableName + "' AND table_schema = (SELECT DATABASE()) ORDER BY ordinal_position";
            ColumnEntity columnEntity = null;
            List<Map> columnList = jdbcUtils.selectByParams(columnSql, null);
            Iterator<Map> columnIterator = columnList.iterator();
            while (columnIterator.hasNext()) {
                Map<String, String> currColumn = columnIterator.next();
                columnEntity = new ColumnEntity();
                columnEntity.setExtra(currColumn.get("EXTRA"));

                String columnName = currColumn.get("COLUMN_NAME");
                String methodName = GenUtils.columnToJava(columnName);
                columnEntity.setColumnName(columnName);
                columnEntity.setFieldName(StringUtils.uncapitalize(methodName));
                columnEntity.setMethodName(methodName);

                columnEntity.setColumnKey(currColumn.get("COLUMN_KEY"));
                columnEntity.setDataType(currColumn.get("DATA_TYPE"));
                columnEntity.setColumnComment(currColumn.get("COLUMN_COMMENT"));

                // 属性类型
                columnEntity.setFieldType(PropertiesUtils.getInstance("templates/generator")
                    .get(columnEntity.getDataType()));

                // 主键判断
                if ("PRI".equals(columnEntity.getColumnKey()) && table.getPk() == null) {
                    table.setPk(columnEntity);
                }

                table.addColumn(columnEntity);
            }
            tables.add(table);
        }

        // 没主键，则第一个字段为主键
        if (table.getPk() == null) {
            table.setPk(table.getColumns().get(0));
        }

        String projectPath = getProjectPath();
        System.out.println("===>>>java generation path:" + projectPath + "/src/main/java");
        Map<String, Object> map = null;
        for (TableEntity tableEntity : tables) {
            // 封装模板数据
            map = new HashMap(16);
            String tableNameNoPrefix = tableEntity.getTableName().replaceFirst(tablePrefix, "");
            map.put("tableName", tableEntity.getTableName());
            map.put("comments", tableEntity.getTableComment());
            map.put("pk", tableEntity.getPk());
            map.put("className", tableEntity.getClassName());
            map.put("objName", tableEntity.getObjName());
            map.put("functionCode", webModule);
            map.put("requestMapping", tableNameNoPrefix.replace("_", "/"));
            map.put("viewPath", webModule + "/" + tableEntity.getInitalClassName().toLowerCase());
            map.put("authKey", GenUtils.urlToAuthKey(tableNameNoPrefix.replace("_", "/")));
            map.put("columns", tableEntity.getColumns());
            map.put("hasDecimal", tableEntity.buildHasDecimal().getHasDecimal());
            map.put("package", PropertiesUtils.getInstance("templates/generator").get("package"));
            map.put("module", javaModule);
            map.put("author", PropertiesUtils.getInstance("templates/generator").get("author"));
            map.put("email", PropertiesUtils.getInstance("templates/generator").get("email"));
            map.put("mode", mode);
            
            //处理特定字段如创建时间，名称， 编码，手机号,状态,类型
            String columnCreate = "";
            String columnName = "";
            String columnCode = "";
            String columnPhone = "";
            String columnStatus = "";
            String columnType = "";
            for (ColumnEntity column : tableEntity.getColumns()) {
                String tableColumnName = column.getColumnName();
                if (tableColumnName.equalsIgnoreCase(COLUMN_CREATE) || tableColumnName.equalsIgnoreCase("gmt_create")) {
                    columnCreate = tableColumnName;
                    continue;
                } 
                if (tableColumnName.endsWith("name")) {
                    columnName = tableColumnName;
                    continue;
                }
                if (tableColumnName.endsWith("code")) {
                    columnCode = tableColumnName;
                    continue;
                }
                if (tableColumnName.contains("phone")) {
                    columnPhone = tableColumnName;
                    continue;
                }
                if (tableColumnName.equalsIgnoreCase("status")) {
                    columnStatus = tableColumnName;
                    continue;
                }
                if (tableColumnName.endsWith("type")) {
                    columnType = tableColumnName;
                    continue;
                }
            }
            map.put("columnCreate", columnCreate);
            map.put("columnName", columnName);
            map.put("columnCode", columnCode);
            map.put("columnPhone", columnPhone);
            map.put("columnStatus", columnStatus);
            map.put("columnType", columnType);

            System.out.println("============ start table: " + tableEntity.getTableName() + " ================");

            for (String template : GenUtils.getTemplates()) {
                String filePath = getFileName(template, javaModule, webModule, tableEntity.getClassName(), rootPath, mode, tableEntity.getInitalClassName());
                String templatePath = rootPath
                                      + JdbcUtils.class.getResource("/" + template).getPath().replaceFirst("/", "");
                System.out.println(filePath);
                File dstDir = new File(CommonUtils.getPath(filePath));
                //文件夹不存在创建文件夹
                if (!dstDir.exists()) {
                    dstDir.mkdirs();
                }
                File dstFile = new File(filePath);
                //文件不存在则创建
                if (!dstFile.exists()) {
                    CommonUtils.generate(templatePath, filePath, map);
                    System.out.println(filePath + "===>>>创建成功！");
                } else {
                    System.out.println(filePath + "===>>>文件已存在，未重新生成！");
                }
            }
            System.out.println("============ finish table: " + tableEntity.getTableName() + " ================\n");
        }
    }

    public static String getFileName(String template, String javaModule, String webModule, String className,
                                     String rootPath, String mode, String initalClassName) {
        String packagePath = rootPath + getProjectPath() + "/src/main/java/"
                             + PropertiesUtils.getInstance("templates/generator").get("package").replace(".", "/") + "/"
                             + javaModule + "/";
        String resourcePath = rootPath + getProjectPath() + "/src/main/resources/";
        String sqlPath = rootPath + getProjectPath() + "/src/main/sql/";
        String webPath = rootPath + getProjectPath() + "/src/main/webapp/";
        if (template.contains(GenConstant.JAVA_ENTITY)) {
            return packagePath + "entity/"  + className + "Entity.java";
        }

        if (template.contains(GenConstant.JAVA_VO)) {
            return packagePath + "vo/"  + className + "Vo.java";
        }

        if (template.contains(GenConstant.JAVA_MAPPER)) {
            return packagePath + "mapper/" + className + "Mapper.java";
        }

        if (template.contains(GenConstant.XML_MAPPER)) {
            return resourcePath + "mapper/" + mode + "/"  + javaModule + "/"  + className + "Mapper.xml";
        }

        if (template.contains(GenConstant.JAVA_SERVICE)) {
            return packagePath + "service/"  + className + "Service.java";
        }

        if (template.contains(GenConstant.JAVA_SERVICE_IMPL)) {
            return packagePath + "service/impl/"  + className + "ServiceImpl.java";
        }

        if (template.contains(GenConstant.JAVA_CONTROLLER)) {
            return packagePath + "controller/"  + className + "Controller.java";
        }

        if (template.contains(GenConstant.HTML_LIST)) {
            return webPath + "WEB-INF/view/" + webModule + "/" + initalClassName.toLowerCase() + "/list.html";
        }

        if (template.contains(GenConstant.HTML_ADD)) {
            return webPath + "WEB-INF/view/" + webModule + "/" + initalClassName.toLowerCase() + "/add.html";
        }

        if (template.contains(GenConstant.HTML_EDIT)) {
            return webPath + "WEB-INF/view/" + webModule + "/" + initalClassName.toLowerCase() + "/edit.html";
        }

        if (template.contains(GenConstant.JS_LIST)) {
            return webPath + "static/js/" + webModule + "/" + initalClassName.toLowerCase() + "/list.js";
        }

        if (template.contains(GenConstant.JS_ADD)) {
            return webPath + "static/js/" + webModule + "/" + initalClassName.toLowerCase() + "/add.js";
        }

        if (template.contains(GenConstant.JS_EDIT)) {
            return webPath + "static/js/" + webModule + "/" + initalClassName.toLowerCase() + "/edit.js";
        }

        return null;
    }

    public static String getProjectPath() {
        String basePath = JdbcUtils.class.getResource("/").getPath().replace("/target/classes/", "")
            .replaceFirst("/", "");
        return basePath;
    }

}
