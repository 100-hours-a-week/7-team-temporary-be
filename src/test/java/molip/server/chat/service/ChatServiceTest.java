package molip.server.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.repository.ChatRoomRepository;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatRoomRepository chatRoomRepository;

    @InjectMocks private ChatService chatService;

    @Test
    void 채팅방_생성에_성공하면_OPEN_CHAT_타입으로_저장된다() {
        // given
        Long ownerId = 1L;
        String title = "삼전 적정가는 18만이다.";
        String description = "삼전이 18만원이 적정가인가에 대한 토론방";
        Integer maxParticipants = 10;

        ChatRoom savedChatRoom = new ChatRoom(ownerId, title, description, maxParticipants);
        ReflectionTestUtils.setField(savedChatRoom, "id", 101L);

        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(savedChatRoom);

        // when
        ChatRoom result = chatService.createChatRoom(ownerId, title, description, maxParticipants);

        // then
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getType()).isEqualTo(ChatRoomType.OPEN_CHAT);
    }

    @Test
    void 제목이_비어있으면_예외를_반환한다() {
        // given
        Long ownerId = 1L;
        String title = " ";
        String description = "설명";
        Integer maxParticipants = 10;

        // when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () ->
                                chatService.createChatRoom(
                                        ownerId, title, description, maxParticipants));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
    }

    @Test
    void 최대인원이_0이하면_예외를_반환한다() {
        // given
        Long ownerId = 1L;
        String title = "제목";
        String description = "설명";
        Integer maxParticipants = 0;

        // when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () ->
                                chatService.createChatRoom(
                                        ownerId, title, description, maxParticipants));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
    }

    @Test
    void 방장이_채팅방을_삭제하면_소프트삭제된다() {
        // given
        Long userId = 1L;
        Long roomId = 101L;
        ChatRoom chatRoom = new ChatRoom(userId, "제목", "설명", 10);
        ReflectionTestUtils.setField(chatRoom, "id", roomId);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        // when
        chatService.deleteChatRoom(userId, roomId);

        // then
        assertThat(chatRoom.getDeletedAt()).isNotNull();
    }

    @Test
    void 채팅방이_없으면_예외를_반환한다() {
        // given
        Long userId = 1L;
        Long roomId = 101L;

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.empty());

        // when
        BaseException exception =
                assertThrows(BaseException.class, () -> chatService.deleteChatRoom(userId, roomId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_ROOM);
    }

    @Test
    void 이미_삭제된_채팅방이면_예외를_반환한다() {
        // given
        Long userId = 1L;
        Long roomId = 101L;
        ChatRoom chatRoom = new ChatRoom(userId, "제목", "설명", 10);
        ReflectionTestUtils.setField(chatRoom, "id", roomId);
        ReflectionTestUtils.setField(chatRoom, "deletedAt", LocalDateTime.now());

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        // when
        BaseException exception =
                assertThrows(BaseException.class, () -> chatService.deleteChatRoom(userId, roomId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONFLICT_ROOM_ALREADY_DELETED);
    }

    @Test
    void 방장이_아니면_예외를_반환한다() {
        // given
        Long userId = 2L;
        Long roomId = 101L;
        ChatRoom chatRoom = new ChatRoom(1L, "제목", "설명", 10);
        ReflectionTestUtils.setField(chatRoom, "id", roomId);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        // when
        BaseException exception =
                assertThrows(BaseException.class, () -> chatService.deleteChatRoom(userId, roomId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ROOM_DELETE);
    }
}
