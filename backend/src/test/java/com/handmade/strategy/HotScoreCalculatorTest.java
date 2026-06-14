package com.handmade.strategy;

import com.handmade.entity.Work;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        assertEquals(0L, calculator.calculate(null));
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

        long recentScore = calculator.calculate(recent);
        long oldScore = calculator.calculate(old);

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

        long viewScore = calculator.calculate(viewHeavy);
        long favScore = calculator.calculate(favoriteHeavy);

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

        long scoreDay1 = calculator.calculate(day1);
        long scoreDay10 = calculator.calculate(day10);

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

        long score = calculator.calculate(veryOld);
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

        long scoreHotNew = calculator.calculate(hotNew);
        long scoreMidOld = calculator.calculate(midOld);
        long scoreClassic = calculator.calculate(oldClassic);

        System.out.println("热门新作品(2天, 分数: " + scoreHotNew);
        System.out.println("中等作品(60天): " + scoreMidOld);
        System.out.println("经典老作品(180天): " + scoreClassic);
    }

    @Test
    @DisplayName("相同指标的作品分数相同 - 消除浮点抖动")
    void testIdenticalWorksSameScore() {
        LocalDateTime sameTime = LocalDateTime.now().minusDays(5);

        Work a = new Work();
        a.setId(1L);
        a.setViewCount(500);
        a.setFavoriteCount(50);
        a.setLikeCount(100);
        a.setCreateTime(sameTime);

        Work b = new Work();
        b.setId(2L);
        b.setViewCount(500);
        b.setFavoriteCount(50);
        b.setLikeCount(100);
        b.setCreateTime(sameTime);

        assertEquals(calculator.calculate(a), calculator.calculate(b),
                "相同指标的作品应返回相同分数");
    }

    @Test
    @DisplayName("同分作品按ID降序排列 - 排序稳定性")
    void testSameScoreSortedByIdDesc() {
        LocalDateTime sameTime = LocalDateTime.now().minusDays(10);

        Work work1 = new Work();
        work1.setId(100L);
        work1.setViewCount(200);
        work1.setFavoriteCount(20);
        work1.setLikeCount(40);
        work1.setCreateTime(sameTime);

        Work work2 = new Work();
        work2.setId(200L);
        work2.setViewCount(200);
        work2.setFavoriteCount(20);
        work2.setLikeCount(40);
        work2.setCreateTime(sameTime);

        Work work3 = new Work();
        work3.setId(150L);
        work3.setViewCount(200);
        work3.setFavoriteCount(20);
        work3.setLikeCount(40);
        work3.setCreateTime(sameTime);

        List<Work> works = new ArrayList<>();
        works.add(work1);
        works.add(work2);
        works.add(work3);

        List<Work> sorted = works.stream()
                .sorted(Comparator.comparingLong((Work work) -> calculator.calculate(work))
                        .reversed()
                        .thenComparing(Work::getId, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        assertEquals(200L, sorted.get(0).getId(), "同分应按ID降序，最大ID排第一");
        assertEquals(150L, sorted.get(1).getId(), "同分应按ID降序，中间ID排第二");
        assertEquals(100L, sorted.get(2).getId(), "同分应按ID降序，最小ID排第三");
    }

    @Test
    @DisplayName("多次排序结果一致 - 排序幂等性")
    void testSortingIdempotency() {
        List<Work> works = generateTestWorks(20);

        Comparator<Work> stableComparator = Comparator
                .comparingLong((Work work) -> calculator.calculate(work)).reversed()
                .thenComparing(Work::getId, Comparator.reverseOrder());

        List<Work> sorted1 = works.stream().sorted(stableComparator).collect(Collectors.toList());
        List<Work> sorted2 = works.stream().sorted(stableComparator).collect(Collectors.toList());
        List<Work> sorted3 = works.stream().sorted(stableComparator).collect(Collectors.toList());

        for (int i = 0; i < works.size(); i++) {
            assertEquals(sorted1.get(i).getId(), sorted2.get(i).getId(),
                    "第" + i + "个位置两次排序结果不一致");
            assertEquals(sorted2.get(i).getId(), sorted3.get(i).getId(),
                    "第" + i + "个位置两次排序结果不一致");
        }
    }

    private List<Work> generateTestWorks(int count) {
        List<Work> works = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(5);
        for (long i = 1; i <= count; i++) {
            Work w = new Work();
            w.setId(i);
            w.setViewCount((int) (i * 100 % 500));
            w.setFavoriteCount((int) (i * 10 % 50));
            w.setLikeCount((int) (i * 20 % 100));
            w.setCreateTime(baseTime.minusHours(i % 24));
            works.add(w);
        }
        return works;
    }
}
