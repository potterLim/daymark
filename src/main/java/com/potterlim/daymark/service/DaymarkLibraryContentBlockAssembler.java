package com.potterlim.daymark.service;

import java.util.ArrayList;
import java.util.List;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryContentBlockDto;
import com.potterlim.daymark.dto.daymark.EDaymarkLibraryContentTone;
import com.potterlim.daymark.entity.DaymarkEntry;
import com.potterlim.daymark.support.EDaymarkSectionType;

final class DaymarkLibraryContentBlockAssembler {

    private static final int MAX_CONTENT_BLOCK_LINE_COUNT = 2;
    private static final List<ContentBlockDefinition> CONTENT_BLOCK_DEFINITIONS = List.of(
        ContentBlockDefinition.of(
            EDaymarkSectionType.FOCUS,
            ContentBlockLabel.of("Focus", "집중 영역"),
            EDaymarkLibraryContentTone.FOCUS
        ),
        ContentBlockDefinition.of(
            EDaymarkSectionType.CHALLENGES,
            ContentBlockLabel.of("Risk", "예상 변수"),
            EDaymarkLibraryContentTone.RISK
        ),
        ContentBlockDefinition.of(
            EDaymarkSectionType.ACHIEVEMENTS,
            ContentBlockLabel.of("Outcome", "성과"),
            EDaymarkLibraryContentTone.OUTCOME
        ),
        ContentBlockDefinition.of(
            EDaymarkSectionType.IMPROVEMENTS,
            ContentBlockLabel.of("Learn", "개선점"),
            EDaymarkLibraryContentTone.IMPROVEMENT
        ),
        ContentBlockDefinition.of(
            EDaymarkSectionType.NOTES,
            ContentBlockLabel.of("Next", "내일 메모"),
            EDaymarkLibraryContentTone.NEXT
        )
    );

    private DaymarkLibraryContentBlockAssembler() {
    }

    static List<DaymarkLibraryContentBlockDto> buildContentBlocks(DaymarkEntry daymarkEntry) {
        if (daymarkEntry == null) {
            throw new IllegalArgumentException("daymarkEntry must not be null.");
        }

        List<DaymarkLibraryContentBlockDto> contentBlocks = new ArrayList<>();
        for (ContentBlockDefinition contentBlockDefinition : CONTENT_BLOCK_DEFINITIONS) {
            addContentBlockIfPresent(contentBlocks, daymarkEntry, contentBlockDefinition);
        }

        return contentBlocks;
    }

    private static void addContentBlockIfPresent(
        List<DaymarkLibraryContentBlockDto> contentBlocks,
        DaymarkEntry daymarkEntry,
        ContentBlockDefinition contentBlockDefinition
    ) {
        List<String> contentLines = DaymarkLibraryItemText.splitContentLines(daymarkEntry.readSection(
            contentBlockDefinition.getDaymarkSectionType()
        ));
        if (contentLines.isEmpty()) {
            return;
        }

        List<String> visibleContentLines = new ArrayList<>();
        int visibleLineCount = Math.min(contentLines.size(), MAX_CONTENT_BLOCK_LINE_COUNT);
        for (int lineIndex = 0; lineIndex < visibleLineCount; lineIndex++) {
            visibleContentLines.add(DaymarkLibraryItemText.abbreviate(contentLines.get(lineIndex)));
        }

        contentBlocks.add(DaymarkLibraryContentBlockDto.createBuilder(contentBlockDefinition.getContentTone())
            .setEyebrow(contentBlockDefinition.getLabel().getEyebrow())
            .setTitle(contentBlockDefinition.getLabel().getTitle())
            .setLines(visibleContentLines)
            .setHiddenLineCount(Math.max(0, contentLines.size() - MAX_CONTENT_BLOCK_LINE_COUNT))
            .build());
    }

    private static final class ContentBlockDefinition {

        private final EDaymarkSectionType mDaymarkSectionType;
        private final ContentBlockLabel mLabel;
        private final EDaymarkLibraryContentTone mContentTone;

        private ContentBlockDefinition(
            EDaymarkSectionType daymarkSectionType,
            ContentBlockLabel label,
            EDaymarkLibraryContentTone contentTone
        ) {
            if (daymarkSectionType == null) {
                throw new IllegalArgumentException("daymarkSectionType must not be null.");
            }

            if (label == null) {
                throw new IllegalArgumentException("label must not be null.");
            }

            if (contentTone == null) {
                throw new IllegalArgumentException("contentTone must not be null.");
            }

            mDaymarkSectionType = daymarkSectionType;
            mLabel = label;
            mContentTone = contentTone;
        }

        private static ContentBlockDefinition of(
            EDaymarkSectionType daymarkSectionType,
            ContentBlockLabel label,
            EDaymarkLibraryContentTone contentTone
        ) {
            return new ContentBlockDefinition(daymarkSectionType, label, contentTone);
        }

        private EDaymarkSectionType getDaymarkSectionType() {
            return mDaymarkSectionType;
        }

        private ContentBlockLabel getLabel() {
            return mLabel;
        }

        private EDaymarkLibraryContentTone getContentTone() {
            return mContentTone;
        }
    }

    private static final class ContentBlockLabel {

        private final String mEyebrow;
        private final String mTitle;

        private ContentBlockLabel(String eyebrow, String title) {
            if (eyebrow == null || eyebrow.isBlank()) {
                throw new IllegalArgumentException("eyebrow must not be blank.");
            }

            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("title must not be blank.");
            }

            mEyebrow = eyebrow;
            mTitle = title;
        }

        private static ContentBlockLabel of(String eyebrow, String title) {
            return new ContentBlockLabel(eyebrow, title);
        }

        private String getEyebrow() {
            return mEyebrow;
        }

        private String getTitle() {
            return mTitle;
        }
    }
}
