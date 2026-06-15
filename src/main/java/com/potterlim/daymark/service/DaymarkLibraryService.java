package com.potterlim.daymark.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryCalendarDayDto;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryItemDto;
import com.potterlim.daymark.dto.daymark.DaymarkLibrarySearchCriteria;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryViewDto;
import com.potterlim.daymark.entity.DaymarkEntry;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.repository.IDaymarkEntryRepository;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DaymarkLibraryService implements IDaymarkLibraryService {

    private static final int MAX_TREND_ITEM_COUNT = 14;

    private final IDaymarkEntryRepository mDaymarkEntryRepository;
    private final DaymarkLibraryItemAssembler mDaymarkLibraryItemAssembler;
    private final DaymarkLibraryCalendarBuilder mDaymarkLibraryCalendarBuilder;
    private final DaymarkLibraryMarkdownExporter mDaymarkLibraryMarkdownExporter;
    private final Clock mClock;

    public DaymarkLibraryService(
        IDaymarkEntryRepository daymarkEntryRepository,
        DaymarkLibraryItemAssembler daymarkLibraryItemAssembler,
        DaymarkLibraryCalendarBuilder daymarkLibraryCalendarBuilder,
        DaymarkLibraryMarkdownExporter daymarkLibraryMarkdownExporter,
        Clock clock
    ) {
        mDaymarkEntryRepository = daymarkEntryRepository;
        mDaymarkLibraryItemAssembler = daymarkLibraryItemAssembler;
        mDaymarkLibraryCalendarBuilder = daymarkLibraryCalendarBuilder;
        mDaymarkLibraryMarkdownExporter = daymarkLibraryMarkdownExporter;
        mClock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public DaymarkLibraryViewDto searchLibrary(
        DaymarkLibrarySearchCriteria searchCriteria,
        UserAccountId userAccountId
    ) {
        List<DaymarkLibraryItemDto> matchingItemsAscending = listMatchingItems(searchCriteria, userAccountId);
        List<DaymarkLibraryItemDto> timelineItems = new ArrayList<>(matchingItemsAscending);
        Collections.reverse(timelineItems);

        LibrarySummaryCounts librarySummaryCounts = summarizeItems(matchingItemsAscending);
        List<DaymarkLibraryItemDto> trendItems = buildTrendItems(matchingItemsAscending);
        LocalDate calendarMonthDate = searchCriteria.getEndDate().withDayOfMonth(1);
        List<DaymarkLibraryCalendarDayDto> calendarDays = mDaymarkLibraryCalendarBuilder.buildCalendarDays(
            calendarMonthDate,
            matchingItemsAscending,
            LocalDate.now(mClock)
        );

        return DaymarkLibraryViewDto.createBuilder(searchCriteria)
            .setItems(timelineItems)
            .setTrendItems(trendItems)
            .setCalendarDays(calendarDays)
            .setCalendarMonthDate(calendarMonthDate)
            .setMorningEntryCount(librarySummaryCounts.getMorningEntryCount())
            .setEveningEntryCount(librarySummaryCounts.getEveningEntryCount())
            .setGoalCompletionCounts(librarySummaryCounts.getGoalCompletionCounts())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public String buildLibraryMarkdownExport(
        DaymarkLibrarySearchCriteria searchCriteria,
        UserAccountId userAccountId
    ) {
        List<DaymarkLibraryItemDto> matchingItemsAscending = listMatchingItems(searchCriteria, userAccountId);
        return mDaymarkLibraryMarkdownExporter.buildMarkdownExport(searchCriteria, matchingItemsAscending);
    }

    private List<DaymarkLibraryItemDto> listMatchingItems(
        DaymarkLibrarySearchCriteria searchCriteria,
        UserAccountId userAccountId
    ) {
        List<DaymarkLibraryItemDto> libraryItems = new ArrayList<>();
        for (DaymarkEntry daymarkEntry : mDaymarkEntryRepository.findEntriesByUserAccountIdWithinDateRange(
            userAccountId.getValue(),
            searchCriteria.getStartDate(),
            searchCriteria.getEndDate()
        )) {
            if (!daymarkEntry.hasAnyEntryContent()) {
                continue;
            }

            DaymarkLibraryItemDto libraryItem = mDaymarkLibraryItemAssembler.buildItem(daymarkEntry);
            if (mDaymarkLibraryItemAssembler.matchesSearchCriteria(libraryItem, searchCriteria)) {
                libraryItems.add(libraryItem);
            }
        }

        return libraryItems;
    }

    private static LibrarySummaryCounts summarizeItems(List<DaymarkLibraryItemDto> libraryItems) {
        LibrarySummaryCounts librarySummaryCounts = LibrarySummaryCounts.empty();
        for (DaymarkLibraryItemDto libraryItem : libraryItems) {
            librarySummaryCounts.include(libraryItem);
        }

        return librarySummaryCounts;
    }

    private static List<DaymarkLibraryItemDto> buildTrendItems(List<DaymarkLibraryItemDto> matchingItemsAscending) {
        if (matchingItemsAscending.size() <= MAX_TREND_ITEM_COUNT) {
            return List.copyOf(matchingItemsAscending);
        }

        int fromIndex = matchingItemsAscending.size() - MAX_TREND_ITEM_COUNT;
        return List.copyOf(matchingItemsAscending.subList(fromIndex, matchingItemsAscending.size()));
    }

    private static final class LibrarySummaryCounts {

        private int mMorningEntryCount;
        private int mEveningEntryCount;
        private DaymarkGoalCompletionCounts mGoalCompletionCounts;

        private LibrarySummaryCounts() {
            mMorningEntryCount = 0;
            mEveningEntryCount = 0;
            mGoalCompletionCounts = DaymarkGoalCompletionCounts.empty();
        }

        private static LibrarySummaryCounts empty() {
            return new LibrarySummaryCounts();
        }

        private void include(DaymarkLibraryItemDto libraryItem) {
            if (libraryItem == null) {
                throw new IllegalArgumentException("libraryItem must not be null.");
            }

            if (libraryItem.hasMorningEntry()) {
                mMorningEntryCount++;
            }

            if (libraryItem.hasEveningEntry()) {
                mEveningEntryCount++;
            }

            mGoalCompletionCounts = mGoalCompletionCounts.plus(DaymarkGoalCompletionCounts.of(
                libraryItem.getAchievedGoalCount(),
                libraryItem.getTotalGoalCount()
            ));
        }

        private int getMorningEntryCount() {
            return mMorningEntryCount;
        }

        private int getEveningEntryCount() {
            return mEveningEntryCount;
        }

        private DaymarkGoalCompletionCounts getGoalCompletionCounts() {
            return mGoalCompletionCounts;
        }
    }
}
