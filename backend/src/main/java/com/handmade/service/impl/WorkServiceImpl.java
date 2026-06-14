package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.User;
import com.handmade.entity.Work;
import com.handmade.mapper.WorkMapper;
import com.handmade.service.UserService;
import com.handmade.service.WorkService;
import com.handmade.strategy.HotScoreCalculator;
import com.handmade.strategy.WorkSimilarityCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work> implements WorkService {

    private static final String HOT_WORKS_KEY = "hot:works";
    private static final String MATERIALS_CACHE_KEY = "materials:suggest";
    private static final String RECOMMEND_CACHE_KEY = "recommend:works:";
    private static final long CACHE_EXPIRE = 1;
    private static final long MATERIALS_CACHE_EXPIRE = 2;
    private static final long RECOMMEND_CACHE_EXPIRE = 2;
    private static final Pattern MATERIAL_SPLIT_PATTERN = Pattern.compile("[,，、;；\\n\\r]+");
    private static final int HOT_CANDIDATE_LIMIT = 200;
    private static final int RECOMMEND_CANDIDATE_LIMIT = 300;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private HotScoreCalculator hotScoreCalculator;

    @Autowired
    private WorkSimilarityCalculator workSimilarityCalculator;

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    public IPage<Work> getWorkList(Integer page, Integer size, Long categoryId, String keyword, Integer difficultyLevel) {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Work::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Work::getTitle, keyword).or().like(Work::getDescription, keyword);
        }
        if (difficultyLevel != null) {
            wrapper.eq(Work::getDifficultyLevel, difficultyLevel);
        }
        wrapper.eq(Work::getStatus, 1);
        wrapper.orderByDesc(Work::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public boolean setDifficultyLevel(Long workId, Integer difficultyLevel) {
        if (difficultyLevel != null && (difficultyLevel < 1 || difficultyLevel > 3)) {
            return false;
        }
        Work work = new Work();
        work.setId(workId);
        if (difficultyLevel == null) {
            work.setDifficultyLevel(null);
        } else {
            work.setDifficultyLevel(difficultyLevel);
        }
        boolean result = this.updateById(work);
        if (result) {
            clearWorkCaches();
        }
        return result;
    }

    @Override
    public void clearWorkCaches() {
        redisTemplate.delete(HOT_WORKS_KEY);
        redisTemplate.delete(MATERIALS_CACHE_KEY);
        Set<String> recommendKeys = redisTemplate.keys(RECOMMEND_CACHE_KEY + "*");
        if (recommendKeys != null && !recommendKeys.isEmpty()) {
            redisTemplate.delete(recommendKeys);
        }
    }

    @Override
    public IPage<Work> getHotWorks(Integer page, Integer size) {
        List<Work> hotWorks = (List<Work>) redisTemplate.opsForValue().get(HOT_WORKS_KEY);
        if (hotWorks != null && !hotWorks.isEmpty()) {
            return buildPagedResult(hotWorks, page, size);
        }

        List<Work> candidates = fetchHotCandidates();
        List<Work> sortedWorks = sortByHotScore(candidates);

        if (!sortedWorks.isEmpty()) {
            redisTemplate.opsForValue().set(HOT_WORKS_KEY, sortedWorks, CACHE_EXPIRE, TimeUnit.HOURS);
        }

        return buildPagedResult(sortedWorks, page, size);
    }

    private List<Work> fetchHotCandidates() {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getStatus, 1);
        wrapper.orderByDesc(Work::getCreateTime);
        wrapper.orderByDesc(Work::getId);
        wrapper.last("LIMIT " + HOT_CANDIDATE_LIMIT);
        return this.list(wrapper);
    }

    private List<Work> sortByHotScore(List<Work> works) {
        return works.stream()
                .sorted(Comparator.comparingLong(this::getHotScore)
                        .reversed()
                        .thenComparing(Work::getId, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private long getHotScore(Work work) {
        boolean isCertifiedCreator = false;
        if (work.getUserId() != null) {
            User creator = userService.getById(work.getUserId());
            if (creator != null && creator.getIsCertified() != null && creator.getIsCertified() == 1) {
                isCertifiedCreator = true;
            }
        }
        return hotScoreCalculator.calculate(work, isCertifiedCreator);
    }

    private IPage<Work> buildPagedResult(List<Work> allWorks, Integer page, Integer size) {
        Page<Work> result = new Page<>(page, size);
        int total = allWorks.size();
        result.setTotal(total);

        if (total == 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        if (fromIndex >= total) {
            result.setRecords(Collections.emptyList());
            return result;
        }
        result.setRecords(allWorks.subList(fromIndex, toIndex));
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
        boolean result = baseMapper.incrementViewCountAtomic(workId) > 0;
        if (result) {
            Work work = this.getById(workId);
            if (work != null && work.getUserId() != null) {
                userService.updateCreatorStats(work.getUserId());
            }
        }
        return result;
    }

    @Override
    public boolean incrementFavoriteCount(Long workId, boolean increment) {
        int delta = increment ? 1 : -1;
        boolean result = baseMapper.updateFavoriteCountAtomic(workId, delta) > 0;
        if (result) {
            Work work = this.getById(workId);
            if (work != null && work.getUserId() != null) {
                userService.updateCreatorStats(work.getUserId());
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> suggestMaterials(String keyword, Integer limit) {
        int maxLimit = limit != null ? limit : 10;
        Map<String, Integer> materialCountMap;

        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(MATERIALS_CACHE_KEY);
        if (cached != null && !cached.isEmpty()) {
            materialCountMap = new HashMap<>();
            for (Map<String, Object> item : cached) {
                materialCountMap.put((String) item.get("name"), (Integer) item.get("count"));
            }
        } else {
            materialCountMap = extractAllMaterials();
            List<Map<String, Object>> allMaterials = materialCountMap.entrySet().stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(100)
                    .map(entry -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("name", entry.getKey());
                        m.put("count", entry.getValue());
                        return m;
                    })
                    .collect(Collectors.toList());
            redisTemplate.opsForValue().set(MATERIALS_CACHE_KEY, allMaterials, MATERIALS_CACHE_EXPIRE, TimeUnit.HOURS);
        }

        List<Map<String, Object>> result = materialCountMap.entrySet().stream()
                .filter(entry -> {
                    String name = entry.getKey();
                    if (!StringUtils.hasText(keyword)) {
                        return true;
                    }
                    return name.toLowerCase().contains(keyword.toLowerCase());
                })
                .sorted((a, b) -> {
                    int countCompare = b.getValue() - a.getValue();
                    if (countCompare != 0) {
                        return countCompare;
                    }
                    if (StringUtils.hasText(keyword)) {
                        boolean aStartsWith = a.getKey().toLowerCase().startsWith(keyword.toLowerCase());
                        boolean bStartsWith = b.getKey().toLowerCase().startsWith(keyword.toLowerCase());
                        if (aStartsWith && !bStartsWith) return -1;
                        if (!aStartsWith && bStartsWith) return 1;
                    }
                    return a.getKey().compareTo(b.getKey());
                })
                .limit(maxLimit)
                .map(entry -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", entry.getKey());
                    m.put("count", entry.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        if (result.isEmpty() && StringUtils.hasText(keyword)) {
            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("name", keyword.trim());
            suggestion.put("count", 0);
            result.add(suggestion);
        }

        return result;
    }

    private Map<String, Integer> extractAllMaterials() {
        Map<String, Integer> countMap = new HashMap<>();

        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getStatus, 1);
        wrapper.isNotNull(Work::getMaterials);
        wrapper.ne(Work::getMaterials, "");
        List<Work> works = this.list(wrapper);

        for (Work work : works) {
            String materials = work.getMaterials();
            if (!StringUtils.hasText(materials)) {
                continue;
            }
            String[] parts = MATERIAL_SPLIT_PATTERN.split(materials);
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.length() >= 2 && trimmed.length() <= 50) {
                    countMap.merge(trimmed, 1, Integer::sum);
                }
            }
        }

        addDefaultMaterials(countMap);

        return countMap;
    }

    private void addDefaultMaterials(Map<String, Integer> countMap) {
        String[][] defaults = {
                {"纯羊毛线500g", "5"},
                {"棒针一副", "5"},
                {"钩针", "4"},
                {"亚克力毛线", "4"},
                {"棉花", "4"},
                {"陶土500g", "4"},
                {"釉料", "3"},
                {"麻布", "3"},
                {"纯棉布料", "3"},
                {"缝纫机线", "3"},
                {"松木", "3"},
                {"木雕刀", "3"},
                {"砂纸", "3"},
                {"木蜡油", "2"},
                {"绣线", "2"},
                {"绣绷", "2"},
                {"不织布", "2"},
                {"填充棉", "2"},
                {"剪刀", "2"},
                {"胶水", "2"}
        };
        for (String[] pair : defaults) {
            String name = pair[0];
            int count = Integer.parseInt(pair[1]);
            countMap.merge(name, count, (oldVal, newVal) -> Math.max(oldVal, newVal));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Work> getRecommendedWorks(Long workId, Integer limit) {
        if (workId == null) {
            return Collections.emptyList();
        }
        int maxLimit = limit != null ? limit : 8;

        String cacheKey = RECOMMEND_CACHE_KEY + workId + ":" + maxLimit;
        List<Work> cached = (List<Work>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        Work baseWork = this.getById(workId);
        if (baseWork == null || baseWork.getStatus() == null || baseWork.getStatus() != 1) {
            return Collections.emptyList();
        }

        List<Work> candidates = fetchRecommendCandidates(baseWork);
        List<Work> recommended = workSimilarityCalculator.rankBySimilarity(baseWork, candidates, maxLimit);

        if (!recommended.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, recommended, RECOMMEND_CACHE_EXPIRE, TimeUnit.HOURS);
        }

        return recommended;
    }

    private List<Work> fetchRecommendCandidates(Work baseWork) {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getStatus, 1);
        wrapper.ne(Work::getId, baseWork.getId());
        if (baseWork.getCategoryId() != null) {
            wrapper.and(w -> w.eq(Work::getCategoryId, baseWork.getCategoryId())
                    .or().isNotNull(Work::getMaterials)
                    .or().isNotNull(Work::getTitle));
        }
        wrapper.orderByDesc(Work::getCreateTime);
        wrapper.orderByDesc(Work::getId);
        wrapper.last("LIMIT " + RECOMMEND_CANDIDATE_LIMIT);
        return this.list(wrapper);
    }
}
