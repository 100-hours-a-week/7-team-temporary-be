package molip.server.chat.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.MessageImage;
import molip.server.chat.repository.MessageImageRepository;
import molip.server.image.entity.Image;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageImageService {

    private final MessageImageRepository messageImageRepository;

    @Transactional(readOnly = true)
    public Map<Long, List<MessageImage>> getMessageImagesByMessageIds(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return messageImageRepository
                .findAllByMessageIdInAndDeletedAtIsNullOrderBySortOrderAsc(messageIds)
                .stream()
                .collect(Collectors.groupingBy(messageImage -> messageImage.getMessage().getId()));
    }

    @Transactional
    public void createMessageImages(ChatMessage message, List<Image> images) {
        if (message == null || images == null || images.isEmpty()) {
            return;
        }

        List<MessageImage> messageImages = new ArrayList<>();

        for (int index = 0; index < images.size(); index++) {
            Image image = images.get(index);

            image.markSuccess();
            messageImages.add(new MessageImage(message, image, index + 1));
        }

        messageImageRepository.saveAll(messageImages);
    }
}
