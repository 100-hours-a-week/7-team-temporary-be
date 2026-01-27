package molip.server.chat.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class ChatController implements ChatApi {

    @PostMapping("/chat-rooms")
    @Override
    public ResponseEntity<ServerResponse<ChatRoomCreateResponse>> createChatRoom(
            @RequestBody ChatRoomCreateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @DeleteMapping("/chat-rooms/{roomId}")
    @Override
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long roomId) {
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/chat-rooms/{roomId}")
    @Override
    public ResponseEntity<Void> updateChatRoom(
            @PathVariable Long roomId, @RequestBody ChatRoomUpdateRequest request) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chat-rooms/{roomId}")
    @Override
    public ResponseEntity<ServerResponse<ChatRoomDetailResponse>> getChatRoomDetail(
            @PathVariable Long roomId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping(value = "/chat-rooms")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<ChatRoomSearchItemResponse>>> searchChatRooms(
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping(value = "/chat-rooms/participants")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<ChatRoomSearchItemResponse>>> getMyChatRooms(
            @RequestParam ChatRoomType type,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @PostMapping("/chat-rooms/{roomId}/participants")
    @Override
    public ResponseEntity<ServerResponse<ChatRoomEnterResponse>> enterChatRoom(
            @PathVariable Long roomId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping("/chat-rooms/{roomId}/message")
    @Override
    public ResponseEntity<ServerResponse<CursorResponse<ChatMessageItemResponse>>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "50") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

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
            @PathVariable Long roomId, @PathVariable Long participantId) {
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/chat-rooms/participants/{participantId}/message")
    @Override
    public ResponseEntity<Void> updateLastSeenMessage(
            Long participantId, UpdateLastReadMessageRequest request) {
        return null;
    }
}
