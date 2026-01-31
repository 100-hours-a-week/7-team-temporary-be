package molip.server.common;

public enum SuccessCode {
    LOGIN_SUCCESS("SUCCESS", "로그인을 성공했습니다."),
    TOKEN_REISSUE_SUCCESS("SUCCESS", "토큰이 성공적으로 재발급되었습니다."),
    SIGNUP_SUCCESS("SUCCESS", "회원가입이 완료되었습니다."),
    USER_PROFILE_FETCH_SUCCESS("SUCCESS", "회원 정보를 성공적으로 조회했습니다."),
    USER_SEARCH_SUCCESS("SUCCESS", "회원 검색 결과입니다."),
    EMAIL_DUPLICATION_CHECKED("SUCCESS", "이메일 중복여부가 성공적으로 조회되었습니다."),

    IMAGE_UPLOAD_URL_ISSUED("SUCCESS", "이미지 업로드 URL이 발급되었습니다."),
    IMAGE_GET_URL_ISSUED("SUCCESS", "이미지 조회 URL이 발급되었습니다."),

    FRIEND_REQUEST_SENT("SUCCESS", "친구 요청을 성공적으로 보냈습니다."),
    FRIEND_REQUEST_LIST_SUCCESS("SUCCESS", "친구 요청 목록입니다."),
    FRIEND_LIST_SUCCESS("SUCCESS", "친구 목록 조회 성공"),

    NOTIFICATION_LIST_SUCCESS("SUCCESS", "알림 목록 조회 성공"),

    TERMS_LIST_SUCCESS("SUCCESS", "활성화된 약관 목록이 모두 조회되었습니다."),
    TERMS_SIGN_CREATED("SUCCESS", "약관 동의 내역이 생성되었습니다."),
    TERMS_SIGN_LIST_SUCCESS("SUCCESS", "약관 동의 내역 조회 성공"),
    TERMS_SIGN_DETAIL_SUCCESS("SUCCESS", "약관 동의 상세 조회에 성공했습니다."),

    REPORT_WEEKLY_SUCCESS("SUCCESS", "주간 리포트를 성공적으로 조회했습니다."),
    REPORT_MESSAGE_LIST_SUCCESS("SUCCESS", "리포트 메시지 목록 조회 성공"),
    REPORT_MESSAGE_CREATED("SUCCESS", "메시지가 성공적으로 등록되었습니다."),

    CHAT_ROOM_CREATED("SUCCESS", "채팅방이 생성되었습니다."),
    CHAT_ROOM_DETAIL_SUCCESS("SUCCESS", "채팅방 상세 내역을 성공적으로 조회했습니다."),
    CHAT_ROOM_SEARCH_SUCCESS("SUCCESS", "채팅방 검색 결과입니다."),
    CHAT_ROOM_MY_LIST_SUCCESS("SUCCESS", "내가 속한 채팅방 검색 결과입니다."),
    CHAT_ROOM_ENTER_SUCCESS("SUCCESS", "채팅방 입장에 성공했습니다."),
    CHAT_MESSAGE_LIST_SUCCESS("SUCCESS", "채팅 메시지 목록입니다."),

    SCHEDULE_CREATED("SUCCESS", "일정이 성공적으로 생성되었습니다."),
    DAY_TODO_LIST_SUCCESS("SUCCESS", "금일 TodoList 조회를 완료했습니다."),
    DAY_SCHEDULE_LIST_SUCCESS("SUCCESS", "금일 시간이 지정된 일정 목록을 성공적으로 조회했습니다."),
    ARRANGEMENT_JOB_CREATED("SUCCESS", "AI 배치 작업이 생성되었습니다."),
    ARRANGEMENT_JOB_FETCH_SUCCESS("SUCCESS", "AI 배치 작업 조회 성공"),
    ARRANGEMENT_JOB_RUNNING_SUCCESS("SUCCESS", "AI 배치 작업을 성공적으로 조회했습니다."),
    ARRANGEMENT_JOB_COMPLETED("SUCCESS", "AI 배치가 완료되었습니다."),
    AI_ARRANGEMENT_COMPLETED("SUCCESS", "AI 배치가 완료되었습니다."),
    SCHEDULE_CHILDREN_CREATED("SUCCESS", "자식 일정이 생성되었습니다."),
    CURRENT_SCHEDULE_FETCH_SUCCESS("SUCCESS", "현재 일정 조회에 성공했습니다."),
    EXCLUDED_SCHEDULE_LIST_SUCCESS("SUCCESS", "제외된 일정 목록을 성공적으로 조회했습니다."),

    REFLECTION_CREATED("SUCCESS", "회고가 성공적으로 생성되었습니다."),
    REFLECTION_EXISTS("SUCCESS", "해당 날짜에 회고가 존재합니다."),
    REFLECTION_NOT_EXISTS("SUCCESS", "해당 날짜에 회고가 존재하지 않습니다."),
    MY_REFLECTION_LIST_SUCCESS("SUCCESS", "내 회고 목록을 성공적으로 조회했습니다."),
    OPEN_REFLECTION_LIST_SUCCESS("SUCCESS", "공개된 전체 유저의 회고 목록을 성공적으로 조회했습니다."),
    REFLECTION_DETAIL_SUCCESS("SUCCESS", "공유된 회고 상세 조회를 성공적으로 진행했습니다."),
    REFLECTION_LIKE_STATUS_SUCCESS("SUCCESS", "좋아요 여부를 성공적으로 조회했습니다.");

    private final String statusValue;
    private final String message;

    SuccessCode(String statusValue, String message) {
        this.statusValue = statusValue;
        this.message = message;
    }

    public String getStatusValue() {
        return statusValue;
    }

    public String getMessage() {
        return message;
    }
}
