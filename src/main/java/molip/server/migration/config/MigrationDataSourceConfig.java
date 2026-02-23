package molip.server.migration.config;

import javax.sql.DataSource;
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
@ConditionalOnProperty(
        name = {"migration.enabled", "migration.datasource.url"},
        havingValue = "true")
public class MigrationDataSourceConfig {

    @Bean
    @ConfigurationProperties("migration.datasource")
    public DataSourceProperties migrationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "migrationDataSource")
    public DataSource migrationDataSource(
            @NonNull DataSourceProperties migrationDataSourceProperties) {
        return migrationDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "migrationJdbcTemplate")
    public JdbcTemplate migrationJdbcTemplate(DataSource migrationDataSource) {
        return new JdbcTemplate(migrationDataSource);
    }

    @Bean(name = "migrationTransactionManager")
    public PlatformTransactionManager migrationTransactionManager(DataSource migrationDataSource) {
        return new DataSourceTransactionManager(migrationDataSource);
    }
}
