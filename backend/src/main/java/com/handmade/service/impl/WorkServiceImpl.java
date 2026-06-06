package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.Work;
import com.handmade.mapper.WorkMapper;
import com.handmade.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work> implements WorkService {

    private static final String HOT_WORDS_KEY = "hot:works";
    private static final long CACHE_EXPIRE = 1;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public IPage<Work> getWorkList(Integer page, Integer size, Long categoryId, String keyword) {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Work::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Work::getTitle, keyword).or().like(Work::getDescription, keyword);
        }
        wrapper.eq(Work::getStatus, 1);
        wrapper.orderByDesc(Work::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<Work> getHotWorks(Integer page, Integer size) {
        List<Work> hotWorks = (List<Work>) redisTemplate.opsForValue().get(HOT_WORDS_KEY);
        if (hotWorks != null && !hotWorks.isEmpty()) {
            Page<Work> result = new Page<>(page, size);
            result.setRecords(hotWorks);
            result.setTotal(hotWorks.size());
            return result;
        }

        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getStatus, 1);
        wrapper.eq(Work::getIsHot, 1);
        wrapper.orderByDesc(Work::getViewCount);
        IPage<Work> result = this.page(new Page<>(page, size), wrapper);

        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            redisTemplate.opsForValue().set(HOT_WORDS_KEY, result.getRecords(), CACHE_EXPIRE, TimeUnit.HOURS);
        }
        return result;
    }

    @Override
    public IPage<Work> getUserWorks(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getUserId, userId);
        wrapper.eq(Work::getStatus, 1);
        wrapper.orderByDesc(Work::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public boolean incrementViewCount(Long workId) {
        Work work = this.getById(workId);
        if (work != null) {
            work.setViewCount(work.getViewCount() + 1);
            return this.updateById(work);
        }
        return false;
    }

    @Override
    public boolean incrementFavoriteCount(Long workId, boolean increment) {
        Work work = this.getById(workId);
        if (work != null) {
            int count = increment ? 1 : -1;
            work.setFavoriteCount(Math.max(0, work.getFavoriteCount() + count));
            return this.updateById(work);
        }
        return false;
    }
}
