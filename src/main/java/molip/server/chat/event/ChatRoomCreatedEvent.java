package molip.server.chat.event;

import molip.server.chat.entity.ChatRoom;

public record ChatRoomCreatedEvent(ChatRoom chatRoom, Long ownerId) {}
