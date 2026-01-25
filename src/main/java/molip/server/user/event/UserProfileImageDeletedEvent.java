package molip.server.user.event;

import molip.server.common.enums.ImageType;

public record UserProfileImageDeletedEvent(ImageType imageType, String imageKey) {}
