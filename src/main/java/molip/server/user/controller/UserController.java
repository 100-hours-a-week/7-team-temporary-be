package molip.server.user.controller;

import lombok.RequiredArgsConstructor;
import molip.server.common.SuccessCode;
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
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;

  @PostMapping("/users")
  @Override
  public ResponseEntity<ServerResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest request) {
    Users user =
        userService.registerUser(
            request.email(),
            request.password(),
            request.nickname(),
            request.gender(),
            request.birth(),
            request.focusTimeZone(),
            request.dayEndTime());

    return ResponseEntity.ok(
        ServerResponse.success(SuccessCode.SIGNUP_SUCCESS, SignUpResponse.from(user.getId())));
  }

  @GetMapping("/users")
  @Override
  public ResponseEntity<ServerResponse<UserProfileResponse>> getMe() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping(value = "/users", params = "nickname")
  @Override
  public ResponseEntity<ServerResponse<PageResponse<UserSearchItemResponse>>> searchByNickname(
      @RequestParam String nickname,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping(value = "/users", params = "email")
  @Override
  public ResponseEntity<ServerResponse<DuplicatedResponse>> checkEmail(@RequestParam String email) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PatchMapping("/users")
  @Override
  public ResponseEntity<Void> update(@RequestBody UpdateUserRequest request) {
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/users/image")
  @Override
  public ResponseEntity<Void> updateProfileImage(@RequestBody UpdateProfileImageRequest request) {
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/users/password")
  @Override
  public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request) {
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/users")
  @Override
  public ResponseEntity<Void> delete() {
    return ResponseEntity.noContent().build();
  }
}
