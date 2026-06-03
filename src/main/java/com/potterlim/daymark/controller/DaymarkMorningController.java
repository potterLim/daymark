package com.potterlim.daymark.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import com.potterlim.daymark.dto.daymark.DaymarkDayStatusDto;
import com.potterlim.daymark.dto.daymark.MorningFormDto;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.service.DaymarkRecordViewService;
import com.potterlim.daymark.service.IDaymarkService;
import com.potterlim.daymark.service.OperationUsageEventService;
import com.potterlim.daymark.support.EDaymarkSectionType;
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
public class DaymarkMorningController {

    private final IDaymarkService mDaymarkService;
    private final DaymarkRecordViewService mDaymarkRecordViewService;
    private final OperationUsageEventService mOperationUsageEventService;
    private final Clock mClock;

    public DaymarkMorningController(
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

    @GetMapping("/morning")
    public String showMorningDateList(@AuthenticationPrincipal UserAccount userAccount, Model model) {
        LocalDate currentDate = LocalDate.now(mClock);
        UserAccountId userAccountId = userAccount.getUserAccountId();
        mOperationUsageEventService.recordUserEvent(EOperationEventType.MORNING_PLAN_VIEWED, userAccountId);
        List<LocalDate> morningDates = mDaymarkService.listWeek(currentDate, userAccountId)
            .stream()
            .filter(DaymarkDayStatusDto::hasMorningEntry)
            .map(DaymarkDayStatusDto::getDate)
            .toList();

        model.addAttribute("morningDates", morningDates);
        model.addAttribute("defaultDate", currentDate);
        return "daymark/morning";
    }

    @GetMapping("/morning/edit")
    public String showMorningEditPage(
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserAccount userAccount,
        Model model
    ) {
        UserAccountId userAccountId = userAccount.getUserAccountId();
        mOperationUsageEventService.recordUserEvent(EOperationEventType.MORNING_PLAN_VIEWED, userAccountId);
        MorningFormDto morningFormDto = new MorningFormDto();
        morningFormDto.setDate(date);
        morningFormDto.setGoals(mDaymarkService.readSection(date, userAccountId, EDaymarkSectionType.GOALS));
        morningFormDto.setFocus(mDaymarkService.readSection(date, userAccountId, EDaymarkSectionType.FOCUS));
        morningFormDto.setChallenges(mDaymarkService.readSection(date, userAccountId, EDaymarkSectionType.CHALLENGES));

        model.addAttribute("morningFormDto", morningFormDto);
        return "daymark/morning-edit";
    }

    @PostMapping("/morning/save")
    public String saveMorningLog(
        @Valid @ModelAttribute("morningFormDto") MorningFormDto morningFormDto,
        BindingResult bindingResult,
        @AuthenticationPrincipal UserAccount userAccount,
        RedirectAttributes redirectAttributes
    ) {
        validateMorningFormInput(morningFormDto, bindingResult);

        if (bindingResult.hasErrors()) {
            return "daymark/morning-edit";
        }

        UserAccountId userAccountId = userAccount.getUserAccountId();
        String goalsMarkdownList = mDaymarkRecordViewService.buildGoalMarkdownList(morningFormDto.getGoals());
        mDaymarkService.writeSection(
            morningFormDto.getDate(),
            userAccountId,
            EDaymarkSectionType.GOALS,
            goalsMarkdownList
        );
        mDaymarkService.writeSection(
            morningFormDto.getDate(),
            userAccountId,
            EDaymarkSectionType.FOCUS,
            morningFormDto.getFocus()
        );
        mDaymarkService.writeSection(
            morningFormDto.getDate(),
            userAccountId,
            EDaymarkSectionType.CHALLENGES,
            morningFormDto.getChallenges()
        );

        redirectAttributes.addFlashAttribute("message", "아침 계획이 저장되었습니다.");
        mOperationUsageEventService.recordUserEvent(EOperationEventType.MORNING_PLAN_SAVED, userAccountId);
        return "redirect:/daymark/morning";
    }

    private static void validateMorningFormInput(
        MorningFormDto morningFormDto,
        BindingResult bindingResult
    ) {
        if (!morningFormDto.hasValidGoalLineCount()) {
            bindingResult.rejectValue(
                "goals",
                "morningForm.goalLineCount",
                "목표는 30개 이하로 입력해주세요."
            );
        }

        if (!morningFormDto.hasValidGoalLineLengths()) {
            bindingResult.rejectValue(
                "goals",
                "morningForm.goalLineLength",
                "목표는 한 줄당 300자 이하로 입력해주세요."
            );
        }

        if (morningFormDto.isWithinTotalBodyLimit()) {
            return;
        }

        bindingResult.reject(
            "morningForm.totalBodyLength",
            "한 번에 저장할 수 있는 전체 기록은 8,000자까지입니다."
        );
    }
}
