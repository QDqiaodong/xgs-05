package com.handmade.strategy;

import com.handmade.entity.Work;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class HotScoreCalculator {

    private static final double VIEW_WEIGHT = 1.0;
    private static final double FAVORITE_WEIGHT = 4.0;
    private static final double LIKE_WEIGHT = 2.0;

    private static final double VIEW_HALF_LIFE_DAYS = 7.0;
    private static final double LIKE_HALF_LIFE_DAYS = 14.0;
    private static final double FAVORITE_HALF_LIFE_DAYS = 30.0;

    private static final double MIN_DECAY = 0.01;
    private static final double NEW_BOOST_DAYS = 3.0;
    private static final double NEW_BOOST_FACTOR = 1.5;
    private static final double LOG_BASE = 2.0;

    public double calculate(Work work) {
        if (work == null) {
            return 0.0;
        }

        LocalDateTime referenceTime = getReferenceTime(work);
        long days = referenceTime != null
                ? Duration.between(referenceTime, LocalDateTime.now()).toDays()
                : 365L;

        double viewScore = calculateViewScore(work, days);
        double favoriteScore = calculateFavoriteScore(work, days);
        double likeScore = calculateLikeScore(work, days);

        double totalScore = viewScore + favoriteScore + likeScore;

        if (days <= NEW_BOOST_DAYS) {
            double boostRatio = 1.0 + (NEW_BOOST_FACTOR - 1.0) * (1.0 - days / NEW_BOOST_DAYS);
            totalScore *= boostRatio;
        }

        return totalScore;
    }

    private double calculateViewScore(Work work, long days) {
        int viewCount = nullToZero(work.getViewCount());
        double decayedViews = applyDecay(viewCount, days, VIEW_HALF_LIFE_DAYS);
        return logScale(decayedViews) * VIEW_WEIGHT;
    }

    private double calculateFavoriteScore(Work work, long days) {
        int favoriteCount = nullToZero(work.getFavoriteCount());
        double decayedFavorites = applyDecay(favoriteCount, days, FAVORITE_HALF_LIFE_DAYS);
        return logScale(decayedFavorites) * FAVORITE_WEIGHT;
    }

    private double calculateLikeScore(Work work, long days) {
        int likeCount = nullToZero(work.getLikeCount());
        double decayedLikes = applyDecay(likeCount, days, LIKE_HALF_LIFE_DAYS);
        return logScale(decayedLikes) * LIKE_WEIGHT;
    }

    private double applyDecay(int count, long days, double halfLifeDays) {
        double decayFactor = Math.pow(0.5, days / halfLifeDays);
        return count * Math.max(decayFactor, MIN_DECAY);
    }

    private double logScale(double value) {
        return Math.log(value + 1.0) / Math.log(LOG_BASE) + 1.0;
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
