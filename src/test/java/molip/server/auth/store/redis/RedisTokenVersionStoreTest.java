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
class RedisTokenVersionStoreTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private RedisTokenVersionStore redisTokenVersionStore;

    @Test
    void 토큰버전이_없으면_초기값_1을_반환한다() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("auth:token-version:1")).willReturn("1");

        // when
        long version = redisTokenVersionStore.getOrInit(1L);

        // then
        then(valueOperations).should().setIfAbsent("auth:token-version:1", "1");
        assertThat(version).isEqualTo(1L);
    }

    @Test
    void 토큰버전을_증가시킬_수_있다() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment("auth:token-version:1")).willReturn(2L);

        // when
        long updated = redisTokenVersionStore.increment(1L);

        // then
        assertThat(updated).isEqualTo(2L);
    }
}
