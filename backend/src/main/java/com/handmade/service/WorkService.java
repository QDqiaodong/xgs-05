package com.handmade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.Work;

import java.util.List;
import java.util.Map;

public interface WorkService extends IService<Work> {
    IPage<Work> getWorkList(Integer page, Integer size, Long categoryId, String keyword, Integer difficultyLevel);

    IPage<Work> getHotWorks(Integer page, Integer size);

    IPage<Work> getUserWorks(Long userId, Integer page, Integer size);

    boolean incrementViewCount(Long workId);

    boolean incrementFavoriteCount(Long workId, boolean increment);

    List<Map<String, Object>> suggestMaterials(String keyword, Integer limit);

    List<Work> getRecommendedWorks(Long workId, Integer limit);

    boolean setDifficultyLevel(Long workId, Integer difficultyLevel);
}
