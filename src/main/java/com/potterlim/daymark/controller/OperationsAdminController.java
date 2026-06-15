package com.potterlim.daymark.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import com.potterlim.daymark.dto.operations.OperationsTrendViewAssembler;
import com.potterlim.daymark.entity.WeeklyOperationMetricSnapshot;
import com.potterlim.daymark.service.WeeklyOperationMetricSnapshotService;
import com.potterlim.daymark.support.WeeklyOperationsSummary;
import com.potterlim.daymark.service.WeeklyOperationsSummaryService;
import com.potterlim.daymark.support.DaymarkDateRange;
import com.potterlim.daymark.support.DaymarkWeekRange;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/operations")
public class OperationsAdminController {

    private static final int DEFAULT_TREND_WEEK_COUNT = 12;
    private static final int MINIMUM_TREND_WEEK_COUNT = 4;
    private static final int MAXIMUM_TREND_WEEK_COUNT = 24;
    private static final List<Integer> TREND_WEEK_OPTIONS = List.of(4, 8, 12, 24);

    private final WeeklyOperationsSummaryService mWeeklyOperationsSummaryService;
    private final WeeklyOperationMetricSnapshotService mWeeklyOperationMetricSnapshotService;
    private final Clock mClock;

    public OperationsAdminController(
        WeeklyOperationsSummaryService weeklyOperationsSummaryService,
        WeeklyOperationMetricSnapshotService weeklyOperationMetricSnapshotService,
        Clock clock
    ) {
        mWeeklyOperationsSummaryService = weeklyOperationsSummaryService;
        mWeeklyOperationMetricSnapshotService = weeklyOperationMetricSnapshotService;
        mClock = clock;
    }

    @GetMapping
    public String showOperationsDashboard(
        @RequestParam(name = "date", required = false) LocalDate referenceDateOrNull,
        @RequestParam(name = "weeks", required = false) Integer trendWeekCountOrNull,
        Model model
    ) {
        LocalDate currentDate = LocalDate.now(mClock);
        LocalDate selectedReferenceDate = resolveSelectedReferenceDate(referenceDateOrNull, currentDate);
        int selectedTrendWeekCount = resolveSelectedTrendWeekCount(trendWeekCountOrNull);
        DaymarkWeekRange selectedWeekRange = DaymarkWeekRange.containing(selectedReferenceDate)
            .withEndNoLaterThan(currentDate);
        LocalDate trendStartDate = selectedWeekRange.getStartDate().minusWeeks(selectedTrendWeekCount - 1L);
        DaymarkDateRange trendDateRange = DaymarkDateRange.of(trendStartDate, selectedWeekRange.getEndDate());
        List<WeeklyOperationMetricSnapshot> selectedWeeklySnapshots =
            mWeeklyOperationMetricSnapshotService.listWeeklySnapshotsWithinDateRange(trendDateRange);
        WeeklyOperationsSummary currentWeeklySummary =
            mWeeklyOperationsSummaryService.buildWeeklySummary(selectedWeekRange);

        model.addAttribute("currentWeeklySummary", currentWeeklySummary);
        model.addAttribute("recentWeeklySnapshots", selectedWeeklySnapshots);
        model.addAttribute("selectedTrendDate", selectedReferenceDate);
        model.addAttribute("selectedTrendStartDate", trendStartDate);
        model.addAttribute("selectedTrendEndDate", selectedWeekRange.getEndDate());
        model.addAttribute("selectedTrendWeekCount", selectedTrendWeekCount);
        model.addAttribute("trendWeekOptions", TREND_WEEK_OPTIONS);
        model.addAttribute(
            "operationsTrendViewDto",
            OperationsTrendViewAssembler.create(selectedWeeklySnapshots, currentWeeklySummary)
        );
        return "admin/operations";
    }

    private static LocalDate resolveSelectedReferenceDate(LocalDate referenceDateOrNull, LocalDate currentDate) {
        if (referenceDateOrNull == null || referenceDateOrNull.isAfter(currentDate)) {
            return currentDate;
        }

        return referenceDateOrNull;
    }

    private static int resolveSelectedTrendWeekCount(Integer trendWeekCountOrNull) {
        if (trendWeekCountOrNull == null) {
            return DEFAULT_TREND_WEEK_COUNT;
        }

        if (trendWeekCountOrNull < MINIMUM_TREND_WEEK_COUNT) {
            return MINIMUM_TREND_WEEK_COUNT;
        }

        if (trendWeekCountOrNull > MAXIMUM_TREND_WEEK_COUNT) {
            return MAXIMUM_TREND_WEEK_COUNT;
        }

        return trendWeekCountOrNull;
    }
}
