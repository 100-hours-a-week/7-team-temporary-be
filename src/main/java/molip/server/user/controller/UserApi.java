package molip.server.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.user.dto.request.SignUpRequest;
import molip.server.user.dto.request.UpdatePasswordRequest;
import molip.server.user.dto.request.UpdateProfileImageRequest;
import molip.server.user.dto.request.UpdateUserRequest;
import molip.server.user.dto.response.DuplicatedResponse;
import molip.server.user.dto.response.SignUpResponse;
import molip.server.user.dto.response.UserProfileResponse;
import molip.server.user.dto.response.UserSearchItemResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;

@Tag(name = "User", description = "유저 API")
public interface UserApi {

    @Operation(summary = "회원가입", description = "회원가입 성공 시 refreshToken, deviceId는 쿠키로 전달됩니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "회원가입 성공",
                content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "필수 값 누락",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "이메일/닉네임 중복",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<SignUpResponse>> signUp(
            SignUpRequest request,
            @CookieValue(name = "deviceId", required = false) String deviceId);

    @Operation(summary = "회원 상세 정보 조회")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "회원 정보 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<UserProfileResponse>> getMe();

    @Operation(summary = "닉네임 기반 회원 검색")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "검색 성공",
                content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "닉네임 검색어 필요",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<PageResponse<UserSearchItemResponse>>> searchByNickname(
            String nickname, int page, int size);

    @Operation(summary = "이메일 중복 체크")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "중복 여부 확인",
                content = @Content(schema = @Schema(implementation = DuplicatedResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "이메일 필요",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<DuplicatedResponse>> checkEmail(String email);

    @Operation(summary = "회원 정보 수정")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "수정 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "필수 값 누락",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> update(UpdateUserRequest request);

    @Operation(summary = "프로필 이미지 수정")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "수정 성공"),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "이미지 키 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> updateProfileImage(UpdateProfileImageRequest request);

    @Operation(summary = "비밀번호 수정")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "수정 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "비밀번호 확인 불일치",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "422",
                description = "비밀번호 길이 초과",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> updatePassword(UpdatePasswordRequest request);

    @Operation(summary = "회원 탈퇴")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "회원 정보 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> delete();
}
