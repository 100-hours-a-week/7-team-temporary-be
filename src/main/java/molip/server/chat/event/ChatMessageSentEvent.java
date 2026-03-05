package molip.server.chat.event;

import java.util.List;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoom;
import molip.server.image.entity.Image;

public record ChatMessageSentEvent(
        ChatRoom chatRoom, ChatMessage message, List<Image> images, Long senderUserId) {}
