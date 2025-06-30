package com.lwf.ytlivechatanalyse.interceptor;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    public DynamicRoutingDataSource dynamicRoutingDataSource(DynamicDataSourceProperties properties) {
        Map<Object, Object> targetDataSources = new HashMap<>();

        // 加载配置的所有数据源
        for (Map.Entry<String, DynamicDataSourceProperties.DataSourceProperty> entry : properties.getDatasource().entrySet()) {
            String dsName = entry.getKey();
            DynamicDataSourceProperties.DataSourceProperty config = entry.getValue();

            DataSource dataSource = DataSourceBuilder.create()
                    .url(config.getUrl())
                    .username(config.getUsername())
                    .password(config.getPassword())
                    .driverClassName(config.getDriverClassName())
                    .build();

            targetDataSources.put(dsName, dataSource);
        }

        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(targetDataSources.get(properties.getPrimary()));
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DynamicRoutingDataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DynamicRoutingDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

