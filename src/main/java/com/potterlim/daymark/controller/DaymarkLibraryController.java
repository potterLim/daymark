package com.potterlim.daymark.controller;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import com.potterlim.daymark.dto.daymark.DaymarkLibrarySearchCriteria;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryViewDto;
import com.potterlim.daymark.dto.daymark.EDaymarkLibraryExportFormat;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.service.DaymarkRecordViewService;
import com.potterlim.daymark.service.IDaymarkLibraryService;
import com.potterlim.daymark.service.OperationUsageEventService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/daymark")
public class DaymarkLibraryController {

    private final IDaymarkLibraryService mDaymarkLibraryService;
    private final DaymarkRecordViewService mDaymarkRecordViewService;
    private final OperationUsageEventService mOperationUsageEventService;
    private final Clock mClock;

    public DaymarkLibraryController(
        IDaymarkLibraryService daymarkLibraryService,
        DaymarkRecordViewService daymarkRecordViewService,
        OperationUsageEventService operationUsageEventService,
        Clock clock
    ) {
        mDaymarkLibraryService = daymarkLibraryService;
        mDaymarkRecordViewService = daymarkRecordViewService;
        mOperationUsageEventService = operationUsageEventService;
        mClock = clock;
    }

    @GetMapping("/library")
    public String showLibraryPage(
        @RequestParam(name = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDateOrNull,
        @RequestParam(name = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDateOrNull,
        @RequestParam(name = "keyword", required = false) String keywordOrNull,
        @AuthenticationPrincipal UserAccount userAccount,
        Model model
    ) {
        DaymarkLibrarySearchCriteria searchCriteria = buildLibrarySearchCriteria(
            startDateOrNull,
            endDateOrNull,
            keywordOrNull
        );
        DaymarkLibraryViewDto libraryViewDto = mDaymarkLibraryService.searchLibrary(searchCriteria, userAccount.getUserAccountId());

        mOperationUsageEventService.recordUserEvent(EOperationEventType.RECORD_LIBRARY_VIEWED, userAccount.getUserAccountId());
        model.addAttribute("libraryViewDto", libraryViewDto);
        return "daymark/library";
    }

    @GetMapping(value = "/library/export/markdown", produces = "text/markdown; charset=UTF-8")
    public ResponseEntity<String> downloadLibraryMarkdown(
        @RequestParam(name = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDateOrNull,
        @RequestParam(name = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDateOrNull,
        @RequestParam(name = "keyword", required = false) String keywordOrNull,
        @AuthenticationPrincipal UserAccount userAccount
    ) {
        DaymarkLibrarySearchCriteria searchCriteria = buildLibrarySearchCriteria(
            startDateOrNull,
            endDateOrNull,
            keywordOrNull
        );
        String markdownText = mDaymarkLibraryService.buildLibraryMarkdownExport(
            searchCriteria,
            userAccount.getUserAccountId()
        );
        mOperationUsageEventService.recordUserEvent(EOperationEventType.MARKDOWN_EXPORTED, userAccount.getUserAccountId());
        ContentDisposition contentDisposition = ContentDisposition.attachment()
            .filename(
                buildLibraryExportFileName(searchCriteria, EDaymarkLibraryExportFormat.MARKDOWN),
                StandardCharsets.UTF_8
            )
            .build();

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"))
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .body(markdownText);
    }

    @GetMapping("/library/export/pdf")
    public String showLibraryPdfExportPage(
        @RequestParam(name = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDateOrNull,
        @RequestParam(name = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDateOrNull,
        @RequestParam(name = "keyword", required = false) String keywordOrNull,
        @AuthenticationPrincipal UserAccount userAccount,
        Model model
    ) {
        DaymarkLibrarySearchCriteria searchCriteria = buildLibrarySearchCriteria(
            startDateOrNull,
            endDateOrNull,
            keywordOrNull
        );
        DaymarkLibraryViewDto libraryViewDto = mDaymarkLibraryService.searchLibrary(searchCriteria, userAccount.getUserAccountId());

        mOperationUsageEventService.recordUserEvent(EOperationEventType.PDF_EXPORT_VIEWED, userAccount.getUserAccountId());
        model.addAttribute("libraryViewDto", libraryViewDto);
        model.addAttribute("exportItemHtmlByDate", mDaymarkRecordViewService.buildExportItemHtmlByDate(libraryViewDto.getItems()));
        model.addAttribute(
            "exportFileName",
            buildLibraryExportFileName(searchCriteria, EDaymarkLibraryExportFormat.PDF)
        );
        return "daymark/library-export-print";
    }

    private DaymarkLibrarySearchCriteria buildLibrarySearchCriteria(
        LocalDate startDateOrNull,
        LocalDate endDateOrNull,
        String keywordOrNull
    ) {
        return DaymarkLibrarySearchCriteria.create(startDateOrNull, endDateOrNull, keywordOrNull, LocalDate.now(mClock));
    }

    private static String buildLibraryExportFileName(
        DaymarkLibrarySearchCriteria searchCriteria,
        EDaymarkLibraryExportFormat exportFormat
    ) {
        return "daymark-records-"
            + searchCriteria.getStartDate()
            + "-"
            + searchCriteria.getEndDate()
            + "."
            + exportFormat.getFileExtension();
    }
}
