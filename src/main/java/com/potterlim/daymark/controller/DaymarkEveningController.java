package com.potterlim.daymark.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.potterlim.daymark.support.DaymarkGoalCheckItem;
import com.potterlim.daymark.dto.daymark.DaymarkDayStatusDto;
import com.potterlim.daymark.dto.daymark.EveningGoalItemDto;
import com.potterlim.daymark.dto.daymark.EveningFormDto;
import com.potterlim.daymark.dto.daymark.EveningReviewSaveCommand;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.service.DaymarkRecordViewService;
import com.potterlim.daymark.service.IDaymarkService;
import com.potterlim.daymark.service.OperationUsageEventService;
import com.potterlim.daymark.support.DaymarkEntryDate;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/daymark")
public class DaymarkEveningController {

    private final IDaymarkService mDaymarkService;
    private final DaymarkRecordViewService mDaymarkRecordViewService;
    private final OperationUsageEventService mOperationUsageEventService;
    private final Clock mClock;

    public DaymarkEveningController(
        IDaymarkService daymarkService,
        DaymarkRecordViewService daymarkRecordViewService,
        OperationUsageEventService operationUsageEventService,
        Clock clock
    ) {
        mDaymarkService = daymarkService;
        mDaymarkRecordViewService = daymarkRecordViewService;
        mOperationUsageEventService = operationUsageEventService;
        mClock = clock;
    }

    @GetMapping("/evening")
    public String showEveningDateList(
        @AuthenticationPrincipal UserAccount userAccount,
        Model model
    ) {
        LocalDate currentDate = LocalDate.now(mClock);
        UserAccountId userAccountId = userAccount.getUserAccountId();
        mOperationUsageEventService.recordUserEvent(EOperationEventType.EVENING_REVIEW_VIEWED, userAccountId);

        List<LocalDate> eveningDates = mDaymarkService.listWeek(DaymarkEntryDate.of(currentDate), userAccountId)
            .stream()
            .filter(daymarkDayStatusDto -> daymarkDayStatusDto.hasMorningEntry() || daymarkDayStatusDto.hasEveningEntry())
            .map(DaymarkDayStatusDto::getDate)
            .toList();

        model.addAttribute("eveningDates", eveningDates);
        model.addAttribute("defaultDate", currentDate);
        return "daymark/evening";
    }

    @GetMapping("/evening/edit")
    public String showEveningEditPage(
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserAccount userAccount,
        Model model
    ) {
        UserAccountId userAccountId = userAccount.getUserAccountId();
        mOperationUsageEventService.recordUserEvent(EOperationEventType.EVENING_REVIEW_VIEWED, userAccountId);
        model.addAttribute("morningEntryHtml", mDaymarkRecordViewService.buildMorningPlanHtml(date, userAccountId));
        model.addAttribute("eveningFormDto", mDaymarkRecordViewService.buildEveningFormDto(date, userAccountId));
        return "daymark/evening-edit";
    }

    @PostMapping("/evening/save")
    public String saveEveningLog(
        @Valid @ModelAttribute("eveningFormDto") EveningFormDto eveningFormDto,
        BindingResult bindingResult,
        @AuthenticationPrincipal UserAccount userAccount,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        validateEveningFormInput(eveningFormDto, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                "morningEntryHtml",
                mDaymarkRecordViewService.buildMorningPlanHtml(eveningFormDto.getDate(), userAccount.getUserAccountId())
            );
            return "daymark/evening-edit";
        }

        UserAccountId userAccountId = userAccount.getUserAccountId();
        mDaymarkService.saveEveningReview(EveningReviewSaveCommand.createFromRawInput(
            eveningFormDto.getDate(),
            userAccountId,
            toGoalCheckItems(eveningFormDto.getGoals()),
            eveningFormDto.getAchievements(),
            eveningFormDto.getImprovements(),
            eveningFormDto.getGratitude(),
            eveningFormDto.getNotes()
        ));

        redirectAttributes.addFlashAttribute("message", "저녁 회고가 저장되었습니다.");
        mOperationUsageEventService.recordUserEvent(EOperationEventType.EVENING_REVIEW_SAVED, userAccountId);
        return "redirect:/daymark/evening";
    }

    private static List<DaymarkGoalCheckItem> toGoalCheckItems(List<EveningGoalItemDto> goalItemsOrNull) {
        if (goalItemsOrNull == null || goalItemsOrNull.isEmpty()) {
            return List.of();
        }

        List<DaymarkGoalCheckItem> goalCheckItems = new ArrayList<>();
        for (EveningGoalItemDto goalItem : goalItemsOrNull) {
            if (goalItem == null) {
                continue;
            }

            if (goalItem.isDone()) {
                goalCheckItems.add(DaymarkGoalCheckItem.createCompleted(goalItem.getText()));
            } else {
                goalCheckItems.add(DaymarkGoalCheckItem.createPending(goalItem.getText()));
            }
        }

        return goalCheckItems;
    }

    private static void validateEveningFormInput(
        EveningFormDto eveningFormDto,
        BindingResult bindingResult
    ) {
        if (eveningFormDto.isWithinTotalBodyLimit()) {
            return;
        }

        bindingResult.reject(
            "eveningForm.totalBodyLength",
            "한 번에 저장할 수 있는 전체 기록은 8,000자까지입니다."
        );
    }
}
