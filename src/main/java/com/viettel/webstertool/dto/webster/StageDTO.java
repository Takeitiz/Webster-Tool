package com.viettel.webstertool.dto.webster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StageDTO {
    private Long id;
    private String oldId;
    private Double weight;
    private Integer minGreenTime;
    private Integer maxGreenTime;
    private Integer yellow;
    private Integer redClear;
    private List<LampDTO> lamps = new ArrayList<>();
}
