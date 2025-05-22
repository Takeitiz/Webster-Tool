package com.viettel.webstertool.dto.webster;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoadDTO {
    private Integer direction;
    private Integer numberOfLane;
    private List<@Valid FlowDataDTO> flows;
}
