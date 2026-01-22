package molip.server.friend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 요청 생성 응답")
public record FriendRequestResponse(
        @Schema(description = "요청 ID", example = "123") Long requestId) {}
