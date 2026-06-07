package com.handmade.strategy;

import com.handmade.entity.Work;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class HotScoreCalculator {

    private static final double VIEW_WEIGHT = 1.0;
    private static final double FAVORITE_WEIGHT = 5.0;
    private static final double LIKE_WEIGHT = 3.0;
    private static final double DECAY_FACTOR = 0.05;
    private static final double MIN_DECAY = 0.01;

    public double calculate(Work work) {
        if (work == null) {
            return 0.0;
        }

        double baseScore = calculateBaseScore(work);
        double decayFactor = calculateDecayFactor(work);
        return baseScore * decayFactor;
    }

    private double calculateBaseScore(Work work) {
        int viewCount = nullToZero(work.getViewCount());
        int favoriteCount = nullToZero(work.getFavoriteCount());
        int likeCount = nullToZero(work.getLikeCount());

        return viewCount * VIEW_WEIGHT
                + favoriteCount * FAVORITE_WEIGHT
                + likeCount * LIKE_WEIGHT;
    }

    private double calculateDecayFactor(Work work) {
        LocalDateTime referenceTime = getReferenceTime(work);
        if (referenceTime == null) {
            return MIN_DECAY;
        }

        long days = Duration.between(referenceTime, LocalDateTime.now()).toDays();
        double decay = Math.exp(-DECAY_FACTOR * days);
        return Math.max(decay, MIN_DECAY);
    }

    private LocalDateTime getReferenceTime(Work work) {
        LocalDateTime updateTime = work.getUpdateTime();
        LocalDateTime createTime = work.getCreateTime();

        if (updateTime != null) {
            return updateTime;
        }
        return createTime;
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
