package molip.server.migration.outbox;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED,
    DLQ
}
