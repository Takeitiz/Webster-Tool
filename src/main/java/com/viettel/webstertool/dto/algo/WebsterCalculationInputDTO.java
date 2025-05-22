package com.viettel.webstertool.dto.algo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebsterCalculationInputDTO {

    @NotEmpty(message = "At least one phase is required")
    private List<@Valid PhaseInputDTO> phases;

    @NotNull(message = "Saturation flow rate is required")
    @Min(value = 1800, message = "Saturation flow rate must be at least 1800 vph")
    private Double saturationFlowRate; // vehicles per hour per lane

    @Min(value = 30, message = "Minimum cycle length must be at least 30 seconds")
    private Integer minCycleLength; // seconds

    @Min(value = 60, message = "Maximum cycle length must be at least 60 seconds")
    private Integer maxCycleLength; // seconds
}
