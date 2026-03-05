package molip.server.auth.store.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisTokenBlacklistStoreTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private RedisTokenBlacklistStore redisTokenBlacklistStore;

    @Test
    void 블랙리스트_토큰을_저장하고_조회할_수_있다() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("auth:blacklist:user:1")).willReturn("token-value");
        given(
                        redisTemplate.hasKey(
                                org.mockito.ArgumentMatchers.startsWith("auth:blacklist:token:")))
                .willReturn(true);

        // when
        redisTokenBlacklistStore.add(1L, "token-value");
        boolean containsByUser = redisTokenBlacklistStore.contains(1L, "token-value");
        boolean containsByToken = redisTokenBlacklistStore.contains("token-value");

        // then
        then(valueOperations).should().set("auth:blacklist:user:1", "token-value");
        assertThat(containsByUser).isTrue();
        assertThat(containsByToken).isTrue();
    }
}
