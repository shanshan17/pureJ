package com.lamb.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * <p>Title : 配置myBatis</p>
 * <p>Description : Druid数据源、SqlSessionFactory、事务管理</p>
 * <p>Date : 2017/3/9 11:57</p>
 *
 * @author : hejie (hjnlxuexi@126.com)
 * @version : 1.0
 */
@Configuration
@EnableTransactionManagement
public class MyBatisConfig {
    /**
     * 数据库产品类型
     */
    @Value("${sql.path}")
    private String sqlPath;
    /**
     * 配置数据源
     * @return Druid数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "jdbc")
    public DataSource dataSource() {
        return new DruidDataSource();
    }

    /**
     * 配置SqlSessionFactory
     * @param dataSource 数据源
     * @return SqlSessionFactory实例
     * @throws Exception SqlSessionFactory实例创建异常
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean
                .setMapperLocations(resolver
                        .getResources(sqlPath));
        return sqlSessionFactoryBean.getObject();
    }

    /**
     * 配置事务管理
     * @param dataSource 数据源
     * @return DataSourceTransactionManager实例
     * @throws Exception 事务管理器创建异常
     */
    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource)
            throws Exception {
        return new DataSourceTransactionManager(dataSource);
    }
}
