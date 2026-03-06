package molip.server.batch.weeklyIngest.step2;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import molip.server.batch.entity.BatchJobRun;
import molip.server.batch.entity.BatchStepRun;
import molip.server.batch.enums.BatchStepStatus;
import molip.server.batch.enums.BatchTargetType;
import molip.server.batch.service.BatchTrackingService;
import molip.server.common.enums.AssignedBy;
import molip.server.common.enums.AssignmentStatus;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.entity.ScheduleHistory;
import molip.server.schedule.repository.DayPlanRepository;
import molip.server.schedule.repository.ScheduleHistoryRepository;
import molip.server.schedule.repository.ScheduleRepository;
import molip.server.user.entity.Users;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

@RequiredArgsConstructor
public class WeeklyAiIngestItemWriter implements ItemWriter<Users>, StepExecutionListener {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_TEXT_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final String RECORD_TYPE = "USER_FINAL";

    private static final String INSERT_RECORD_TASKS =
            "insert into record_tasks (record_id, task_id, day_plan_id, title, status, "
                    + "parent_schedule_id, task_type, assigned_by, assignment_status, "
                    + "start_at, end_at, created_at) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_SCHEDULE_HISTORIES =
            "insert into schedule_histories (record_id, schedule_id, event_type, prev_start_at, "
                    + "prev_end_at, new_start_at, new_end_at, created_at_client, created_at_server) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final BatchTrackingService trackingService;
    private final DayPlanRepository dayPlanRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleHistoryRepository scheduleHistoryRepository;
    private final JdbcTemplate aiJdbcTemplate;

    private BatchJobRun jobRun;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private SimpleJdbcInsert plannerRecordInsert;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        Long jobRunId =
                stepExecution.getJobExecution().getExecutionContext().getLong("batchJobRunId");
        this.jobRun = trackingService.getJobRun(jobRunId);
        this.plannerRecordInsert =
                new SimpleJdbcInsert(aiJdbcTemplate)
                        .withTableName("planner_records")
                        .usingGeneratedKeyColumns("id");

        LocalDate runDate = resolveRunDate(stepExecution);
        this.periodEnd = runDate.minusDays(1);
        this.periodStart = periodEnd.minusDays(6);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    @Override
    public void write(Chunk<? extends Users> items) {
        for (Users user : items) {
            BatchStepRun stepRun =
                    trackingService.createStepRun(
                            jobRun, "weeklyAiIngestStep", BatchTargetType.USER, user.getId());
            trackingService.markStepStarted(stepRun.getId());
            try {
                ingestUser(user);
                trackingService.markStepFinished(stepRun.getId(), BatchStepStatus.SUCCESS, null);
            } catch (Exception e) {
                trackingService.markStepFinished(
                        stepRun.getId(), BatchStepStatus.FAILED, e.getMessage());
                throw new IllegalStateException(
                        "Weekly AI ingest failed for user " + user.getId(), e);
            }
        }
    }

    private void ingestUser(Users user) {
        List<DayPlan> dayPlans =
                dayPlanRepository.findByUserIdAndPlanDateBetweenAndDeletedAtIsNull(
                        user.getId(), periodStart, periodEnd);
        if (dayPlans.isEmpty()) {
            return;
        }

        List<Long> dayPlanIds = dayPlans.stream().map(DayPlan::getId).toList();

        purgeExistingRecords(user.getId(), dayPlanIds);

        List<Schedule> schedules =
                scheduleRepository.findByDayPlanIdInAndDeletedAtIsNull(dayPlanIds);
        List<Long> scheduleIds = schedules.stream().map(Schedule::getId).toList();

        List<ScheduleHistory> histories =
                scheduleIds.isEmpty()
                        ? List.of()
                        : scheduleHistoryRepository.findByScheduleIdInAndDeletedAtIsNull(
                                scheduleIds);

        Map<Long, List<Schedule>> schedulesByDayPlan =
                schedules.stream()
                        .collect(Collectors.groupingBy(schedule -> schedule.getDayPlan().getId()));

        Map<Long, Long> recordIdByDayPlanId =
                createPlannerRecords(user, dayPlans, schedulesByDayPlan);

        if (!schedules.isEmpty()) {
            batchInsertRecordTasks(schedules, recordIdByDayPlanId);
        }

        if (!histories.isEmpty()) {
            batchInsertScheduleHistories(histories, recordIdByDayPlanId);
        }
    }

