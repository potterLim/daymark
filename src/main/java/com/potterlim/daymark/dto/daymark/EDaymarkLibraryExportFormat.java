package com.potterlim.daymark.dto.daymark;

public enum EDaymarkLibraryExportFormat {
    MARKDOWN("md"),
    PDF("pdf");

    private final String mFileExtension;

    EDaymarkLibraryExportFormat(String fileExtension) {
        mFileExtension = fileExtension;
    }

    public String getFileExtension() {
        return mFileExtension;
    }
}
