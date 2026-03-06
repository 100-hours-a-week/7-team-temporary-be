package molip.server.ai.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnProperty(name = "ai.datasource.url")
public class AiDataSourceConfig {

    @Bean(name = "aiDataSourceProperties")
    @ConfigurationProperties("ai.datasource")
    public DataSourceProperties aiDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "aiDataSource")
    public DataSource aiDataSource(
            @NonNull @Qualifier("aiDataSourceProperties")
                    DataSourceProperties aiDataSourceProperties) {
        return aiDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "aiJdbcTemplate")
    public JdbcTemplate aiJdbcTemplate(@Qualifier("aiDataSource") DataSource aiDataSource) {
        return new JdbcTemplate(aiDataSource);
    }

    @Bean(name = "aiTransactionManager")
    public PlatformTransactionManager aiTransactionManager(
            @Qualifier("aiDataSource") DataSource aiDataSource) {
        return new DataSourceTransactionManager(aiDataSource);
    }
}
