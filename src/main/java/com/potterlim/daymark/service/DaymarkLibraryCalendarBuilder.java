package com.potterlim.daymark.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryCalendarDayDto;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryItemDto;
import org.springframework.stereotype.Component;

@Component
public class DaymarkLibraryCalendarBuilder {

    public List<DaymarkLibraryCalendarDayDto> buildCalendarDays(
        LocalDate calendarMonthDate,
        List<DaymarkLibraryItemDto> matchingItemsAscending,
        LocalDate today
    ) {
        if (calendarMonthDate == null) {
            throw new IllegalArgumentException("calendarMonthDate must not be null.");
        }

        if (matchingItemsAscending == null) {
            throw new IllegalArgumentException("matchingItemsAscending must not be null.");
        }

        if (today == null) {
            throw new IllegalArgumentException("today must not be null.");
        }

        Map<LocalDate, DaymarkLibraryItemDto> libraryItemByDate = new HashMap<>();
        for (DaymarkLibraryItemDto libraryItem : matchingItemsAscending) {
            libraryItemByDate.put(libraryItem.getDate(), libraryItem);
        }

        YearMonth yearMonth = YearMonth.from(calendarMonthDate);
        LocalDate firstDateOfMonth = yearMonth.atDay(1);
        LocalDate lastDateOfMonth = yearMonth.atEndOfMonth();
        LocalDate firstCalendarDate = firstDateOfMonth.minusDays(
            firstDateOfMonth.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue()
        );
        LocalDate lastCalendarDate = lastDateOfMonth.plusDays(
            DayOfWeek.SUNDAY.getValue() - lastDateOfMonth.getDayOfWeek().getValue()
        );

        List<DaymarkLibraryCalendarDayDto> calendarDays = new ArrayList<>();
        LocalDate cursorDate = firstCalendarDate;
        while (!cursorDate.isAfter(lastCalendarDate)) {
            DaymarkLibraryItemDto libraryItemOrNull = libraryItemByDate.get(cursorDate);
            DaymarkLibraryCalendarDayDto.Builder calendarDayBuilder =
                DaymarkLibraryCalendarDayDto.createBuilder(cursorDate);

            if (YearMonth.from(cursorDate).equals(yearMonth)) {
                calendarDayBuilder.markCurrentMonth();
            }

            int completionPercent = 0;
            if (libraryItemOrNull != null) {
                completionPercent = libraryItemOrNull.getCompletionPercent();
                calendarDayBuilder.markEntryPresent();
            }

            if (cursorDate.equals(today)) {
                calendarDayBuilder.markToday();
            }

            calendarDays.add(calendarDayBuilder
                .setCompletionPercent(completionPercent)
                .build());
            cursorDate = cursorDate.plusDays(1L);
        }

        return calendarDays;
    }
}
