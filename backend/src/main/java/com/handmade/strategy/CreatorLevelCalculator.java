package com.handmade.strategy;

import com.handmade.entity.User;
import com.handmade.entity.Work;
import com.handmade.service.WorkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreatorLevelCalculator {

    private static final double WORK_WEIGHT = 10.0;
    private static final double VIEW_WEIGHT = 0.1;
    private static final double FAVORITE_WEIGHT = 1.0;
    private static final double LIKE_WEIGHT = 2.0;

    private static final int LEVEL_APPRENTICE = 1;
    private static final int LEVEL_CRAFTSMAN = 2;
    private static final int LEVEL_SKILLED_CRAFTSMAN = 3;
    private static final int LEVEL_MASTER_CRAFTSMAN = 4;
    private static final int LEVEL_GRAND_MASTER = 5;

    private static final int SCORE_APPRENTICE_MAX = 99;
    private static final int SCORE_CRAFTSMAN_MAX = 499;
    private static final int SCORE_SKILLED_MAX = 1999;
    private static final int SCORE_MASTER_MAX = 4999;

    @Autowired
    private WorkService workService;

    public int calculateScore(Long userId) {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getUserId, userId);
        wrapper.eq(Work::getStatus, 1);
        List<Work> works = workService.list(wrapper);

        if (works == null || works.isEmpty()) {
            return 0;
        }

        int totalWorkCount = works.size();
        int totalViewCount = works.stream().mapToInt(w -> w.getViewCount() != null ? w.getViewCount() : 0).sum();
        int totalFavoriteCount = works.stream().mapToInt(w -> w.getFavoriteCount() != null ? w.getFavoriteCount() : 0).sum();
        int totalLikeCount = works.stream().mapToInt(w -> w.getLikeCount() != null ? w.getLikeCount() : 0).sum();

        return calculateScore(totalWorkCount, totalViewCount, totalFavoriteCount, totalLikeCount);
    }

    public int calculateScore(int workCount, int viewCount, int favoriteCount, int likeCount) {
        double score = workCount * WORK_WEIGHT
                + viewCount * VIEW_WEIGHT
                + favoriteCount * FAVORITE_WEIGHT
                + likeCount * LIKE_WEIGHT;
        return (int) Math.floor(score);
    }

    public int determineLevel(int score) {
        if (score <= SCORE_APPRENTICE_MAX) {
            return LEVEL_APPRENTICE;
        } else if (score <= SCORE_CRAFTSMAN_MAX) {
            return LEVEL_CRAFTSMAN;
        } else if (score <= SCORE_SKILLED_MAX) {
            return LEVEL_SKILLED_CRAFTSMAN;
        } else if (score <= SCORE_MASTER_MAX) {
            return LEVEL_MASTER_CRAFTSMAN;
        } else {
            return LEVEL_GRAND_MASTER;
        }
    }

    public int getNextLevelScore(int currentLevel) {
        switch (currentLevel) {
            case LEVEL_APPRENTICE:
                return SCORE_APPRENTICE_MAX + 1;
            case LEVEL_CRAFTSMAN:
                return SCORE_CRAFTSMAN_MAX + 1;
            case LEVEL_SKILLED_CRAFTSMAN:
                return SCORE_SKILLED_MAX + 1;
            case LEVEL_MASTER_CRAFTSMAN:
                return SCORE_MASTER_MAX + 1;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public String getLevelName(int level) {
        switch (level) {
            case LEVEL_APPRENTICE:
                return "学徒";
            case LEVEL_CRAFTSMAN:
                return "匠人";
            case LEVEL_SKILLED_CRAFTSMAN:
                return "熟练匠人";
            case LEVEL_MASTER_CRAFTSMAN:
                return "工艺师";
            case LEVEL_GRAND_MASTER:
                return "工艺大师";
            default:
                return "学徒";
        }
    }
}
