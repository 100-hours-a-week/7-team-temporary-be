package molip.server.chat.controller;

import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.request.ChatMessageSendRequest;
import molip.server.chat.dto.request.ChatMessageUpdateRequest;
import molip.server.chat.dto.request.ChatRoomCreateRequest;
import molip.server.chat.dto.request.ChatRoomParticipantCameraUpdateRequest;
import molip.server.chat.dto.request.ChatRoomUpdateRequest;
import molip.server.chat.dto.request.UpdateLastReadMessageRequest;
import molip.server.chat.dto.response.ChatMessageItemResponse;
import molip.server.chat.dto.response.ChatMessageSendCommandResult;
import molip.server.chat.dto.response.ChatMessageSendResponse;
import molip.server.chat.dto.response.ChatMyRoomItemResponse;
import molip.server.chat.dto.response.ChatRoomCreateResponse;
import molip.server.chat.dto.response.ChatRoomDetailResponse;
import molip.server.chat.dto.response.ChatRoomEnterResponse;
import molip.server.chat.dto.response.ChatRoomOwnerCheckResponse;
import molip.server.chat.dto.response.ChatRoomSearchItemResponse;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.facade.ChatRoomCommandFacade;
import molip.server.chat.facade.ChatRoomQueryFacade;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.common.SuccessCode;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.response.CursorResponse;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController implements ChatApi {

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatRoomCommandFacade chatRoomCommandFacade;
    private final ChatRoomQueryFacade chatRoomQueryFacade;

    @PostMapping("/chat-rooms")
    @Override
    public ResponseEntity<ServerResponse<ChatRoomCreateResponse>> createChatRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChatRoomCreateRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        ChatRoom chatRoom =
                chatRoomService.createChatRoom(
                        userId, request.title(), request.description(), request.maxParticipants());

        return ResponseEntity.ok(
                ServerResponse.success(
                        SuccessCode.CHAT_ROOM_CREATED,
                        ChatRoomCreateResponse.from(chatRoom.getId())));
    }

    @DeleteMapping("/chat-rooms/{roomId}")
    @Override
    public ResponseEntity<Void> deleteChatRoom(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long roomId) {
        Long userId = Long.valueOf(userDetails.getUsername());

        chatRoomService.deleteChatRoom(userId, roomId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/chat-rooms/{roomId}")
    @Override
    public ResponseEntity<Void> updateChatRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody ChatRoomUpdateRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        chatRoomService.updateChatRoom(
                userId, roomId, request.title(), request.description(), request.maxParticipants());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chat-rooms/{roomId}")
    @Override
    public ResponseEntity<ServerResponse<ChatRoomDetailResponse>> getChatRoomDetail(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long roomId) {

        ChatRoomDetailResponse response = chatRoomQueryFacade.getChatRoomDetail(roomId);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.CHAT_ROOM_DETAIL_SUCCESS, response));
    }

    @GetMapping("/chat-rooms/{roomId}/owner/{ownerId}")
    @Override
    public ResponseEntity<ServerResponse<ChatRoomOwnerCheckResponse>> checkOwner(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @PathVariable Long ownerId) {

        ChatRoomOwnerCheckResponse response = chatRoomQueryFacade.checkOwner(roomId, ownerId);

        String message = response.isOwner() ? "해당 사용자는 방장입니다." : "해당 사용자는 방장이 아닙니다.";

        return ResponseEntity.ok(new ServerResponse<>("SUCCESS", message, response));
    }

    @GetMapping("/chat-rooms")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<ChatRoomSearchItemResponse>>> searchChatRooms(
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        PageResponse<ChatRoomSearchItemResponse> response =
                chatRoomQueryFacade.searchChatRooms(title, page, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.CHAT_ROOM_SEARCH_SUCCESS, response));
    }

    @GetMapping("/chat-rooms/participants")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<ChatMyRoomItemResponse>>> getMyChatRooms(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam ChatRoomType type,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Long userId = Long.valueOf(userDetails.getUsername());

        PageResponse<ChatMyRoomItemResponse> response =
                chatRoomQueryFacade.getMyChatRooms(userId, type, page, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.CHAT_ROOM_SEARCH_SUCCESS, response));
    }

    @PostMapping("/chat-rooms/{roomId}/participants")
    @Override
    public ResponseEntity<ServerResponse<ChatRoomEnterResponse>> enterChatRoom(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long roomId) {
        Long userId = Long.valueOf(userDetails.getUsername());

        ChatRoomEnterResponse response = chatRoomCommandFacade.enterChatRoom(userId, roomId);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.CHAT_ROOM_ENTER_SUCCESS, response));
    }

    @GetMapping("/chat-rooms/{roomId}/message")
    @Override
    public ResponseEntity<ServerResponse<CursorResponse<ChatMessageItemResponse>>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "50") int size) {
        Long userId = Long.valueOf(userDetails.getUsername());

        CursorResponse<ChatMessageItemResponse> response =
                chatRoomQueryFacade.getMessages(userId, roomId, cursor, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.CHAT_MESSAGE_LIST_SUCCESS, response));
    }

    @Deprecated
    @PatchMapping("/chat-rooms/participants/{participantId}")
    @Override
    public ResponseEntity<Void> updateParticipantCamera(
            @PathVariable Long participantId,
            @RequestBody ChatRoomParticipantCameraUpdateRequest request) {
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/chat-rooms/{roomId}/participants/{participantId}")
    @Override
    public ResponseEntity<Void> leaveChatRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @PathVariable Long participantId) {
        Long userId = Long.valueOf(userDetails.getUsername());

        chatRoomParticipantService.leaveChatRoom(userId, roomId, participantId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/chat-rooms/participants/{participantId}/message")
    @Override
    public ResponseEntity<Void> updateLastSeenMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long participantId,
            @RequestBody UpdateLastReadMessageRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        chatRoomCommandFacade.updateLastSeenMessage(userId, participantId, request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chat-rooms/{roomId}/messages")
    @Override
    public ResponseEntity<ServerResponse<ChatMessageSendResponse>> sendMessageFallback(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody ChatMessageSendRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        ChatMessageSendCommandResult result =
                chatRoomCommandFacade.sendMessageFallback(userId, roomId, request);

        return ResponseEntity.status(result.httpStatus()).body(result.body());
    }

    @Deprecated
    @DeleteMapping("/chat-rooms/{roomId}/messages/{messageId}")
    @Override
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @PathVariable Long messageId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Deprecated
    @PatchMapping("/chat-rooms/{roomId}/messages/{messageId}")
    @Override
    public ResponseEntity<Void> updateMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @PathVariable Long messageId,
            @RequestBody ChatMessageUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
