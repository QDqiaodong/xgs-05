package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.Activity;
import com.handmade.entity.ActivityVote;
import com.handmade.entity.ActivityWork;
import com.handmade.mapper.ActivityVoteMapper;
import com.handmade.service.ActivityService;
import com.handmade.service.ActivityVoteService;
import com.handmade.service.ActivityWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityVoteServiceImpl extends ServiceImpl<ActivityVoteMapper, ActivityVote> implements ActivityVoteService {

    @Autowired
    @Lazy
    private ActivityService activityService;

    @Autowired
    @Lazy
    private ActivityWorkService activityWorkService;

    @Override
    @Transactional
    public boolean vote(Long activityId, Long activityWorkId, Long workId, Long userId, Integer voteCount) {
        if (voteCount == null || voteCount < 1) {
            voteCount = 1;
        }
        Activity activity = activityService.getById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime voteStart = activity.getVoteStartTime() != null ? activity.getVoteStartTime() : activity.getStartTime();
        LocalDateTime voteEnd = activity.getVoteEndTime() != null ? activity.getVoteEndTime() : activity.getEndTime();
        if (now.isBefore(voteStart) || now.isAfter(voteEnd)) {
            return false;
        }
        ActivityWork activityWork = activityWorkService.getById(activityWorkId);
        if (activityWork == null || !activityWork.getActivityId().equals(activityId) || activityWork.getAuditStatus() != 1) {
            return false;
        }
        int userVoteCount = getUserVoteCount(activityId, userId);
        if (activity.getMaxVotePerUser() != null && userVoteCount + voteCount > activity.getMaxVotePerUser()) {
            return false;
        }
        if (activity.getAllowSameWorkMultivote() == null || activity.getAllowSameWorkMultivote() == 0) {
            if (hasVotedWork(activityId, activityWorkId, userId)) {
                return false;
            }
        }
        ActivityVote vote = new ActivityVote();
        vote.setActivityId(activityId);
        vote.setActivityWorkId(activityWorkId);
        vote.setWorkId(workId);
        vote.setUserId(userId);
        vote.setVoteCount(voteCount);
        vote.setVoteTime(now);
        this.save(vote);
        activityWorkService.incrementVoteCount(activityWorkId, voteCount);
        activity.setVoteCount(activity.getVoteCount() + voteCount);
        activityService.updateById(activity);
        return true;
    }

    @Override
    public int getUserVoteCount(Long activityId, Long userId) {
        LambdaQueryWrapper<ActivityVote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityVote::getActivityId, activityId);
        wrapper.eq(ActivityVote::getUserId, userId);
        List<ActivityVote> votes = this.list(wrapper);
        int total = 0;
        for (ActivityVote v : votes) {
            total += v.getVoteCount() != null ? v.getVoteCount() : 0;
        }
        return total;
    }

    @Override
    public int getUserWorkVoteCount(Long activityId, Long activityWorkId, Long userId) {
        LambdaQueryWrapper<ActivityVote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityVote::getActivityId, activityId);
        wrapper.eq(ActivityVote::getActivityWorkId, activityWorkId);
        wrapper.eq(ActivityVote::getUserId, userId);
        List<ActivityVote> votes = this.list(wrapper);
        int total = 0;
        for (ActivityVote v : votes) {
            total += v.getVoteCount() != null ? v.getVoteCount() : 0;
        }
        return total;
    }

    @Override
    public boolean hasVotedWork(Long activityId, Long activityWorkId, Long userId) {
        LambdaQueryWrapper<ActivityVote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityVote::getActivityId, activityId);
        wrapper.eq(ActivityVote::getActivityWorkId, activityWorkId);
        wrapper.eq(ActivityVote::getUserId, userId);
        return this.count(wrapper) > 0;
    }
}
