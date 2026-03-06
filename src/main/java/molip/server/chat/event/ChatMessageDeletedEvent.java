package molip.server.chat.event;

import molip.server.chat.entity.ChatMessage;

public record ChatMessageDeletedEvent(ChatMessage message) {}
