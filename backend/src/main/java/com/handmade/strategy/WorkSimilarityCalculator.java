package com.handmade.strategy;

import com.handmade.entity.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class WorkSimilarityCalculator {

    private static final double CATEGORY_WEIGHT = 0.35;
    private static final double MATERIAL_WEIGHT = 0.30;
    private static final double TEXT_WEIGHT = 0.25;
    private static final double HOT_BONUS_WEIGHT = 0.10;

    private static final double MIN_SCORE = 0.0;
    private static final long SCORE_SCALE = 10_000_000L;

    private static final Pattern MATERIAL_SPLIT_PATTERN = Pattern.compile("[,，、;；\\n\\r\\s]+");
    private static final Pattern TEXT_SPLIT_PATTERN = Pattern.compile("[\\s\\p{Punct}，。！？、；：\"''（）【】《》…—]+");

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "和", "是", "就", "都", "而", "及", "与", "着",
            "或", "一个", "没有", "我们", "你们", "他们", "她们", "它们",
            "这个", "那个", "这些", "那些", "自己", "可以", "可能", "应该",
            "非常", "很", "更", "最", "也", "还", "又", "再", "但", "但是",
            "然而", "不过", "就是", "这样", "那样", "如何", "什么", "怎么",
            "作品", "手工", "制作", "做", "用", "在", "有", "我", "你", "他",
            "她", "它", "为", "对", "从", "到", "把", "被", "给", "让",
            "来", "去", "上", "下", "里", "外", "中", "内", "前", "后"
    ));

    @Autowired
    private HotScoreCalculator hotScoreCalculator;

    public long calculate(Work baseWork, Work candidateWork) {
        if (baseWork == null || candidateWork == null) {
            return MIN_SCORE;
        }
        if (baseWork.getId() != null && baseWork.getId().equals(candidateWork.getId())) {
            return MIN_SCORE;
        }

        double categoryScore = calculateCategoryScore(baseWork, candidateWork);
        double materialScore = calculateMaterialScore(baseWork, candidateWork);
        double textScore = calculateTextScore(baseWork, candidateWork);
        double hotBonus = calculateHotBonus(candidateWork);

        double totalScore = categoryScore * CATEGORY_WEIGHT
                + materialScore * MATERIAL_WEIGHT
                + textScore * TEXT_WEIGHT
                + hotBonus * HOT_BONUS_WEIGHT;

        return (long) (totalScore * SCORE_SCALE);
    }

    public List<Work> rankBySimilarity(Work baseWork, List<Work> candidates, int limit) {
        if (baseWork == null || candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        return candidates.stream()
                .filter(c -> !baseWork.getId().equals(c.getId()))
                .sorted(Comparator.comparingLong((Work w) -> this.calculate(baseWork, w))
                        .reversed()
                        .thenComparing(Work::getId, Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private double calculateCategoryScore(Work a, Work b) {
        Long catA = a.getCategoryId();
        Long catB = b.getCategoryId();
        if (catA == null || catB == null) {
            return 0.0;
        }
        return catA.equals(catB) ? 1.0 : 0.0;
    }

    private double calculateMaterialScore(Work a, Work b) {
        Set<String> materialsA = extractMaterials(a.getMaterials());
        Set<String> materialsB = extractMaterials(b.getMaterials());

        if (materialsA.isEmpty() || materialsB.isEmpty()) {
            return 0.0;
        }

        return jaccardSimilarity(materialsA, materialsB);
    }

    private double calculateTextScore(Work a, Work b) {
        Set<String> tokensA = extractTextTokens(a);
        Set<String> tokensB = extractTextTokens(b);

        if (tokensA.isEmpty() || tokensB.isEmpty()) {
            return 0.0;
        }

        return jaccardSimilarity(tokensA, tokensB);
    }

    private double calculateHotBonus(Work work) {
        long hotScore = hotScoreCalculator.calculate(work);
        if (hotScore <= 0) {
            return 0.0;
        }
        double normalized = Math.log(hotScore + 1.0) / Math.log(10.0);
        return Math.min(normalized / 8.0, 1.0);
    }

    private Set<String> extractMaterials(String materialsStr) {
        if (!StringUtils.hasText(materialsStr)) {
            return Collections.emptySet();
        }
        return Arrays.stream(MATERIAL_SPLIT_PATTERN.split(materialsStr))
                .map(String::trim)
                .filter(s -> s.length() >= 2 && s.length() <= 50)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private Set<String> extractTextTokens(Work work) {
        Set<String> tokens = new HashSet<>();

        addTextTokens(tokens, work.getTitle(), 2, 20);
        addTextTokens(tokens, work.getDescription(), 2, 30);
        addTextTokens(tokens, work.getCreationIdea(), 2, 30);

        return tokens;
    }

    private void addTextTokens(Set<String> tokens, String text, int minLen, int maxLen) {
        if (!StringUtils.hasText(text)) {
            return;
        }
        String[] parts = TEXT_SPLIT_PATTERN.split(text);
        for (String part : parts) {
            String trimmed = part.trim().toLowerCase();
            if (trimmed.length() >= minLen && trimmed.length() <= maxLen
                    && !STOP_WORDS.contains(trimmed)) {
                tokens.add(trimmed);
            }
        }
        addNGrams(tokens, text, 2);
    }

    private void addNGrams(Set<String> tokens, String text, int n) {
        if (!StringUtils.hasText(text)) {
            return;
        }
        String clean = text.replaceAll("[\\s\\p{Punct}]", "");
        for (int i = 0; i <= clean.length() - n; i++) {
            String gram = clean.substring(i, i + n);
            if (!STOP_WORDS.contains(gram)) {
                tokens.add(gram);
            }
        }
    }

    private double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        if (union.isEmpty()) {
            return 0.0;
        }
        return (double) intersection.size() / union.size();
    }
}
