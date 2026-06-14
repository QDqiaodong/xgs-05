package com.handmade.vo;

import com.handmade.entity.ChainActivity;
import lombok.Data;

@Data
public class ChainActivityDetailVO {
    private ChainActivity chainActivity;
    private Boolean isOngoing;
    private Integer mySubmitCount;
}
