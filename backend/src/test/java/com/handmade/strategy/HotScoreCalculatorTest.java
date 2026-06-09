package com.handmade.strategy;

import com.handmade.entity.Work;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HotScoreCalculatorTest {

    private HotScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HotScoreCalculator();
    }

    @Test
    @DisplayName("null作品返回0分")
    void testNullWork() {
        assertEquals(0.0, calculator.calculate(null));
    }

    @Test
    @DisplayName("新发布的高分作品 > 很久以前的高分作品")
    void testTimeDecayNewVsOld() {
        Work recent = new Work();
        recent.setViewCount(1000);
        recent.setFavoriteCount(100);
        recent.setLikeCount(200);
        recent.setCreateTime(LocalDateTime.now().minusDays(1));

        Work old = new Work();
        old.setViewCount(1000);
        old.setFavoriteCount(100);
        old.setLikeCount(200);
        old.setCreateTime(LocalDateTime.now().minusDays(30));

        double recentScore = calculator.calculate(recent);
        double oldScore = calculator.calculate(old);

        System.out.println("近期作品分数: " + recentScore);
        System.out.println("久远作品分数: " + oldScore);
        assertTrue(recentScore > oldScore, "近期作品分数应高于久远作品");
    }

    @Test
    @DisplayName("浏览量衰减快于收藏衰减")
    void testViewDecayFasterThanFavoriteDecay() {
        Work viewHeavy = new Work();
        viewHeavy.setViewCount(2000);
        viewHeavy.setFavoriteCount(0);
        viewHeavy.setLikeCount(0);
        viewHeavy.setCreateTime(LocalDateTime.now().minusDays(14));

        Work favoriteHeavy = new Work();
        favoriteHeavy.setViewCount(0);
        favoriteHeavy.setFavoriteCount(2000);
        favoriteHeavy.setLikeCount(0);
        favoriteHeavy.setCreateTime(LocalDateTime.now().minusDays(14));

        double viewScore = calculator.calculate(viewHeavy);
        double favScore = calculator.calculate(favoriteHeavy);

        System.out.println("浏览量主导分数: " + viewScore);
        System.out.println("收藏量主导分数: " + favScore);
    }

    @Test
    @DisplayName("新作品获得冷启动加分")
    void testNewWorkBoost() {
        Work day1 = new Work();
        day1.setViewCount(100);
        day1.setFavoriteCount(10);
        day1.setLikeCount(20);
        day1.setCreateTime(LocalDateTime.now().minusDays(1));

        Work day10 = new Work();
        day10.setViewCount(100);
        day10.setFavoriteCount(10);
        day10.setLikeCount(20);
        day10.setCreateTime(LocalDateTime.now().minusDays(10));

        double scoreDay1 = calculator.calculate(day1);
        double scoreDay10 = calculator.calculate(day10);

        System.out.println("发布1天作品分数: " + scoreDay1);
        System.out.println("发布10天作品分数: " + scoreDay10);
        assertTrue(scoreDay1 > scoreDay10, "新发布作品应有冷启动加分");
    }

    @Test
    @DisplayName("久远作品不会衰减到0")
    void testMinDecay() {
        Work veryOld = new Work();
        veryOld.setViewCount(10000);
        veryOld.setFavoriteCount(1000);
        veryOld.setLikeCount(500);
        veryOld.setCreateTime(LocalDateTime.now().minusYears(1));

        double score = calculator.calculate(veryOld);
        System.out.println("一年前作品分数: " + score);
        assertTrue(score > 0, "久远作品分数不应为0");
    }

    @Test
    @DisplayName("综合热度排行测试")
    void testOverallRanking() {
        Work hotNew = new Work();
        hotNew.setViewCount(500);
        hotNew.setFavoriteCount(50);
        hotNew.setLikeCount(100);
        hotNew.setCreateTime(LocalDateTime.now().minusDays(2));

        Work midOld = new Work();
        midOld.setViewCount(2000);
        midOld.setFavoriteCount(200);
        midOld.setLikeCount(400);
        midOld.setCreateTime(LocalDateTime.now().minusDays(60));

        Work oldClassic = new Work();
        oldClassic.setViewCount(10000);
        oldClassic.setFavoriteCount(1000);
        oldClassic.setLikeCount(2000);
        oldClassic.setCreateTime(LocalDateTime.now().minusDays(180));

        double scoreHotNew = calculator.calculate(hotNew);
        double scoreMidOld = calculator.calculate(midOld);
        double scoreClassic = calculator.calculate(oldClassic);

        System.out.println("热门新作品(2天, 分数: " + scoreHotNew);
        System.out.println("中等作品(60天): " + scoreMidOld);
        System.out.println("经典老作品(180天): " + scoreClassic);
    }
}
