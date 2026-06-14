package com.handmade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.Activity;
import com.handmade.vo.ActivityDetailVO;

public interface ActivityService extends IService<Activity> {
    IPage<Activity> getActivityList(Integer page, Integer size, Integer status);

    IPage<Activity> getOngoingActivities(Integer page, Integer size);

    ActivityDetailVO getActivityDetail(Long id);

    Long createActivity(Activity activity);

    boolean updateActivity(Long id, Activity activity);

    boolean deleteActivity(Long id);

    boolean incrementViewCount(Long id);
}
