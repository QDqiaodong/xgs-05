package com.handmade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.CheckInRecord;
import com.handmade.vo.CheckInStatsVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckInRecordService extends IService<CheckInRecord> {

    CheckInRecord checkIn(CheckInRecord record);

    CheckInRecord getTodayRecord(Long userId);

    List<CheckInRecord> getMonthRecords(Long userId, Integer year, Integer month);

    IPage<CheckInRecord> getCheckInList(Long userId, Integer page, Integer size);

    CheckInStatsVO getUserStats(Long userId);

    List<Map<String, Object>> getCalendarWall(Long userId, Integer year, Integer month);

    List<CheckInRecord> getGrowthTimeline(Long userId, Integer limit);

    boolean likeCheckIn(Long id);
}
