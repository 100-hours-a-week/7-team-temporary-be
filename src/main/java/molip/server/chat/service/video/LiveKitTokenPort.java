package molip.server.chat.service.video;

public interface LiveKitTokenPort {

    IssuedLiveKitToken issueToken(LiveKitTokenCommand command);
}
