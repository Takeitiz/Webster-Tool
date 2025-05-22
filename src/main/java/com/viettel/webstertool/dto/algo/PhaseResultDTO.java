package com.viettel.webstertool.dto.algo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhaseResultDTO {
    private Integer phaseId;
    private String phaseName;
    private Integer greenTime; // seconds
    private Integer yellowTime; // seconds
    private Integer allRedTime; // seconds
    private Integer totalPhaseTime; // green + yellow + all-red
    private Double flowRatio; // critical flow / saturation flow
    private Double degreeOfSaturation; // flow ratio for this phase
}
