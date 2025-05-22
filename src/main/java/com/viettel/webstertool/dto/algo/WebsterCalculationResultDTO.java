package com.viettel.webstertool.dto.algo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebsterCalculationResultDTO {
    private Double optimalCycleLength; // seconds
    private Double totalLostTime; // seconds
    private Double totalFlowRatio; // sum of all critical flow ratios
    private List<PhaseResultDTO> phaseResults;
    private Boolean isOptimal; // whether the solution is within constraints
    private String message; // any warnings or notes about the calculation
}
