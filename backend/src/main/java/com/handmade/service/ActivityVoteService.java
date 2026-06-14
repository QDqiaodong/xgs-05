package com.handmade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.ActivityVote;

public interface ActivityVoteService extends IService<ActivityVote> {
    boolean vote(Long activityId, Long activityWorkId, Long workId, Long userId, Integer voteCount);

    int getUserVoteCount(Long activityId, Long userId);

    int getUserWorkVoteCount(Long activityId, Long activityWorkId, Long userId);

    boolean hasVotedWork(Long activityId, Long activityWorkId, Long userId);
}
