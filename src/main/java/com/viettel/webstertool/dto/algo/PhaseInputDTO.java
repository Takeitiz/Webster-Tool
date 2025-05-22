package com.viettel.webstertool.dto.algo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhaseInputDTO {
    @NotNull(message = "Phase ID is required")
    private Integer phaseId;

    @NotNull(message = "Phase name is required")
    private String phaseName;

    @Min(value = 0, message = "Critical flow must be non-negative")
    private Double criticalFlow; // vehicles per hour

    @Min(value = 5, message = "Minimum green time must be at least 5 seconds")
    private Integer minGreenTime; // seconds

    @Min(value = 10, message = "Maximum green time must be at least 10 seconds")
    private Integer maxGreenTime; // seconds

    @Min(value = 3, message = "Yellow time must be at least 3 seconds")
    private Integer yellowTime; // seconds

    @Min(value = 0, message = "All-red time must be non-negative")
    private Integer allRedTime; // seconds
}
