package com.viettel.webstertool.dto.webster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LampDTO {
    private Integer direction;
    private Integer route;
}
