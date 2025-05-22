package com.viettel.webstertool.dto.webster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VolumesDTO {
    private Long id;
    private Integer direction;
    private Double flowData;
    private Double straightProportion;
    private Double leftProportion;
    private Double rightProportion;
}
