package molip.server.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.chat.dto.request.VideoSessionSyncRequest;
import molip.server.chat.dto.request.WebRtcTokenIssueRequest;
import molip.server.chat.dto.response.WebRtcTokenIssueResponse;
import molip.server.common.response.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "WebRTC", description = "WebRTC API")
public interface WebRtcApi {

    @Operation(summary = "WebRTC 토큰 발급")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "토큰 발급 성공",
                content =
                        @Content(
                                schema = @Schema(implementation = WebRtcTokenIssueResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "CAM_STUDY 방 아님/필수값 누락",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "참여자 권한 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "방/참여자 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "503",
                description = "토큰 발급 실패",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<WebRtcTokenIssueResponse>> issueWebRtcToken(
            @AuthenticationPrincipal UserDetails userDetails,
            Long roomId,
            WebRtcTokenIssueRequest request);

    @Operation(summary = "비디오 세션 동기화")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "동기화 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "필수값 누락/CAM_STUDY 방 아님",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "참여자 권한 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "방/참여자 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> syncVideoSession(
            @AuthenticationPrincipal UserDetails userDetails,
            Long roomId,
            VideoSessionSyncRequest request);
}
