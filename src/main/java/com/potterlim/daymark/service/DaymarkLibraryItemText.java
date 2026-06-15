package com.potterlim.daymark.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.potterlim.daymark.support.DaymarkTextLines;

final class DaymarkLibraryItemText {

    private static final int MAX_PREVIEW_CHARACTER_COUNT = 96;

    private DaymarkLibraryItemText() {
    }

    static String abbreviate(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null.");
        }

        if (text.length() <= MAX_PREVIEW_CHARACTER_COUNT) {
            return text;
        }

        return text.substring(0, MAX_PREVIEW_CHARACTER_COUNT - 1).stripTrailing() + "…";
    }

    static String normalizeComparableGoalText(String textOrNull) {
        if (textOrNull == null) {
            return "";
        }

        return textOrNull.trim().toLowerCase(Locale.ROOT);
    }

    static List<String> splitContentLines(String textOrNull) {
        if (textOrNull == null || textOrNull.isBlank()) {
            return List.of();
        }

        List<String> contentLines = new ArrayList<>();
        for (String line : DaymarkTextLines.splitLines(textOrNull)) {
            String normalizedLine = line.stripLeading()
                .replaceFirst("^- \\[[xX ]\\]\\s*", "")
                .replaceFirst("^-\\s*", "")
                .trim();
            if (!normalizedLine.isEmpty()) {
                contentLines.add(normalizedLine);
            }
        }

        return contentLines;
    }
}
