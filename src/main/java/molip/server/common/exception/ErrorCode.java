package molip.server.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // Common
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "일시적으로 접속이 원활하지 않습니다. 서버 팀에 문의 부탁드립니다."),
    INVALID_REQUEST_MISSING_REQUIRED(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "필수 값이 누락되었습니다. 확인 부탁드립니다."),
    INVALID_REQUEST_INVALID_PAGE(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "페이지 정보가 올바르지 않습니다."),

    // Auth
    UNAUTHORIZED_INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "아이디나 비밀번호를 다시 확인해주세요."),
    UNAUTHORIZED_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "유효하지 않은 토큰입니다."),
    INVALID_REQUEST_REFRESH_MISSING(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "리프레시 토큰이 누락되었습니다."),

    // User
    EMAIL_CONFLICT(HttpStatus.CONFLICT, "EMAIL_CONFLICT", "이미 사용 중인 이메일입니다."),
    NICKNAME_CONFLICT(HttpStatus.CONFLICT, "NICKNAME_CONFLICT", "이미 사용 중인 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "회원 정보를 찾을 수 없습니다."),
    INVALID_REQUEST_NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "닉네임 검색어가 필요합니다."),
    INVALID_REQUEST_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "이메일이 필요합니다."),
    INVALID_REQUEST_EMAIL_POLICY(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "이메일 형식이 올바르지 않습니다."),
    CONFLICT_INVALID_IMAGE_KEY(HttpStatus.CONFLICT, "CONFLICT", "잘못된 이미지 키 입니다. 다시 입력해주세요"),
    INVALID_REQUEST_PASSWORD_MISMATCH(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "비밀번호 확인 값이 일치하지 않습니다."),
    INVALID_REQUEST_PASSWORD_TOO_LONG(
            HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_REQUEST", "비밀번호 길이가 최대 길이를 초과했습니다."),
    INVALID_REQUEST_PASSWORD_POLICY(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "비밀번호 형식이 올바르지 않습니다."),

    // Image
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE_NOT_FOUND", "이미지를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR_IMAGE_UPLOAD(
            HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "이미지 업로드 URL 발급에 실패했습니다."),
    INTERNAL_SERVER_ERROR_IMAGE_GET(
            HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "이미지 조회 URL 발급에 실패했습니다."),

    // Friend
    INVALID_REQUEST_SELF_FRIEND(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "자기 자신에게 친구 요청을 보낼 수 없습니다."),
    USER_NOT_FOUND_TARGET(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "대상 사용자를 찾을 수 없습니다."),
    CONFLICT_ALREADY_REQUESTED(HttpStatus.CONFLICT, "CONFLICT", "이미 친구 요청을 보낸 상태입니다."),
    CONFLICT_RECEIVED_ALREADY(
            HttpStatus.CONFLICT, "CONFLICT", "상대가 이미 친구 요청을 보냈습니다. 받은 요청 목록에서 수락해주세요."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND", "해당 친구 요청을 찾을 수 없습니다."),
    FORBIDDEN_DELETE_REQUEST(HttpStatus.FORBIDDEN, "FORBIDDEN", "해당 요청을 삭제할 권한이 없습니다."),
    CONFLICT_ALREADY_HANDLED_REQUEST(HttpStatus.CONFLICT, "CONFLICT", "이미 처리된 친구 요청입니다."),
    INVALID_REQUEST_STATUS_VALUE(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 상태 값이 올바르지 않습니다."),
    FORBIDDEN_ACCEPT_REQUEST(HttpStatus.FORBIDDEN, "FORBIDDEN", "해당 요청을 수락할 권한이 없습니다."),
    NOT_FOUND_REQUEST(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 친구 요청을 찾을 수 없습니다."),
    CONFLICT_ALREADY_FRIEND(HttpStatus.CONFLICT, "CONFLICT", "이미 친구 관계입니다."),
    REQUEST_NOT_FOUND_FRIEND(HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND", "친구 관계를 찾을 수 없습니다."),

    // Notification
    INVALID_REQUEST_NOTIFICATION_PAGE(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "페이지 정보가 올바르지 않습니다."),

    // Terms
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS_NOT_FOUND", "존재하지않는 약관에 대한 요청입니다."),
    TERMS_SIGN_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS_SIGN_NOT_FOUND", "약관 동의 내역을 찾을 수 없습니다."),
    CONFLICT_TERMS_SIGN_EXISTS(HttpStatus.CONFLICT, "CONFLICT", "이미 동의 내역이 존재합니다."),
    CONFLICT_TERMS_WITHDRAW_NOT_ALLOWED(HttpStatus.CONFLICT, "CONFLICT", "필수 약관 동의는 철회할 수 없습니다."),
    FORBIDDEN_TERMS_SIGN_ACCESS(HttpStatus.FORBIDDEN, "FORBIDDEN", "다른 사람의 약관 동의 내역은 조회할 수 없습니다."),

    // Report
    INVALID_REQUEST_DATE_REQUIRED(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "날짜가 누락되었습니다. 확인 부탁드립니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "해당 기간의 리포트를 찾을 수 없습니다."),
    FORBIDDEN_REPORT_ACCESS(HttpStatus.FORBIDDEN, "FORBIDDEN", "해당 리포트에 접근할 권한이 없습니다."),
    REPORT_NOT_FOUND_GENERIC(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "리포트를 찾을 수 없습니다."),
    INVALID_REQUEST_INPUT_MESSAGE_REQUIRED(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "inputMessage 값이 필요합니다."),
    CONFLICT_REPORT_RESPONSE_RUNNING(HttpStatus.CONFLICT, "CONFLICT", "이미 응답 생성이 진행 중입니다."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND", "해당 메시지를 찾을 수 없습니다."),
    CONFLICT_STREAM_ENDED(HttpStatus.CONFLICT, "CONFLICT", "이미 종료된 스트림입니다."),
    CONFLICT_RESPONSE_ALREADY_ENDED(HttpStatus.CONFLICT, "CONFLICT", "이미 종료된 응답 생성입니다."),

    // Chat
    NOT_FOUND_ROOM(HttpStatus.NOT_FOUND, "NOT_FOUND", "채팅방을 찾을 수 없습니다."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
    FORBIDDEN_ROOM_DELETE(HttpStatus.FORBIDDEN, "FORBIDDEN", "방장만 채팅방을 삭제할 수 있습니다."),
    CONFLICT_ROOM_ALREADY_DELETED(HttpStatus.CONFLICT, "CONFLICT", "이미 삭제된 채팅방입니다."),
    FORBIDDEN_ROOM_UPDATE(HttpStatus.FORBIDDEN, "FORBIDDEN", "채팅방을 수정할 권한이 없습니다."),
    CONFLICT_ROOM_CAPACITY(HttpStatus.CONFLICT, "CONFLICT", "현재 참여자 수보다 최대 인원을 낮출 수 없습니다."),
    INVALID_REQUEST_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "검색어가 필요합니다."),
    INVALID_REQUEST_CHAT_TYPE(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "채팅방 타입이 올바르지 않습니다."),
    CONFLICT_ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "CONFLICT", "이미 채팅방에 참가 중입니다."),
    CONFLICT_ROOM_FULL(HttpStatus.CONFLICT, "CONFLICT", "채팅방 정원이 가득 찼습니다."),
    INVALID_REQUEST_CURSOR_RANGE(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "cursor가 범위를 벗어났습니다."),
    FORBIDDEN_CHAT_ACCESS(HttpStatus.FORBIDDEN, "FORBIDDEN", "채팅방에 속한 사용자만 메시지를 조회할 수  있습니다."),
    INVALID_REQUEST_CAMERA_REQUIRED(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "카메라 허용 여부가 누락되었습니다. 확인 부탁드립니다."),
    FORBIDDEN_CAMERA_UPDATE(HttpStatus.FORBIDDEN, "FORBIDDEN", "자신의 카메라 상태만 변경할 수 있습니다."),
    INVALID_REQUEST_REQUIRED_VALUES(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "필수 값이 누락되었습니다. 확인 부탁드립니다."),
    FORBIDDEN_PARTICIPANT_REMOVE(
            HttpStatus.FORBIDDEN, "FORBIDDEN", "본인이 아닌 대상은 채팅방 퇴장시키기가 불가능합니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT_NOT_FOUND", "참가자 정보를 찾을 수 없습니다."),
    CONFLICT_ALREADY_LEFT(HttpStatus.CONFLICT, "CONFLICT", "이미 퇴장한 사용자입니다."),
    CONFLICT_MESSAGE_NOT_IN_ROOM(HttpStatus.CONFLICT, "CONFLICT", "해당 채팅방에 존재하지 않는 메시지입니다."),
    CONFLICT_LAST_SEEN_DECREASE(HttpStatus.CONFLICT, "CONFLICT", "lastSeenMessageId는 감소시킬 수 없습니다."),

    // Schedule
    CONFLICT_TIME_OVERLAP(HttpStatus.CONFLICT, "CONFLICT", "해당 시간대에 이미 일정이 존재합니다."),
    DAYPLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "DAYPLAN_NOT_FOUND", "해당 일자 플랜을 찾을 수 없습니다."),
    FORBIDDEN_SCHEDULE_UPDATE(HttpStatus.FORBIDDEN, "FORBIDDEN", "해당 일정을 수정할 권한이 없습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "해당 일정을 찾을 수 없습니다."),
    CONFLICT_SCHEDULE_ALREADY_DELETED(HttpStatus.CONFLICT, "CONFLICT", "이미 삭제된 일정입니다."),
    DAYPLAN_NOT_FOUND_GENERIC(HttpStatus.NOT_FOUND, "DAYPLAN_NOT_FOUND", "DayPlan을 찾을 수 없습니다."),
    CONFLICT_ARRANGEMENT_RUNNING(HttpStatus.CONFLICT, "CONFLICT", "이미 작업이 진행중입니다."),
    GONE_ARRANGEMENT_EXPIRED(HttpStatus.GONE, "GONE", "작업 결과 조회 기간이 만료되었습니다."),
    INVALID_REQUEST_CHILDREN_MIN(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "자식은 적어도 2개 이상이여야합니다. titles를 2개 이상 입력해주세요"),
    INVALID_REQUEST_FIXED_SCHEDULE_SPLIT(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "고정 일정은 분할할 수 없습니다."),
    INVALID_REQUEST_MANDATORY_TERMS_REQUIRED(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "필수 약관에 동의해야 합니다."),
    FORBIDDEN_OWN_SCHEDULE_ONLY(HttpStatus.FORBIDDEN, "FORBIDDEN", "본인의 일정만 수정이 가능합니다."),
    SCHEDULE_NOT_FOUND_PARENT(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "부모 일정을 찾을 수 없습니다."),
    CONFLICT_CHILDREN_ALREADY_EXISTS(HttpStatus.CONFLICT, "CONFLICT", "이미 자식 일정이 존재하는 부모 일정입니다."),
    INVALID_REQUEST_STATUS_CHECK(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 상태 값을 똑바로 입력했는지 확인해주세요"),
    INVALID_REQUEST_INVALID_TIME_FORMAT(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "시간 형식이 올바르지 않습니다."),
    INVALID_REQUEST_INVALID_TIME_RANGE(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "종료 시간이 시작 시간보다 빨라질 수 없습니다."),

    // Reflection
    INVALID_REQUEST_REFLECTION_IMAGES(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "이미지는 1개 이상 첨부되어야합니다."),
    FORBIDDEN_REFLECTION_ONLY_OWN(HttpStatus.FORBIDDEN, "FORBIDDEN", "본인의 일정만 수정할 수 있습니다."),
    DAYPLAN_NOT_FOUND_REFLECTION(HttpStatus.NOT_FOUND, "DAYPLAN_NOT_FOUND", "일자 정보를 찾을 수 없습니다."),
    CONFLICT_REFLECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "CONFLICT", "이미 회고가 작성된 일정입니다."),
    REFLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "REFLECTION_NOT_FOUND", "회고를 찾을 수 없습니다."),
    INVALID_REQUEST_REFLECTION_IMAGES_MIN(
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "이미지가 1개 이상 포함되어야 합니다."),
    FORBIDDEN_REFLECTION_UPDATE(HttpStatus.FORBIDDEN, "FORBIDDEN", "본인의 회고만 수정할 수 있습니다."),
    CONFLICT_ALREADY_LIKED(HttpStatus.CONFLICT, "CONFLICT", "이미 좋아요를 누른 회고입니다.");

    private final HttpStatus status;
    private final String statusValue;
    private final String message;

    ErrorCode(HttpStatus status, String statusValue, String message) {
        this.status = status;
        this.statusValue = statusValue;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getStatusValue() {
        return statusValue;
    }
}
