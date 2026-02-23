package molip.server.user.dto.cache;

import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;
import molip.server.user.entity.Users;

public record UserCachePayload(
        Long userId,
        long version,
        String email,
        String nickname,
        Gender gender,
        String birth,
        FocusTimeZone focusTimeZone,
        String dayEndTime,
        String profileImageKey,
        String deletedAt) {

    public static UserCachePayload from(Users user, String profileImageKey, Long version) {
        return new UserCachePayload(
                user.getId(),
                resolveVersion(version),
                user.getEmail(),
                user.getNickname(),
                user.getGender(),
                user.getBirth() == null ? null : user.getBirth().toString(),
                user.getFocusTimeZone(),
                user.getDayEndTime() == null ? null : user.getDayEndTime().toString(),
                profileImageKey,
                user.getDeletedAt() == null ? null : user.getDeletedAt().toString());
    }

    private static long resolveVersion(Long version) {
        return version == null ? 0L : Math.max(0L, version);
    }
}
