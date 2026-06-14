package com.handmade.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChainGenealogyVO {
    private Long chainActivityId;
    private Integer totalWorks;
    private Integer totalParticipants;
    private Integer maxDepth;
    private List<ChainGenealogyNode> roots;
}
