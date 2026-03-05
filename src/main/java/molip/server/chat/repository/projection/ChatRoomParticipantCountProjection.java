package molip.server.chat.repository.projection;

public interface ChatRoomParticipantCountProjection {

    Long getChatRoomId();

    long getParticipantsCount();
}
