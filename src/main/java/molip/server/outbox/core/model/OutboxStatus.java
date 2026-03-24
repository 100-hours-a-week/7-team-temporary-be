package molip.server.outbox.core.model;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED,
    DLQ
}
