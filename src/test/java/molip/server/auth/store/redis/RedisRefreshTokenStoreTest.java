package molip.server.auth.store.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisRefreshTokenStoreTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private RedisRefreshTokenStore redisRefreshTokenStore;

    @Test
    void 리프레시토큰을_저장하고_검증할_수_있다() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("auth:refresh:1:device-1")).willReturn("hashed-token");

        // when
        redisRefreshTokenStore.save(1L, "device-1", "hashed-token");
        boolean matches = redisRefreshTokenStore.matches(1L, "device-1", "hashed-token");

        // then
        then(valueOperations).should().set("auth:refresh:1:device-1", "hashed-token");
        assertThat(matches).isTrue();
    }

    @Test
    void 유저의_모든_디바이스_리프레시토큰을_삭제할_수_있다() {
        // when
        redisRefreshTokenStore.deleteAll(1L, Set.of("device-1", "device-2"));

        // then
        then(redisTemplate).should().delete("auth:refresh:1:device-1");
        then(redisTemplate).should().delete("auth:refresh:1:device-2");
    }
}
