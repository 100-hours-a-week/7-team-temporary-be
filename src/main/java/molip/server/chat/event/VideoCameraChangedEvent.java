package molip.server.chat.event;

import molip.server.chat.dto.response.VideoCameraChangedResponse;

public record VideoCameraChangedEvent(Long roomId, VideoCameraChangedResponse payload) {}
