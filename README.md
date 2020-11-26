##  tk-mybatis-generator

mybatis自动生成工具，集成tk-mybatis，可以通过接口访问，也可本地生成，支持单独打成jar包，集成到项目中使用

## 1:修改配置

    文件：application.yml
    参数：
      url: jdbc:mysql://localhost:3306/test?useSSL=false&useUnicode=true&characterEncoding=utf8
      username: root
      password:
    
    文件：resources/templates/generator.properties
      # 配置 controller 包名
      apiPackage=com.lchainl.api
      # 配置 core 包名
      corePackage=com.lchainl.core
      # 配置作者名
      author=ethan
      # 配置 Email 地址
      email=xuyangit@163.com
      # 表名是否转换成
      autoRemovePre=false
      # 表前缀
      tablePrefix=t_
  
## 2:启动服务

    启动类：com.lchainl.generator.LchainlGeneratorApplication

## 3:接口访问

    接口1 查询数据库所有表：http://localhost:9001/generator/list

    接口2 根据表名生成自动代码: http://localhost:9001/generator/{tableName}
  
    接口3 根据多个表名批量生成代码：http://localhost:9001/generator/batchCode?tables=
    
    接口4 批量生成所有表代码：http://localhost:9001/generator/allTables

## 4:生成后的代码结构

    main
      -- java
        -- com
          -- lchainl
            -- core
              -- vo
              -- service
                -- impl
              -- dao
              -- entity
            -- api  
              -- controller
      -- resources # 配置文件
        -- mybatis # Mapper文件路径
          -- core
    
  
