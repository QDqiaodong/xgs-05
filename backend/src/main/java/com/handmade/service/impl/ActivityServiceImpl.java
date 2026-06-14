package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.Activity;
import com.handmade.entity.ActivityWork;
import com.handmade.entity.ActivityVote;
import com.handmade.mapper.ActivityMapper;
import com.handmade.service.ActivityService;
import com.handmade.service.ActivityWorkService;
import com.handmade.service.ActivityVoteService;
import com.handmade.vo.ActivityDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    @Lazy
    private ActivityWorkService activityWorkService;

    @Autowired
    @Lazy
    private ActivityVoteService activityVoteService;

    @Override
    public IPage<Activity> getActivityList(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<Activity> getOngoingActivities(Integer page, Integer size) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getStatus, 1);
        wrapper.le(Activity::getStartTime, now);
        wrapper.ge(Activity::getEndTime, now);
        wrapper.orderByDesc(Activity::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public ActivityDetailVO getActivityDetail(Long id) {
        Activity activity = this.getById(id);
        if (activity == null) {
            return null;
        }
        incrementViewCount(id);
        ActivityDetailVO vo = new ActivityDetailVO();
        vo.setActivity(activity);
        LocalDateTime now = LocalDateTime.now();
        vo.setIsOngoing(activity.getStatus() == 1 && now.isAfter(activity.getStartTime()) && now.isBefore(activity.getEndTime()));
        LocalDateTime voteStart = activity.getVoteStartTime() != null ? activity.getVoteStartTime() : activity.getStartTime();
        LocalDateTime voteEnd = activity.getVoteEndTime() != null ? activity.getVoteEndTime() : activity.getEndTime();
        vo.setIsVoting(now.isAfter(voteStart) && now.isBefore(voteEnd));
        vo.setMySubmitCount(0);
        vo.setMyVoteCount(0);
        return vo;
    }

    @Override
    public Long createActivity(Activity activity) {
        if (activity.getMaxSubmitPerUser() == null) {
            activity.setMaxSubmitPerUser(1);
        }
        if (activity.getMaxVotePerUser() == null) {
            activity.setMaxVotePerUser(10);
        }
        if (activity.getAllowSameWorkMultivote() == null) {
            activity.setAllowSameWorkMultivote(0);
        }
        if (activity.getWorkCount() == null) {
            activity.setWorkCount(0);
        }
        if (activity.getVoteCount() == null) {
            activity.setVoteCount(0);
        }
        if (activity.getViewCount() == null) {
            activity.setViewCount(0);
        }
        if (activity.getStatus() == null) {
            activity.setStatus(1);
        }
        this.save(activity);
        return activity.getId();
    }

    @Override
    public boolean updateActivity(Long id, Activity activity) {
        activity.setId(id);
        return this.updateById(activity);
    }

    @Override
    public boolean deleteActivity(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean incrementViewCount(Long id) {
        LambdaUpdateWrapper<Activity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Activity::getId, id)
                .setSql("view_count = view_count + 1");
        return this.baseMapper.update(null, updateWrapper) > 0;
    }
}
