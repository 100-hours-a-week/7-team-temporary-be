package molip.server.reflection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.reflection.dto.request.ReflectionCreateRequest;
import molip.server.reflection.dto.request.ReflectionOpenUpdateRequest;
import molip.server.reflection.dto.request.ReflectionUpdateRequest;
import molip.server.reflection.dto.response.ReflectionCreateResponse;
import molip.server.reflection.dto.response.ReflectionDetailResponse;
import molip.server.reflection.dto.response.ReflectionExistResponse;
import molip.server.reflection.dto.response.ReflectionLikeResponse;
import molip.server.reflection.dto.response.ReflectionListItemResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Reflection", description = "회고 API")
public interface ReflectionApi {

    @Operation(summary = "회고 생성")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content =
                        @Content(
                                schema = @Schema(implementation = ReflectionCreateResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "이미지 조건 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "수정 권한 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "일자 플랜 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "이미 회고 작성됨",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<ReflectionCreateResponse>> createReflection(
            @AuthenticationPrincipal UserDetails userDetails,
            Long dayPlanId,
            ReflectionCreateRequest request);

    @Operation(summary = "특정 일자 회고 작성 여부 조회")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(schema = @Schema(implementation = ReflectionExistResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "일자 플랜 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<ReflectionExistResponse>> existsReflection(
            @AuthenticationPrincipal UserDetails userDetails, Long dayPlanId);

    @Operation(summary = "로그인한 유저의 개인 회고 조회")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "페이지 정보 오류 또는 공개 회고만 조회 가능",
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
    @Deprecated
    ResponseEntity<ServerResponse<PageResponse<ReflectionListItemResponse>>> getMyReflections(
            @AuthenticationPrincipal UserDetails userDetails, int page, int size);

    @Operation(summary = "공개된 전체 회고 조회")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "페이지 정보 오류 또는 공개 회고만 조회 가능",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<PageResponse<ReflectionListItemResponse>>> getOpenReflections(
            @AuthenticationPrincipal UserDetails userDetails, boolean isOpen, int page, int size);

    @Operation(summary = "비로그인 유저 회고 상세 조회")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                schema = @Schema(implementation = ReflectionDetailResponse.class),
                                examples =
                                        @ExampleObject(
                                                value =
                                                        "{\n"
                                                                + "  \"status\": \"SUCCESS\",\n"
                                                                + "  \"message\": \"공유된 회고 상세 조회를 성공적으로 진행했습니다.\",\n"
                                                                + "  \"data\": {\n"
                                                                + "    \"isMine\": true,\n"
                                                                + "    \"isLikedByMe\": false,\n"
                                                                + "    \"ownerNickname\": \"모립\",\n"
                                                                + "    \"userId\": 65,\n"
                                                                + "    \"reflectionId\": 55,\n"
                                                                + "    \"isOpen\": true,\n"
                                                                + "    \"title\": \"2026.01.13(수)\",\n"
                                                                + "    \"content\": \"오늘 집중 잘 됐다\",\n"
                                                                + "    \"likes\": 37,\n"
                                                                + "    \"images\": [\n"
                                                                + "      {\n"
                                                                + "        \"url\": \"https://...presigned...\",\n"
                                                                + "        \"expiresAt\": \"2026-01-13T11:10:00+09:00\",\n"
                                                                + "        \"key\": \"asifdhs124uishadfiaufh\"\n"
                                                                + "      }\n"
                                                                + "    ],\n"
                                                                + "    \"createdAt\": \"2026-01-13T23:10:00+09:00\"\n"
                                                                + "  }\n"
                                                                + "}"))),
        @ApiResponse(
                responseCode = "404",
                description = "회고 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<ReflectionDetailResponse>> getReflectionDetail(
            @AuthenticationPrincipal UserDetails userDetails, Long reflectionId);

    @Operation(summary = "회고 공개 여부 수정")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "수정 성공"),
        @ApiResponse(
                responseCode = "403",
                description = "본인 회고 아님",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "회고 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> updateOpen(
            @AuthenticationPrincipal UserDetails userDetails,
            Long reflectionId,
            ReflectionOpenUpdateRequest request);

    @Operation(summary = "회고 수정")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "수정 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "이미지 조건 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "수정 권한 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "회고 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> updateReflection(
            @AuthenticationPrincipal UserDetails userDetails,
            Long reflectionId,
            ReflectionUpdateRequest request);

    @Operation(summary = "회고 삭제")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(
                responseCode = "403",
                description = "본인 회고 아님",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "이미 삭제됨",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> deleteReflection(
            @AuthenticationPrincipal UserDetails userDetails, Long reflectionId);

    @Operation(summary = "회고 좋아요 생성")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "생성 성공"),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "회고 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "이미 좋아요 누름",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    @Deprecated
    ResponseEntity<Void> likeReflection(
            @AuthenticationPrincipal UserDetails userDetails, Long reflectionId);

    @Operation(summary = "회고 좋아요 삭제")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "회고 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "좋아요 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> unlikeReflection(
            @AuthenticationPrincipal UserDetails userDetails, Long reflectionId);

    @Operation(summary = "회고 좋아요 여부 조회")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(schema = @Schema(implementation = ReflectionLikeResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "회고 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    @Deprecated
    ResponseEntity<ServerResponse<ReflectionLikeResponse>> getLikeStatus(Long reflectionId);
}
