package molip.server.friend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.enums.FriendRequestStatus;

@Schema(description = "친구 요청 상태 변경 요청")
public record FriendRequestStatusUpdateRequest(
    @Schema(description = "요청 상태", example = "ACCEPTED") FriendRequestStatus status) {}
