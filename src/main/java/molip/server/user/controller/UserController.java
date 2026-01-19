package molip.server.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.response.ServerResponse;
import molip.server.common.response.PageResponse;
import molip.server.user.dto.request.SignUpRequest;
import molip.server.user.dto.request.UpdatePasswordRequest;
import molip.server.user.dto.request.UpdateProfileImageRequest;
import molip.server.user.dto.request.UpdateUserRequest;
import molip.server.user.dto.response.DuplicatedResponse;
import molip.server.user.dto.response.SignUpResponse;
import molip.server.user.dto.response.UserProfileResponse;
import molip.server.user.dto.response.UserSearchItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 API")
@RestController
@RequestMapping("/users")
public class UserController {
  @Operation(summary = "회원가입")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "회원가입 성공",
        content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이메일/닉네임 중복",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping
  public ResponseEntity<ServerResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "회원 상세 정보 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원 정보 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping
  public ResponseEntity<ServerResponse<UserProfileResponse>> getMe() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "닉네임 기반 회원 검색")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "검색 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "닉네임 검색어 필요",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping(params = "nickname")
  public ResponseEntity<ServerResponse<PageResponse<UserSearchItemResponse>>> searchByNickname(
      @RequestParam String nickname,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "이메일 중복 체크")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "중복 여부 확인",
        content = @Content(schema = @Schema(implementation = DuplicatedResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "이메일 필요",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping(params = "email")
  public ResponseEntity<ServerResponse<DuplicatedResponse>> checkEmail(
      @RequestParam String email) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "회원 정보 수정")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PatchMapping
  public ResponseEntity<Void> update(@RequestBody UpdateUserRequest request) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "프로필 이미지 수정")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미지 키 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PatchMapping("/image")
  public ResponseEntity<Void> updateProfileImage(@RequestBody UpdateProfileImageRequest request) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "비밀번호 수정")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "비밀번호 확인 불일치",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "비밀번호 길이 초과",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PatchMapping("/password")
  public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "회원 탈퇴")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "탈퇴 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원 정보 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @DeleteMapping
  public ResponseEntity<Void> delete() {
    return ResponseEntity.noContent().build();
  }
}
