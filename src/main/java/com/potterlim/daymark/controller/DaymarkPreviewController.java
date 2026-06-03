package com.potterlim.daymark.controller;

import java.time.LocalDate;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.service.DaymarkRecordViewService;
import com.potterlim.daymark.service.IDaymarkService;
import com.potterlim.daymark.service.OperationUsageEventService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/daymark")
public class DaymarkPreviewController {

    private final IDaymarkService mDaymarkService;
    private final DaymarkRecordViewService mDaymarkRecordViewService;
    private final OperationUsageEventService mOperationUsageEventService;

    public DaymarkPreviewController(
        IDaymarkService daymarkService,
        DaymarkRecordViewService daymarkRecordViewService,
        OperationUsageEventService operationUsageEventService
    ) {
        mDaymarkService = daymarkService;
        mDaymarkRecordViewService = daymarkRecordViewService;
        mOperationUsageEventService = operationUsageEventService;
    }

    @GetMapping("/preview")
    public String showLogPreview(
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserAccount userAccount,
        Model model
    ) {
        UserAccountId userAccountId = userAccount.getUserAccountId();
        mOperationUsageEventService.recordUserEvent(EOperationEventType.RECORD_PREVIEW_VIEWED, userAccountId);
        String markdownText = mDaymarkService.readEntryMarkdownContent(date, userAccountId);
        boolean hasPreviewContent = !markdownText.isBlank();
        String previewHtml = "";
        if (hasPreviewContent) {
            previewHtml = mDaymarkRecordViewService.buildPreviewHtml(markdownText);
        }

        model.addAttribute("previewDate", date);
        model.addAttribute("hasPreviewContent", hasPreviewContent);
        model.addAttribute("previewHtml", previewHtml);
        return "daymark/log-preview";
    }
}
