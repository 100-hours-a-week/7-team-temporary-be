package molip.server.reflection.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import molip.server.reflection.entity.DayReflectionImage;
import molip.server.reflection.repository.DayReflectionImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReflectionImageService {

    private final DayReflectionImageRepository dayReflectionImageRepository;

    @Transactional(readOnly = true)
    public List<DayReflectionImage> getImagesByReflectionId(Long reflectionId) {
        return dayReflectionImageRepository.findByReflectionIdWithImage(reflectionId);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<DayReflectionImage>> getImagesByReflectionIds(List<Long> reflectionIds) {

        if (reflectionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<DayReflectionImage> images =
                dayReflectionImageRepository.findByReflectionIdsWithImage(reflectionIds);

        return images.stream()
                .collect(Collectors.groupingBy(item -> item.getDayReflection().getId()));
    }
}
