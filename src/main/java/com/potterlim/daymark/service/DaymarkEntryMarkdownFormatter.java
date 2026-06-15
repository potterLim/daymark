package com.potterlim.daymark.service;

import java.util.ArrayList;
import java.util.List;
import com.potterlim.daymark.entity.DaymarkEntry;
import com.potterlim.daymark.support.DaymarkTextLines;
import com.potterlim.daymark.support.EDaymarkSectionType;

final class DaymarkEntryMarkdownFormatter {

    private static final List<EDaymarkSectionType> MORNING_SECTION_ORDER = List.of(
        EDaymarkSectionType.GOALS,
        EDaymarkSectionType.FOCUS,
        EDaymarkSectionType.CHALLENGES
    );

    private static final List<EDaymarkSectionType> EVENING_SECTION_ORDER = List.of(
        EDaymarkSectionType.EVENING_GOALS,
        EDaymarkSectionType.ACHIEVEMENTS,
        EDaymarkSectionType.IMPROVEMENTS,
        EDaymarkSectionType.GRATITUDE,
        EDaymarkSectionType.NOTES
    );

    private DaymarkEntryMarkdownFormatter() {
    }

    static String format(DaymarkEntry daymarkEntry) {
        if (daymarkEntry == null) {
            throw new IllegalArgumentException("daymarkEntry must not be null.");
        }

        List<String> blocks = new ArrayList<>();

        if (daymarkEntry.hasMorningEntry()) {
            appendSectionBlocks(blocks, daymarkEntry, MORNING_SECTION_ORDER);
        }

        if (daymarkEntry.hasEveningEntry()) {
            appendSectionBlocks(blocks, daymarkEntry, EVENING_SECTION_ORDER);
        }

        return String.join("\r\n\r\n", blocks).stripTrailing();
    }

    private static void appendSectionBlocks(
        List<String> blocks,
        DaymarkEntry daymarkEntry,
        List<EDaymarkSectionType> sectionOrder
    ) {
        for (EDaymarkSectionType daymarkSectionType : sectionOrder) {
            String sectionBlock = buildSectionBlock(daymarkEntry, daymarkSectionType);
            if (!sectionBlock.isBlank()) {
                blocks.add(sectionBlock);
            }
        }
    }

    private static String buildSectionBlock(DaymarkEntry daymarkEntry, EDaymarkSectionType daymarkSectionType) {
        String sectionBody = daymarkEntry.readSection(daymarkSectionType).strip();
        if (sectionBody.isEmpty()) {
            return "";
        }

        List<String> blockLines = new ArrayList<>();
        blockLines.add(daymarkSectionType.getHeaderText());

        for (String line : DaymarkTextLines.splitLines(sectionBody)) {
            if (line.isBlank()) {
                continue;
            }

            String normalizedLine = DaymarkTextLines.removeMarkdownListPrefix(line);
            blockLines.add("- " + normalizedLine);
        }

        return String.join("\r\n", blockLines);
    }
}
