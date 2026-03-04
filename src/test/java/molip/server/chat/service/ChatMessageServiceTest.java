package molip.server.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.repository.ChatMessageRepository;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock private ChatMessageRepository chatMessageRepository;

    @InjectMocks private ChatMessageService chatMessageService;

    @Test
    void 본인의_TEXT_메시지는_성공적으로_수정된다() {
        // given
        Long userId = 1L;
        Long roomId = 10L;
        Long messageId = 100L;
        ChatMessage message = createMessage(roomId, messageId, userId, MessageType.TEXT, "기존 내용");

        given(chatMessageRepository.findById(messageId)).willReturn(Optional.of(message));

        // when
        ChatMessage updated =
                chatMessageService.updateMessage(userId, roomId, messageId, "  수정 내용  ");

        // then
        assertThat(updated.getContent()).isEqualTo("수정 내용");
    }

    @Test
    void 다른_사람의_메시지를_수정하면_예외를_반환한다() {
        // given
        Long roomId = 10L;
        Long messageId = 100L;
        ChatMessage message = createMessage(roomId, messageId, 1L, MessageType.TEXT, "기존 내용");

        given(chatMessageRepository.findById(messageId)).willReturn(Optional.of(message));

        // when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> chatMessageService.updateMessage(2L, roomId, messageId, "수정"));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_MESSAGE_UPDATE);
    }

    @Test
    void IMAGE_메시지를_수정하면_예외를_반환한다() {
        // given
        Long userId = 1L;
        Long roomId = 10L;
        Long messageId = 100L;
        ChatMessage message = createMessage(roomId, messageId, userId, MessageType.IMAGE, null);

        given(chatMessageRepository.findById(messageId)).willReturn(Optional.of(message));

        // when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> chatMessageService.updateMessage(userId, roomId, messageId, "수정"));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST_MESSAGE_UPDATE);
    }

    @Test
    void 다른_채팅방의_메시지를_수정하면_예외를_반환한다() {
        // given
        Long userId = 1L;
        Long roomId = 10L;
        Long messageId = 100L;
        ChatMessage message = createMessage(20L, messageId, userId, MessageType.TEXT, "기존 내용");

        given(chatMessageRepository.findById(messageId)).willReturn(Optional.of(message));

        // when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> chatMessageService.updateMessage(userId, roomId, messageId, "수정"));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONFLICT_MESSAGE_NOT_IN_ROOM);
    }

    @Test
    void 이미_삭제된_메시지를_수정하면_예외를_반환한다() {
        // given
        Long userId = 1L;
        Long roomId = 10L;
        Long messageId = 100L;
        ChatMessage message = createMessage(roomId, messageId, userId, MessageType.TEXT, "기존 내용");
        ReflectionTestUtils.setField(message, "isDeleted", true);

        given(chatMessageRepository.findById(messageId)).willReturn(Optional.of(message));

        // when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> chatMessageService.updateMessage(userId, roomId, messageId, "수정"));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MESSAGE_NOT_FOUND);
    }

    private ChatMessage createMessage(
            Long roomId, Long messageId, Long senderId, MessageType messageType, String content) {
        ChatRoom chatRoom = new ChatRoom(99L, "제목", "설명", 10);
        ReflectionTestUtils.setField(chatRoom, "id", roomId);

        ChatMessage message =
                new ChatMessage(
                        chatRoom,
                        messageType,
                        content,
                        false,
                        LocalDateTime.now(),
                        SenderType.USER,
                        senderId);

        ReflectionTestUtils.setField(message, "id", messageId);
        return message;
    }
}
