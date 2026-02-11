package molip.server.reflection.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import molip.server.image.entity.Image;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.event.DayReflectionImagesCreateEvent;
import molip.server.reflection.repository.DayReflectionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReflectionService {

    private static final DateTimeFormatter TITLE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd(E)", Locale.KOREAN);

    private final DayReflectionRepository dayReflectionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DayReflection createReflection(
            molip.server.schedule.entity.DayPlan dayPlan,
            String content,
            boolean isOpen,
            List<Image> images) {
        String title = formatTitle(dayPlan.getPlanDate());

        DayReflection reflection =
                dayReflectionRepository.save(
                        new DayReflection(dayPlan.getUser(), dayPlan, title, content, isOpen));

        if (images != null && !images.isEmpty()) {
            eventPublisher.publishEvent(new DayReflectionImagesCreateEvent(reflection, images));
        }

        return reflection;
    }

    private String formatTitle(LocalDate planDate) {
        return planDate.format(TITLE_FORMATTER);
    }
}
