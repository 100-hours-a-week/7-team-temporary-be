package molip.server.chat.event;

import molip.server.chat.entity.ChatRoom;

public record ChatRoomDeletedEvent(ChatRoom chatRoom) {}
