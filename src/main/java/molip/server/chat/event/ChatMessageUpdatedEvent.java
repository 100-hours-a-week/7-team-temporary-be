package molip.server.chat.event;

import molip.server.chat.entity.ChatMessage;

public record ChatMessageUpdatedEvent(ChatMessage message) {}
