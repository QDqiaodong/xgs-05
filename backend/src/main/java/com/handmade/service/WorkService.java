package com.handmade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.Work;

public interface WorkService extends IService<Work> {
    IPage<Work> getWorkList(Integer page, Integer size, Long categoryId, String keyword);

    IPage<Work> getHotWorks(Integer page, Integer size);

    IPage<Work> getUserWorks(Long userId, Integer page, Integer size);

    boolean incrementViewCount(Long workId);

    boolean incrementFavoriteCount(Long workId, boolean increment);
}
