package molip.server.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "액세스 토큰 응답")
public record AccessTokenResponse(
        @Schema(description = "액세스 토큰", example = "dsifhsfgwgr") String accessToken) {}
