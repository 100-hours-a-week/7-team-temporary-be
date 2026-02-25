package molip.server.common.cache;

public final class CacheKey {

    private CacheKey() {}

    public static String user(Long userId) {
        return "user:" + userId;
    }

    public static String reflection(Long reflectionId) {
        return "reflection:" + reflectionId;
    }

    public static String dayPlan(Long dayPlanId) {
        return "dayplan:" + dayPlanId;
    }
}
