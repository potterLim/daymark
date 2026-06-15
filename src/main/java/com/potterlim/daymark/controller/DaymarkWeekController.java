package com.potterlim.daymark.controller;

import com.potterlim.daymark.dto.daymark.WeeklyProgressViewDto;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.service.DaymarkWeeklyProgressService;
import com.potterlim.daymark.service.OperationUsageEventService;
import com.potterlim.daymark.support.DaymarkWeekOffset;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/daymark")
public class DaymarkWeekController {

    private final DaymarkWeeklyProgressService mDaymarkWeeklyProgressService;
    private final OperationUsageEventService mOperationUsageEventService;

    public DaymarkWeekController(
        DaymarkWeeklyProgressService daymarkWeeklyProgressService,
        OperationUsageEventService operationUsageEventService
    ) {
        mDaymarkWeeklyProgressService = daymarkWeeklyProgressService;
        mOperationUsageEventService = operationUsageEventService;
    }

    @GetMapping("/week")
    public String showWeekPage(
        @RequestParam(name = "week", defaultValue = "0") int weekOffset,
        @AuthenticationPrincipal UserAccount userAccount,
        Model model
    ) {
        UserAccountId userAccountId = userAccount.getUserAccountId();
        mOperationUsageEventService.recordUserEvent(EOperationEventType.WEEKLY_REVIEW_VIEWED, userAccountId);
        WeeklyProgressViewDto weeklyProgressViewDto = mDaymarkWeeklyProgressService.buildWeeklyProgressView(
            DaymarkWeekOffset.of(weekOffset),
            userAccountId
        );

        model.addAttribute("weeklyProgressItems", weeklyProgressViewDto.getWeeklyProgressItems());
        model.addAttribute("weekAchieved", weeklyProgressViewDto.getWeeklyAchievedGoalCount());
        model.addAttribute("weekTotal", weeklyProgressViewDto.getWeeklyTotalGoalCount());
        model.addAttribute("weekPercent", weeklyProgressViewDto.getWeeklyCompletionPercent());
        model.addAttribute("weekOffset", weeklyProgressViewDto.getWeekOffset());
        model.addAttribute("previousWeekOffset", weeklyProgressViewDto.getPreviousWeekOffset());
        model.addAttribute("nextWeekOffset", weeklyProgressViewDto.getNextWeekOffset());
        model.addAttribute("rangeLabel", weeklyProgressViewDto.getRangeLabel());
        model.addAttribute("currentWeekRangeLabel", weeklyProgressViewDto.getCurrentWeekRangeLabel());
        model.addAttribute("previousWeekRangeLabel", weeklyProgressViewDto.getPreviousWeekRangeLabel());
        model.addAttribute("nextWeekRangeLabel", weeklyProgressViewDto.getNextWeekRangeLabel());
        model.addAttribute("defaultDate", weeklyProgressViewDto.getDefaultDate());
        return "daymark/week";
    }
}
