package molip.server.report.redis;

public record ReportChatStreamState(
        Long inputMessageId,
        Long streamMessageId,
        Long lastSequence,
        String status,
        String content) {

    public static ReportChatStreamState initial(Long inputMessageId, Long streamMessageId) {
        return new ReportChatStreamState(inputMessageId, streamMessageId, 0L, "GENERATING", "");
    }

    public ReportChatStreamState withChunk(Long sequence, String delta) {
        String safeDelta = delta == null ? "" : delta;
        String nextContent = (content == null ? "" : content) + safeDelta;

        return new ReportChatStreamState(
                inputMessageId, streamMessageId, sequence, status, nextContent);
    }

    public ReportChatStreamState withStatus(String nextStatus) {
        return new ReportChatStreamState(
                inputMessageId, streamMessageId, lastSequence, nextStatus, content);
    }
}
