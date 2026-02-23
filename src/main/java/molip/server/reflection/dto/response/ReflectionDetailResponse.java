package molip.server.reflection.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import molip.server.common.response.ImageInfoResponse;

@Schema(description = "회고 상세")
public record ReflectionDetailResponse(
        @Schema(description = "요청 유저 소유 여부", example = "true") boolean isMine,
        @Schema(description = "요청 유저 좋아요 여부", example = "false") boolean isLikedByMe,
        @Schema(description = "작성자 닉네임", example = "모립") String ownerNickname,
        @Schema(description = "사용자 ID", example = "65") Long userId,
        @Schema(description = "회고 ID", example = "55") Long reflectionId,
        @Schema(description = "공개 여부", example = "true") boolean isOpen,
        @Schema(description = "제목", example = "2026.01.13(수)") String title,
        @Schema(description = "내용", example = "오늘 집중 잘 됐다") String content,
        @Schema(description = "좋아요 수", example = "37") int likes,
        @Schema(description = "이미지 목록") List<ImageInfoResponse> images,
        @Schema(description = "작성 시각", example = "2026-01-13T23:10:00+09:00")
                OffsetDateTime createdAt) {

    public static ReflectionDetailResponse of(
            boolean isMine,
            boolean isLikedByMe,
            String ownerNickname,
            Long userId,
            Long reflectionId,
            boolean isOpen,
            String title,
            String content,
            int likes,
            List<ImageInfoResponse> images,
            OffsetDateTime createdAt) {
        return new ReflectionDetailResponse(
                isMine,
                isLikedByMe,
                ownerNickname,
                userId,
                reflectionId,
                isOpen,
                title,
                content,
                likes,
                images,
                createdAt);
    }
}
