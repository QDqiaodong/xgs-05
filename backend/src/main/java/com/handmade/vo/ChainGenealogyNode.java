package com.handmade.vo;

import com.handmade.entity.User;
import com.handmade.entity.Work;
import lombok.Data;

import java.util.List;

@Data
public class ChainGenealogyNode {
    private Long chainWorkId;
    private Long workId;
    private Long userId;
    private String workTitle;
    private String workCoverImage;
    private String userAvatar;
    private String username;
    private Integer chainLevel;
    private String inspirationRemark;
    private Integer branchCount;
    private Integer likeCount;
    private List<ChainGenealogyNode> children;
}
