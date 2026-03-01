package molip.server.socket.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

public record SocketEventRequest(String event, JsonNode payload) {}
