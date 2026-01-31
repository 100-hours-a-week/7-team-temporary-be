package molip.server.common.enums;

public enum NotificationTitle {
    SCHEDULE_CREATED("일정이 생성되었습니다."),
    SCHEDULE_REMINDER("5분 후에 일정이 시작됩니다.");

    private final String value;

    NotificationTitle(String value) {

        this.value = value;
    }

    public String getValue() {

        return value;
    }
}
