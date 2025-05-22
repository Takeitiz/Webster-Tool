package com.viettel.webstertool.dto.webster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StageOutputDTO {
    private Long stageId;
    private String oldId;
    private Integer greenTime;
    private Integer redClearTime;
    private Integer yellowTime;
}
