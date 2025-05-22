package com.viettel.webstertool.dto.webster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlowDataDTO {
    private Integer direction;
    private Integer route;
    private Double flowData;
}
