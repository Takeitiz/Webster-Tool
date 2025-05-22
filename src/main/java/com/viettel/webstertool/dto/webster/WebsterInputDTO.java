package com.viettel.webstertool.dto.webster;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WebsterInputDTO {

    @NotEmpty(message = "Roads list must not be empty")
    private List<@Valid RoadDTO> roads;

    @Positive(message = "Saturation volume must be positive")
    private double saturationVolume;

    @NotEmpty(message = "Stages list must not be empty")
    private List<@Valid StageDTO> stages;
}