    private void purgeExistingRecords(Long userId, List<Long> dayPlanIds) {
        if (userId == null || dayPlanIds == null || dayPlanIds.isEmpty()) {
            return;
        }

        List<Long> recordIds = findExistingRecordIds(userId, dayPlanIds);

        if (recordIds.isEmpty()) {
            return;
        }

        deleteByRecordIds("delete from schedule_histories where record_id in ", recordIds);
        deleteByRecordIds("delete from record_tasks where record_id in ", recordIds);
        deleteByRecordIds("delete from planner_records where id in ", recordIds);
    }

    private List<Long> findExistingRecordIds(Long userId, List<Long> dayPlanIds) {
        String placeholders = buildPlaceholders(dayPlanIds.size());
        String sql =
                "select id from planner_records "
                        + "where user_id = ? and day_plan_id in ("
                        + placeholders
                        + ")";

        Object[] params = new Object[1 + dayPlanIds.size()];
        params[0] = userId;

        for (int i = 0; i < dayPlanIds.size(); i++) {
            params[i + 1] = dayPlanIds.get(i);
        }

        return aiJdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong(1), params);
    }

    private void deleteByRecordIds(String sqlPrefix, List<Long> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return;
        }

        String placeholders = buildPlaceholders(recordIds.size());
        String sql = sqlPrefix + "(" + placeholders + ")";

        aiJdbcTemplate.update(sql, new ArrayList<>(recordIds).toArray());
    }

    private String buildPlaceholders(int size) {
        return IntStream.range(0, size).mapToObj(index -> "?").collect(Collectors.joining(","));
    }

    private Map<Long, Long> createPlannerRecords(
            Users user, List<DayPlan> dayPlans, Map<Long, List<Schedule>> schedulesByDayPlan) {
        return dayPlans.stream()
                .collect(
                        Collectors.toMap(
                                DayPlan::getId,
                                dayPlan -> insertPlannerRecord(user, dayPlan, schedulesByDayPlan)));
    }

    private Long insertPlannerRecord(
            Users user, DayPlan dayPlan, Map<Long, List<Schedule>> schedulesByDayPlan) {
        List<Schedule> schedules = schedulesByDayPlan.getOrDefault(dayPlan.getId(), List.of());

        int assignedCount =
                (int)
                        schedules.stream()
                                .filter(
                                        schedule ->
                                                schedule.getAssignmentStatus()
                                                        == AssignmentStatus.ASSIGNED)
                                .count();
        int excludedCount =
                (int)
                        schedules.stream()
                                .filter(
                                        schedule ->
                                                schedule.getAssignmentStatus()
                                                        == AssignmentStatus.EXCLUDED)
                                .count();
        int totalTasks = assignedCount + excludedCount;

        double fillRate = calculateFillRate(assignedCount, totalTasks);

        LocalDateTime now = LocalDateTime.now(ZONE_ID);

        Map<String, Object> values = new HashMap<>();
        values.put("user_id", user.getId());
        values.put("day_plan_id", dayPlan.getId());
        values.put("record_type", RECORD_TYPE);
        values.put("start_arrange", toTimeText(resolveAiStartArrange(schedules)));
        values.put("day_end_time", toTimeText(user.getDayEndTime()));
        values.put("focus_time_zone", user.getFocusTimeZone().name());
        values.put("user_age", calculateAge(user, dayPlan.getPlanDate()));
        values.put("user_gender", user.getGender().name());
        values.put("total_tasks", totalTasks);
        values.put("assigned_count", assignedCount);
        values.put("excluded_count", excludedCount);
        values.put("fill_rate", fillRate);
        values.put("created_at", Timestamp.valueOf(now));

        Number key = plannerRecordInsert.executeAndReturnKey(values);
        return key.longValue();
    }

    private void batchInsertRecordTasks(
            List<Schedule> schedules, Map<Long, Long> recordIdByDayPlanId) {
        List<Object[]> params =
                schedules.stream()
                        .map(
                                schedule -> {
                                    Long recordId =
                                            recordIdByDayPlanId.get(schedule.getDayPlan().getId());
                                    if (recordId == null) {
                                        return null;
                                    }
                                    Long parentId =
                                            schedule.getParentSchedule() == null
                                                    ? null
                                                    : schedule.getParentSchedule().getId();
                                    LocalDateTime now = LocalDateTime.now(ZONE_ID);
                                    return new Object[] {
                                        recordId,
                                        schedule.getId(),
                                        schedule.getDayPlan().getId(),
                                        schedule.getTitle(),
                                        schedule.getStatus().name(),
                                        parentId,
                                        schedule.getType().name(),
                                        schedule.getAssignedBy().name(),
                                        schedule.getAssignmentStatus().name(),
                                        toTimeText(schedule.getStartAt()),
                                        toTimeText(schedule.getEndAt()),
                                        Timestamp.valueOf(now)
                                    };
                                })
                        .filter(value -> value != null)
                        .toList();
        if (!params.isEmpty()) {
            aiJdbcTemplate.batchUpdate(INSERT_RECORD_TASKS, params);
        }
    }

    private void batchInsertScheduleHistories(
            List<ScheduleHistory> histories, Map<Long, Long> recordIdByDayPlanId) {
        List<Object[]> params =
                histories.stream()
                        .map(
                                history -> {
                                    Long scheduleId = history.getSchedule().getId();
                                    Long dayPlanId = history.getSchedule().getDayPlan().getId();
                                    Long recordId = recordIdByDayPlanId.get(dayPlanId);
                                    if (recordId == null) {
                                        return null;
                                    }
                                    LocalDateTime now = LocalDateTime.now(ZONE_ID);
                                    return new Object[] {
                                        recordId,
                                        scheduleId,
                                        history.getEventType().name(),
                                        toTimeTextTime(history.getPrevStartAt()),
                                        toTimeTextTime(history.getPrevEndAt()),
                                        toTimeTextTime(history.getNextStartAt()),
                                        toTimeTextTime(history.getNextEndAt()),
                                        toTimestamp(history.getCreatedAt()),
                                        Timestamp.valueOf(now)
                                    };
                                })
                        .filter(value -> value != null)
                        .toList();
        if (!params.isEmpty()) {
            aiJdbcTemplate.batchUpdate(INSERT_SCHEDULE_HISTORIES, params);
        }
    }

    private Timestamp toTimestamp(java.time.LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private String toTimeText(LocalTime value) {
        return value == null ? null : value.format(TIME_TEXT_FORMATTER);
    }

    private String toTimeTextTime(LocalDateTime value) {
        return value == null ? null : value.format(TIME_TEXT_FORMATTER);
    }

    private java.time.LocalTime findStartArrange(List<Schedule> schedules) {
        return schedules.stream()
                .map(Schedule::getStartAt)
                .filter(value -> value != null)
                .min(java.time.LocalTime::compareTo)
                .orElse(null);
    }

    private LocalTime resolveAiStartArrange(List<Schedule> schedules) {
        return schedules.stream()
                .filter(schedule -> schedule.getAssignedBy() == AssignedBy.AI)
                .map(Schedule::getUpdatedAt)
                .filter(value -> value != null)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toLocalTime)
                .orElse(null);
    }

    private int calculateAge(Users user, LocalDate referenceDate) {
        if (user.getBirth() == null) {
            return 0;
        }
        return Period.between(user.getBirth(), referenceDate).getYears();
    }

    private double calculateFillRate(int assignedCount, int totalTasks) {
        if (totalTasks <= 0) {
            return 0.0;
        }
        double rate = (double) assignedCount / totalTasks;
        return Math.round(rate * 10000.0) / 10000.0;
    }

    private LocalDate resolveRunDate(StepExecution stepExecution) {
        String runDateText =
                stepExecution.getJobExecution().getExecutionContext().getString("batchRunDate");
        if (runDateText == null || runDateText.isBlank()) {
            return LocalDate.now(ZONE_ID);
        }

        try {
            return LocalDate.parse(runDateText);
        } catch (DateTimeParseException e) {
            return LocalDate.now(ZONE_ID);
        }
    }
}
