package molip.server.schedule.dto.cache;

import molip.server.schedule.entity.DayPlan;

public record DayPlanCachePayload(
        Long dayPlanId,
        Long userId,
        long version,
        String planDate,
        Integer aiUsageRemainingCount,
        String deletedAt) {

    public static DayPlanCachePayload from(DayPlan dayPlan) {
        return new DayPlanCachePayload(
                dayPlan.getId(),
                dayPlan.getUser().getId(),
                resolveVersion(dayPlan.getVersion()),
                dayPlan.getPlanDate() == null ? null : dayPlan.getPlanDate().toString(),
                dayPlan.getAiUsageRemainingCount(),
                dayPlan.getDeletedAt() == null ? null : dayPlan.getDeletedAt().toString());
    }

    private static long resolveVersion(Long version) {
        return version == null ? 0L : Math.max(0L, version);
    }
}
