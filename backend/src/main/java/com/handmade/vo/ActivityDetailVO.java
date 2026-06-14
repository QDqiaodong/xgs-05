package com.handmade.vo;

import com.handmade.entity.Activity;
import lombok.Data;

@Data
public class ActivityDetailVO {
    private Activity activity;
    private Boolean isOngoing;
    private Boolean isVoting;
    private Integer mySubmitCount;
    private Integer myVoteCount;
}
