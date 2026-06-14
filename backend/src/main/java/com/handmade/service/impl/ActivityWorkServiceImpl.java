package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.Activity;
import com.handmade.entity.ActivityWork;
import com.handmade.entity.User;
import com.handmade.entity.Work;
import com.handmade.mapper.ActivityWorkMapper;
import com.handmade.service.ActivityService;
import com.handmade.service.ActivityWorkService;
import com.handmade.service.UserService;
import com.handmade.service.WorkService;
import com.handmade.vo.ActivityWorkVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityWorkServiceImpl extends ServiceImpl<ActivityWorkMapper, ActivityWork> implements ActivityWorkService {

    @Autowired
    @Lazy
    private ActivityService activityService;

    @Autowired
    private WorkService workService;

    @Autowired
    private UserService userService;

    @Override
    public IPage<ActivityWorkVO> getActivityWorks(Long activityId, Integer page, Integer size, String sortBy) {
        LambdaQueryWrapper<ActivityWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityWork::getActivityId, activityId);
        wrapper.eq(ActivityWork::getAuditStatus, 1);
        if ("vote".equals(sortBy)) {
            wrapper.orderByDesc(ActivityWork::getVoteCount);
        } else {
            wrapper.orderByDesc(ActivityWork::getSubmitTime);
        }
        IPage<ActivityWork> activityWorkPage = this.page(new Page<>(page, size), wrapper);
        IPage<ActivityWorkVO> voPage = new Page<>(activityWorkPage.getCurrent(), activityWorkPage.getSize(), activityWorkPage.getTotal());
        List<ActivityWorkVO> voList = new ArrayList<>();
        if (!activityWorkPage.getRecords().isEmpty()) {
            List<Long> workIds = activityWorkPage.getRecords().stream().map(ActivityWork::getWorkId).collect(Collectors.toList());
            List<Long> userIds = activityWorkPage.getRecords().stream().map(ActivityWork::getUserId).collect(Collectors.toList());
            Map<Long, Work> workMap = workService.listByIds(workIds).stream().collect(Collectors.toMap(Work::getId, w -> w));
            Map<Long, User> userMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
            for (ActivityWork aw : activityWorkPage.getRecords()) {
                ActivityWorkVO vo = new ActivityWorkVO();
                BeanUtils.copyProperties(aw, vo);
                vo.setWork(workMap.get(aw.getWorkId()));
                vo.setUser(userMap.get(aw.getUserId()));
                voList.add(vo);
            }
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public IPage<ActivityWorkVO> getMySubmissions(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<ActivityWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityWork::getUserId, userId);
        wrapper.orderByDesc(ActivityWork::getSubmitTime);
        IPage<ActivityWork> activityWorkPage = this.page(new Page<>(page, size), wrapper);
        IPage<ActivityWorkVO> voPage = new Page<>(activityWorkPage.getCurrent(), activityWorkPage.getSize(), activityWorkPage.getTotal());
        List<ActivityWorkVO> voList = new ArrayList<>();
        if (!activityWorkPage.getRecords().isEmpty()) {
            List<Long> workIds = activityWorkPage.getRecords().stream().map(ActivityWork::getWorkId).collect(Collectors.toList());
            Map<Long, Work> workMap = workService.listByIds(workIds).stream().collect(Collectors.toMap(Work::getId, w -> w));
            User user = userService.getById(userId);
            for (ActivityWork aw : activityWorkPage.getRecords()) {
                ActivityWorkVO vo = new ActivityWorkVO();
                BeanUtils.copyProperties(aw, vo);
                vo.setWork(workMap.get(aw.getWorkId()));
                vo.setUser(user);
                voList.add(vo);
            }
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Long submitWork(Long activityId, Long workId, Long userId, String submitRemark) {
        Activity activity = activityService.getById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            return null;
        }
        Work work = workService.getById(workId);
        if (work == null || !work.getUserId().equals(userId)) {
            return null;
        }
        if (activity.getCategoryId() != null && !activity.getCategoryId().equals(work.getCategoryId())) {
            return null;
        }
        int userSubmitCount = getUserSubmitCount(activityId, userId);
        if (activity.getMaxSubmitPerUser() != null && userSubmitCount >= activity.getMaxSubmitPerUser()) {
            return null;
        }
        LambdaQueryWrapper<ActivityWork> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(ActivityWork::getActivityId, activityId);
        existWrapper.eq(ActivityWork::getWorkId, workId);
        if (this.count(existWrapper) > 0) {
            return null;
        }
        ActivityWork activityWork = new ActivityWork();
        activityWork.setActivityId(activityId);
        activityWork.setWorkId(workId);
        activityWork.setUserId(userId);
        activityWork.setSubmitRemark(submitRemark);
        activityWork.setVoteCount(0);
        activityWork.setRankNum(0);
        activityWork.setSubmitTime(now);
        activityWork.setAuditStatus(1);
        this.save(activityWork);
        activity.setWorkCount(activity.getWorkCount() + 1);
        activityService.updateById(activity);
        return activityWork.getId();
    }

    @Override
    public boolean auditWork(Long id, Integer auditStatus, String auditRemark, Long auditorId) {
        ActivityWork activityWork = new ActivityWork();
        activityWork.setId(id);
        activityWork.setAuditStatus(auditStatus);
        activityWork.setAuditRemark(auditRemark);
        activityWork.setAuditorId(auditorId);
        activityWork.setAuditTime(LocalDateTime.now());
        return this.updateById(activityWork);
    }

    @Override
    public boolean incrementVoteCount(Long activityWorkId, int count) {
        LambdaUpdateWrapper<ActivityWork> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityWork::getId, activityWorkId)
                .setSql("vote_count = vote_count + " + count);
        return this.baseMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public int getUserSubmitCount(Long activityId, Long userId) {
        LambdaQueryWrapper<ActivityWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityWork::getActivityId, activityId);
        wrapper.eq(ActivityWork::getUserId, userId);
        return (int) this.count(wrapper);
    }
}
