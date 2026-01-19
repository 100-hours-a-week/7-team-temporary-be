package molip.server.friend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.response.ServerResponse;
import molip.server.common.response.PageResponse;
import molip.server.friend.dto.request.FriendRequestStatusUpdateRequest;
import molip.server.friend.dto.response.FriendItemResponse;
import molip.server.friend.dto.response.FriendRequestItemResponse;
import molip.server.friend.dto.response.FriendRequestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Friend", description = "친구 API")
@RestController
@RequestMapping
public class FriendController {
  @Operation(summary = "친구 요청 보내기")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "요청 성공",
        content = @Content(schema = @Schema(implementation = FriendRequestResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "자기 자신에게 요청 불가",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "대상 사용자 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "친구 요청 충돌",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping("/friend-requests/{targetUserId}")
  public ResponseEntity<ServerResponse<FriendRequestResponse>> sendFriendRequest(
      @PathVariable Long targetUserId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "친구 요청 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "페이지 정보 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/friend-requests")
  public ResponseEntity<ServerResponse<PageResponse<FriendRequestItemResponse>>> getFriendRequests(
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "친구 요청 삭제")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "삭제 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "친구 요청 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미 처리된 요청",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @DeleteMapping("/friend-requests/{requestId}")
  public ResponseEntity<Void> deleteFriendRequest(@PathVariable Long requestId) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "친구 요청 상태 변경")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "상태 변경 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "요청 상태 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "수락 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "친구 요청 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미 처리된 요청/친구 관계",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PatchMapping("/friend-requests/{requestId}")
  public ResponseEntity<Void> updateFriendRequestStatus(
      @PathVariable Long requestId, @RequestBody FriendRequestStatusUpdateRequest request) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "친구 삭제")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "친구 관계 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @DeleteMapping("/friends/{friendUserId}")
  public ResponseEntity<Void> deleteFriend(@PathVariable Long friendUserId) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "친구 목록 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "페이지 정보 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/friends")
  public ResponseEntity<ServerResponse<PageResponse<FriendItemResponse>>> getFriends(
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
