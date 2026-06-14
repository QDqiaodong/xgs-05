package com.handmade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.ActivityWork;
import com.handmade.vo.ActivityWorkVO;

public interface ActivityWorkService extends IService<ActivityWork> {
    IPage<ActivityWorkVO> getActivityWorks(Long activityId, Integer page, Integer size, String sortBy);

    IPage<ActivityWorkVO> getMySubmissions(Long userId, Integer page, Integer size);

    Long submitWork(Long activityId, Long workId, Long userId, String submitRemark);

    boolean auditWork(Long id, Integer auditStatus, String auditRemark, Long auditorId);

    boolean incrementVoteCount(Long activityWorkId, int count);

    int getUserSubmitCount(Long activityId, Long userId);
}
