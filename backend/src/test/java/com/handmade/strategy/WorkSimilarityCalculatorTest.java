package com.handmade.strategy;

import com.handmade.entity.Work;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkSimilarityCalculatorTest {

    @Mock
    private HotScoreCalculator hotScoreCalculator;

    @InjectMocks
    private WorkSimilarityCalculator calculator;

    private Work baseWork;

    @BeforeEach
    void setUp() {
        baseWork = new Work();
        baseWork.setId(1L);
        baseWork.setCategoryId(1L);
        baseWork.setTitle("手工编织羊毛围巾教程");
        baseWork.setDescription("这款围巾采用纯羊毛线手工编织，温暖舒适，适合秋冬季节佩戴。");
        baseWork.setMaterials("纯羊毛线500g,棒针一副,剪刀");
        baseWork.setCreationIdea("想做一款温暖又时尚的围巾，选用优质羊毛线，简单的平针针法即可完成。");
        baseWork.setViewCount(1000);
        baseWork.setFavoriteCount(100);
        baseWork.setLikeCount(200);
        baseWork.setCreateTime(LocalDateTime.now().minusDays(5));

        when(hotScoreCalculator.calculate(org.mockito.ArgumentMatchers.any(Work.class)))
                .thenReturn(1000000L);
    }

    @Test
    @DisplayName("null作品返回0分")
    void testNullWork() {
        assertEquals(0L, calculator.calculate(null, new Work()));
        assertEquals(0L, calculator.calculate(new Work(), null));
        assertEquals(0L, calculator.calculate(null, null));
    }

    @Test
    @DisplayName("相同ID作品返回0分 - 排除自身")
    void testSameWorkExcluded() {
        Work same = new Work();
        same.setId(1L);
        assertEquals(0L, calculator.calculate(baseWork, same));
    }

    @Test
    @DisplayName("同分类作品得分高于不同分类作品")
    void testCategoryMatching() {
        Work sameCategory = new Work();
        sameCategory.setId(2L);
        sameCategory.setCategoryId(1L);
        sameCategory.setTitle("其他作品");
        sameCategory.setMaterials("材料A,材料B");

        Work diffCategory = new Work();
        diffCategory.setId(3L);
        diffCategory.setCategoryId(2L);
        diffCategory.setTitle("其他作品");
        diffCategory.setMaterials("材料A,材料B");

        long sameScore = calculator.calculate(baseWork, sameCategory);
        long diffScore = calculator.calculate(baseWork, diffCategory);

        System.out.println("同分类分数: " + sameScore);
        System.out.println("不同分类分数: " + diffScore);
        assertTrue(sameScore > diffScore, "同分类作品得分应更高");
    }

    @Test
    @DisplayName("相同用料关键词作品得分更高")
    void testMaterialMatching() {
        Work sameMaterials = new Work();
        sameMaterials.setId(2L);
        sameMaterials.setCategoryId(2L);
        sameMaterials.setTitle("其他编织作品");
        sameMaterials.setMaterials("纯羊毛线500g,棒针一副,毛线针");
        sameMaterials.setDescription("其他描述");

        Work diffMaterials = new Work();
        diffMaterials.setId(3L);
        diffMaterials.setCategoryId(2L);
        diffMaterials.setTitle("陶艺作品");
        diffMaterials.setMaterials("陶土500g,釉料,转盘");
        diffMaterials.setDescription("其他描述");

        long sameScore = calculator.calculate(baseWork, sameMaterials);
        long diffScore = calculator.calculate(baseWork, diffMaterials);

        System.out.println("相同用料分数: " + sameScore);
        System.out.println("不同用料分数: " + diffScore);
        assertTrue(sameScore > diffScore, "用料相似作品得分应更高");
    }

    @Test
    @DisplayName("标题描述相似作品得分更高")
    void testTextSimilarity() {
        Work similarText = new Work();
        similarText.setId(2L);
        similarText.setCategoryId(2L);
        similarText.setTitle("手工编织毛衣教程");
        similarText.setDescription("这款毛衣采用毛线手工编织，适合秋冬季节");
        similarText.setMaterials("材料A");

        Work diffText = new Work();
        diffText.setId(3L);
        diffText.setCategoryId(2L);
        diffText.setTitle("陶瓷花瓶制作");
        diffText.setDescription("陶艺作品，手拉坯成型，上釉烧制");
        diffText.setMaterials("材料B");

        long similarScore = calculator.calculate(baseWork, similarText);
        long diffScore = calculator.calculate(baseWork, diffText);

        System.out.println("相似文本分数: " + similarScore);
        System.out.println("不同文本分数: " + diffScore);
        assertTrue(similarScore > diffScore, "文本相似作品得分应更高");
    }

    @Test
    @DisplayName("综合相似度排名测试 - 全维度匹配排第一")
    void testOverallRanking() {
        Work perfectMatch = new Work();
        perfectMatch.setId(2L);
        perfectMatch.setCategoryId(1L);
        perfectMatch.setTitle("手工编织羊毛帽子教程");
        perfectMatch.setDescription("这款帽子采用纯羊毛线编织，温暖舒适，秋冬必备");
        perfectMatch.setMaterials("纯羊毛线500g,棒针一副,剪刀");
        perfectMatch.setCreationIdea("秋冬季节的温暖编织作品");

        Work mediumMatch = new Work();
        mediumMatch.setId(3L);
        mediumMatch.setCategoryId(1L);
        mediumMatch.setTitle("布艺手提包制作");
        mediumMatch.setDescription("纯棉布料手工缝制，日常百搭");
        mediumMatch.setMaterials("纯棉布料,缝纫机线,剪刀");

        Work lowMatch = new Work();
        lowMatch.setId(4L);
        lowMatch.setCategoryId(2L);
        lowMatch.setTitle("木雕摆件创作");
        lowMatch.setDescription("松木雕刻，传统木雕工艺");
        lowMatch.setMaterials("松木,木雕刀,砂纸,木蜡油");

        List<Work> candidates = new ArrayList<>();
        candidates.add(lowMatch);
        candidates.add(perfectMatch);
        candidates.add(mediumMatch);

        List<Work> ranked = calculator.rankBySimilarity(baseWork, candidates, 3);

        assertEquals(2L, ranked.get(0).getId(), "全维度匹配应排第一");
        assertEquals(3L, ranked.get(1).getId(), "分类相同排第二");
        assertEquals(4L, ranked.get(2).getId(), "匹配度最低排第三");

        System.out.println("=== 综合相似度排名 ===");
        for (Work w : ranked) {
            long score = calculator.calculate(baseWork, w);
            System.out.println("作品ID=" + w.getId() + ", 标题=" + w.getTitle() + ", 分数=" + score);
        }
    }

    @Test
    @DisplayName("rankBySimilarity返回不超过limit数量")
    void testRankLimit() {
        List<Work> candidates = new ArrayList<>();
        for (long i = 2; i <= 20; i++) {
            Work w = new Work();
            w.setId(i);
            w.setCategoryId(1L);
            w.setTitle("作品" + i);
            w.setMaterials("纯羊毛线500g");
            candidates.add(w);
        }

        List<Work> result = calculator.rankBySimilarity(baseWork, candidates, 5);
        assertEquals(5, result.size(), "返回数量不应超过limit");
    }

    @Test
    @DisplayName("rankBySimilarity排除baseWork自身")
    void testRankExcludesSelf() {
        List<Work> candidates = new ArrayList<>();
        candidates.add(baseWork);

        Work other = new Work();
        other.setId(2L);
        other.setCategoryId(1L);
        other.setTitle("其他作品");
        other.setMaterials("纯羊毛线500g");
        candidates.add(other);

        List<Work> result = calculator.rankBySimilarity(baseWork, candidates, 10);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId(), "应排除baseWork自身");
    }

    @Test
    @DisplayName("同分作品按ID降序排列 - 排序稳定性")
    void testSameScoreSortedByIdDesc() {
        Work a = new Work();
        a.setId(10L);
        a.setCategoryId(99L);
        a.setTitle("作品A");
        a.setMaterials("材料X");
        a.setDescription("描述A");

        Work b = new Work();
        b.setId(20L);
        b.setCategoryId(99L);
        b.setTitle("作品B");
        b.setMaterials("材料Y");
        b.setDescription("描述B");

        Work c = new Work();
        c.setId(15L);
        c.setCategoryId(99L);
        c.setTitle("作品C");
        c.setMaterials("材料Z");
        c.setDescription("描述C");

        List<Work> candidates = new ArrayList<>();
        candidates.add(a);
        candidates.add(b);
        candidates.add(c);

        List<Work> ranked = calculator.rankBySimilarity(baseWork, candidates, 10);

        assertEquals(20L, ranked.get(0).getId(), "同分应按ID降序，最大ID排第一");
        assertEquals(15L, ranked.get(1).getId(), "同分应按ID降序，中间ID排第二");
        assertEquals(10L, ranked.get(2).getId(), "同分应按ID降序，最小ID排第三");
    }

    @Test
    @DisplayName("空候选列表返回空结果")
    void testEmptyCandidates() {
        assertTrue(calculator.rankBySimilarity(baseWork, null, 10).isEmpty());
        assertTrue(calculator.rankBySimilarity(baseWork, new ArrayList<>(), 10).isEmpty());
        assertTrue(calculator.rankBySimilarity(null, new ArrayList<>(), 10).isEmpty());
    }
}
