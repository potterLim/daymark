package com.potterlim.daymark.support;

import java.util.List;

public final class DaymarkPreviewMarkdown {

    private final String mValue;

    private DaymarkPreviewMarkdown(String value) {
        mValue = value;
    }

    public static DaymarkPreviewMarkdown empty() {
        return new DaymarkPreviewMarkdown("");
    }

    public static DaymarkPreviewMarkdown create(String markdownTextOrNull) {
        if (markdownTextOrNull == null || markdownTextOrNull.isBlank()) {
            return empty();
        }

        return new DaymarkPreviewMarkdown(markdownTextOrNull);
    }

    public static DaymarkPreviewMarkdown fromMorningSections(
        DaymarkSectionText goals,
        DaymarkSectionText focus,
        DaymarkSectionText challenges
    ) {
        if (goals == null) {
            throw new IllegalArgumentException("goals must not be null.");
        }

        if (focus == null) {
            throw new IllegalArgumentException("focus must not be null.");
        }

        if (challenges == null) {
            throw new IllegalArgumentException("challenges must not be null.");
        }

        StringBuilder markdownBuilder = new StringBuilder();
        appendSection(markdownBuilder, EDaymarkSectionType.GOALS, goals);
        appendSection(markdownBuilder, EDaymarkSectionType.FOCUS, focus);
        appendSection(markdownBuilder, EDaymarkSectionType.CHALLENGES, challenges);
        return DaymarkPreviewMarkdown.create(markdownBuilder.toString().stripTrailing());
    }

    public DaymarkPreviewMarkdown normalizeForRendering() {
        if (mValue.isBlank()) {
            return DaymarkPreviewMarkdown.empty();
        }

        StringBuilder processedMarkdownBuilder = new StringBuilder();

        for (String line : DaymarkTextLines.splitLines(mValue)) {
            if (line.startsWith("#")) {
                appendShiftedHeader(processedMarkdownBuilder, line);
                continue;
            }

            processedMarkdownBuilder.append(line).append("\r\n");
        }

        return DaymarkPreviewMarkdown.create(processedMarkdownBuilder.toString().stripTrailing());
    }

    public boolean isBlank() {
        return mValue.isBlank();
    }

    public String getValue() {
        return mValue;
    }

    private static void appendSection(
        StringBuilder markdownBuilder,
        EDaymarkSectionType daymarkSectionType,
        DaymarkSectionText sectionBody
    ) {
        List<String> lines = DaymarkTextLines.splitNonBlankTrimmedLines(sectionBody.getValue());
        if (lines.isEmpty()) {
            return;
        }

        if (markdownBuilder.length() > 0) {
            markdownBuilder.append("\r\n");
        }

        markdownBuilder.append(daymarkSectionType.getHeaderText()).append("\r\n\r\n");
        for (String line : lines) {
            markdownBuilder.append("- ").append(line).append("\r\n");
        }
    }

    private static void appendShiftedHeader(StringBuilder processedMarkdownBuilder, String line) {
        int hashCount = 0;

        while (hashCount < line.length() && line.charAt(hashCount) == '#') {
            hashCount++;
        }

        int newHashCount = Math.min(hashCount + 2, 6);
        String rest = line.substring(hashCount).trim();
        if (processedMarkdownBuilder.length() > 0) {
            processedMarkdownBuilder.append("\r\n");
        }

        processedMarkdownBuilder.append("#".repeat(newHashCount))
            .append(" ")
            .append(rest)
            .append("\r\n");
    }
}
