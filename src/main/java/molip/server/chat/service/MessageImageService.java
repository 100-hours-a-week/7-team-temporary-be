package molip.server.chat.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.MessageImage;
import molip.server.chat.repository.MessageImageRepository;
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
}
