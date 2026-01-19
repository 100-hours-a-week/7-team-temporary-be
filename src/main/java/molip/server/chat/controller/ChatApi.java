package molip.server.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.chat.dto.request.ChatRoomCreateRequest;
import molip.server.chat.dto.request.ChatRoomParticipantCameraUpdateRequest;
import molip.server.chat.dto.request.ChatRoomUpdateRequest;
import molip.server.chat.dto.request.UpdateLastReadMessageRequest;
import molip.server.chat.dto.response.ChatMessageItemResponse;
import molip.server.chat.dto.response.ChatRoomCreateResponse;
import molip.server.chat.dto.response.ChatRoomDetailResponse;
import molip.server.chat.dto.response.ChatRoomEnterResponse;
import molip.server.chat.dto.response.ChatRoomSearchItemResponse;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.response.CursorResponse;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Chat", description = "채팅 API")
public interface ChatApi {
  @Operation(summary = "채팅방 생성")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "생성 성공",
        content = @Content(schema = @Schema(implementation = ChatRoomCreateResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<ChatRoomCreateResponse>> createChatRoom(
      ChatRoomCreateRequest request);

  @Operation(summary = "채팅방 삭제")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "삭제 성공"),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "삭제 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "채팅방 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "이미 삭제된 채팅방",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<Void> deleteChatRoom(Long roomId);

  @Operation(summary = "채팅방 정보 수정")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "수정 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "수정 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "정원 축소 불가",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<Void> updateChatRoom(Long roomId, ChatRoomUpdateRequest request);

  @Operation(summary = "채팅방 세부 정보 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ChatRoomDetailResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "채팅방 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<ChatRoomDetailResponse>> getChatRoomDetail(Long roomId);

  @Operation(summary = "제목으로 채팅방 검색")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "검색 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "검색어 필요",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<PageResponse<ChatRoomSearchItemResponse>>> searchChatRooms(
      String title, int page, int size);

  @Operation(summary = "내 채팅방 타입에 따라 목록 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "채팅방 타입 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<PageResponse<ChatRoomSearchItemResponse>>> getMyChatRooms(
      ChatRoomType type, int page, int size);

  @Operation(summary = "채팅방 입장")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "입장 성공",
        content = @Content(schema = @Schema(implementation = ChatRoomEnterResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "채팅방 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "이미 참가 중/정원 초과",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<ChatRoomEnterResponse>> enterChatRoom(Long roomId);

  @Operation(summary = "메시지 목록 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = CursorResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "cursor 범위 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "채팅방 접근 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "채팅방 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<CursorResponse<ChatMessageItemResponse>>> getMessages(
      Long roomId, Long cursor, int size);

  @Operation(summary = "카메라 상태 변경")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "변경 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "변경 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "참가자 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "메시지 상태 충돌",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<Void> updateParticipantCamera(
      Long participantId, ChatRoomParticipantCameraUpdateRequest request);

  @Operation(summary = "채팅방 퇴장")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "퇴장 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "퇴장 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "참가자 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "이미 퇴장",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<Void> leaveChatRoom(Long roomId, Long participantId);

  @Operation(summary = "마지막으로 본 채팅 메시지 수정")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "마지막으로 본 메시지 수정 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "참가자 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "lastSeenMessage 감소 불가",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<Void> updateLastSeenMessage(
      Long participantId, UpdateLastReadMessageRequest request);
}
