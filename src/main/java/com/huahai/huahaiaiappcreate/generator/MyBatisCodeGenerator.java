package com.huahai.huahaiaiappcreate.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

/**
 * MyBatis Flex 代码生成器
 *
 * @author huahai
 */
public class MyBatisCodeGenerator {

    // 需要生成的表名
    private static final String[] TABLE_NAMES = {"app"};

    /**
     * 主函数， 运行该方法即可生成代码
     *
     * @param args
     */
    public static void main(String[] args) {
        // 获取数据源信息
        // 获取 application.yml 文件中的数据源信息
        Dict dictMain = YamlUtil.loadByPath("application.yml");
        // 获取 application-dev.yml 文件中的数据库信息
        Dict dictDev = YamlUtil.loadByPath("application-dev.yml");

        // 合并配置
        mergeConfig(dictMain, dictDev);

        // 4. 从合并后的配置中获取数据源信息
        Map<String, Object> dataSourceConfig = dictMain.getByPath("spring.datasource");
        // 从 dataSourceConfig 中获取用于拼接 URL 的各个部分
        String host = String.valueOf(dataSourceConfig.get("host"));
        Integer port = (Integer) dataSourceConfig.get("port");
        String database = String.valueOf(dataSourceConfig.get("database"));

        // 手动拼接 JDBC URL
        String url = String.format(
                "jdbc:mysql://%s:%d/%s?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8",
                host, port, database
        );
        String username = String.valueOf(dataSourceConfig.get("username"));
        String password = String.valueOf(dataSourceConfig.get("password"));

        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 创建配置内容
        GlobalConfig globalConfig = createGlobalConfig();

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        // 生成代码
        generator.generate();
    }


    /**
     * 合并配置：将devConfig的内容合并到mainConfig中（覆盖同名键）
     *
     * @param mainConfig
     * @param devConfig
     */
    private static void mergeConfig(Dict mainConfig, Dict devConfig) {
        if (MapUtil.isEmpty(devConfig)) {
            return;
        }
        // 递归合并所有层级的配置
        for (Map.Entry<String, Object> entry : devConfig.entrySet()) {
            String key = entry.getKey();
            Object devValue = entry.getValue();
            Object mainValue = mainConfig.get(key);

            if (devValue instanceof Dict && mainValue instanceof Dict) {
                // 嵌套配置递归合并
                mergeConfig((Dict) mainValue, (Dict) devValue);
            } else {
                // 直接覆盖主配置
                mainConfig.set(key, devValue);
            }
        }
    }

    /**
     * 创建全局配置
     *
     * @return
     */
    // 详细配置见：https://mybatis-flex.com/zh/others/codegen.html
    public static GlobalConfig createGlobalConfig() {
        // 创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 设置根包，建议先生成到一个临时目录下，生成代码后，再移动到项目目录下
        globalConfig.getPackageConfig()
                .setBasePackage("com.huahai.huahaiaiappcreate.generateResult");

        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES)
                // 设置逻辑删除的默认字段名称
                .setLogicDeleteColumn("isDelete");

        // 设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        // 设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        // 设置生成 service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();

        // 设置生成 controller
        globalConfig.enableController();

        // 设置生成时间和字符串为空，避免多余的代码改动
        globalConfig.getJavadocConfig()
                .setAuthor("<a href=\"https://github.com/huangyi0911\">花海</a>")
                .setSince("");
        return globalConfig;
    }
}
