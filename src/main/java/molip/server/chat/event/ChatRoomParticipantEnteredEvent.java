package molip.server.chat.event;

import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.user.entity.Users;

public record ChatRoomParticipantEnteredEvent(
        ChatRoom chatRoom, ChatRoomParticipant participant, Users user) {}
