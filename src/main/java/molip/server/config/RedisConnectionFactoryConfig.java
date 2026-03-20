package molip.server.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConnectionFactoryConfig {

    private static final String MODE_SENTINEL = "sentinel";
    private static final String MODE_STANDALONE = "standalone";

    @Value("${spring.data.redis.mode}")
    private String mode;

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.sentinel.master}")
    private String sentinelMaster;

    @Value("${spring.data.redis.sentinel.nodes}")
    private String sentinelNodes;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        if (MODE_SENTINEL.equalsIgnoreCase(mode)) {
            return sentinelConnectionFactory();
        }

        if (!MODE_STANDALONE.equalsIgnoreCase(mode)) {
            throw new IllegalStateException("Unsupported redis mode: " + mode);
        }

        return standaloneConnectionFactory();
    }

    private LettuceConnectionFactory standaloneConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
        if (hasText(password)) {
            configuration.setPassword(RedisPassword.of(password));
        }

        return new LettuceConnectionFactory(configuration);
    }

    private LettuceConnectionFactory sentinelConnectionFactory() {
        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
        configuration.master(sentinelMaster);
        Arrays.stream(sentinelNodes.split(","))
                .map(String::trim)
                .filter(this::hasText)
                .map(this::toRedisNode)
                .forEach(configuration::sentinel);

        if (hasText(password)) {
            configuration.setPassword(RedisPassword.of(password));
        }

        return new LettuceConnectionFactory(configuration);
    }

    private RedisNode toRedisNode(String value) {
        String[] hostPort = value.split(":", 2);
        if (hostPort.length != 2 || !hasText(hostPort[0]) || !hasText(hostPort[1])) {
            throw new IllegalStateException("Invalid redis sentinel node: " + value);
        }

        try {
            return new RedisNode(hostPort[0].trim(), Integer.parseInt(hostPort[1].trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid redis sentinel port: " + value, exception);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
