package com.potterlim.daymark.support;

import java.util.ArrayList;
import java.util.List;

public final class DaymarkTextLines {

    private DaymarkTextLines() {
    }

    public static String[] splitLines(String textOrNull) {
        if (textOrNull == null || textOrNull.isEmpty()) {
            return new String[0];
        }

        return textOrNull.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
    }

    public static List<String> splitNonBlankTrimmedLines(String textOrNull) {
        if (textOrNull == null || textOrNull.isBlank()) {
            return List.of();
        }

        List<String> lines = new ArrayList<>();
        for (String line : splitLines(textOrNull)) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                lines.add(trimmedLine);
            }
        }

        return lines;
    }

    public static String normalizeSectionBody(String textOrNull) {
        return String.join("\r\n", splitNonBlankTrimmedLines(textOrNull));
    }

    public static String removeMarkdownListPrefix(String line) {
        return line.stripLeading().replaceFirst("^-\\s*", "").trim();
    }
}
