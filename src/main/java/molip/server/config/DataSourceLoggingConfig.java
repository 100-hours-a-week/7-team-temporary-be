package molip.server.config;

import java.util.Arrays;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Slf4j
public class DataSourceLoggingConfig {

    @Bean
    public ApplicationRunner dataSourceLogger(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource,
            @Qualifier("migrationDataSource") DataSource migrationDataSource) {
        return args -> {
            logDataSource("write", writeDataSource);
            logDataSource("read", readDataSource);
            logDataSource("migration", migrationDataSource);
        };
    }

    private void logDataSource(String label, DataSource dataSource) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String url = jdbcTemplate.queryForObject("select database()", String.class);
            log.info("DataSource [{}] database={}", label, url);
        } catch (Exception e) {
            log.warn(
                    "DataSource [{}] database lookup failed: {}",
                    label,
                    Arrays.toString(e.getStackTrace()));
        }
    }
}
