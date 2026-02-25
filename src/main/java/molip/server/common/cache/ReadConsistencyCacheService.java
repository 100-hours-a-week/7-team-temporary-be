package molip.server.common.cache;

import java.util.Optional;
import molip.server.reflection.dto.cache.ReflectionCachePayload;
import molip.server.schedule.dto.cache.DayPlanCachePayload;
import molip.server.user.dto.cache.UserCachePayload;

public interface ReadConsistencyCacheService {

    void cacheUser(UserCachePayload payload);

    Optional<UserCachePayload> getUser(Long userId);

    void evictUser(Long userId);

    void cacheReflection(ReflectionCachePayload payload);

    Optional<ReflectionCachePayload> getReflection(Long reflectionId);

    void evictReflection(Long reflectionId);

    void cacheDayPlan(DayPlanCachePayload payload);

    Optional<DayPlanCachePayload> getDayPlan(Long dayPlanId);

    void evictDayPlan(Long dayPlanId);
}
