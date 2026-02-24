package molip.server.config;

import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MainDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
}
