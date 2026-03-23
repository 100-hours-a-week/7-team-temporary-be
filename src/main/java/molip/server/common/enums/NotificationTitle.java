package molip.server.common.enums;

public enum NotificationTitle {
    SCHEDULE_CREATED("일정이 생성되었습니다."),
    SCHEDULE_REMINDER("5분 후에 일정이 시작됩니다."),
    FRIEND_REQUESTED("친구 요청이 도착했습니다."),
    FRIEND_CREATED("친구 관계가 생성되었습니다."),
    POST_LIKED("회고 좋아요 알림"),
    REPORT_CREATED("주간 리포트가 생성되었습니다."),
    CHAT_MESSAGE("새로운 메시지가 도착했습니다.");

    private final String value;

    NotificationTitle(String value) {

        this.value = value;
    }

    public String getValue() {

        return value;
    }
}
