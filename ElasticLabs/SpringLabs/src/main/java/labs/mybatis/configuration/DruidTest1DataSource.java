package labs.mybatis.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import java.io.IOException;
import javax.sql.DataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class DruidTest1DataSource /*implements EnvironmentAware*/ {

    @Value("${mybatis.configuration.mapUnderscoreToCamelCase}")
    private boolean mapUnderscoreToCamelCase;
    @Value("${spring.datasource.druid.test1.url}")
    private String url;
    @Value("${spring.datasource.druid.test1.username}")
    private String username;
    @Value("${spring.datasource.druid.test1.password}")
    private String password;

    public boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }

    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    private static Environment environment;
//    @Override public void setEnvironment(Environment environment) {
//        DruidTest1DataSource.environment = environment;
//    }

//    @Primary
    @Bean(name = "test1DataSource")
    public DataSource dataSource(
        @Qualifier(value = "dataSourcePropertiesGenerator") DruidDataSourcePropertiesGenerator generator
    ) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        generator.dataSource(dataSource);
        return dataSource;
    }

//    @Primary
    @Bean(name = "test1SqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory(
        @Qualifier(value = "test1DataSource") DataSource dataSource
    ) throws IOException {
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(mapUnderscoreToCamelCase);

        SqlSessionFactoryBean ssfb = new SqlSessionFactoryBean();
        ssfb.setDataSource(dataSource);
        ssfb.setConfiguration(configuration);
        ResourcePatternResolver tempResourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = tempResourcePatternResolver.getResources("classpath:mappers/test1/*.xml");
        ssfb.setMapperLocations(resources);
        return ssfb;
    }

    @Bean(name = "test1MapperScannerConfigurer")
    public /*static*/ MapperScannerConfigurer test1MapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setSqlSessionFactoryBeanName("test1SqlSessionFactory");
        configurer.setBasePackage("labs.mybatis.dao.test1");
        return configurer;
    }

}
