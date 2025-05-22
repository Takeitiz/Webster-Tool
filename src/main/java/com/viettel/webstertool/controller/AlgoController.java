package com.viettel.webstertool.controller;

import com.viettel.webstertool.dto.webster.WebsterInputDTO;
import com.viettel.webstertool.dto.webster.WebsterOutputDTO;
import com.viettel.webstertool.service.WebsterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/api/algorithm", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class AlgoController {

    private final WebsterService websterService;

    @PostMapping("/webster")
    public WebsterOutputDTO evaluateNewWebster(@RequestBody WebsterInputDTO websterInputDTO) {
        System.out.println(websterInputDTO.getSaturationVolume());
        System.out.println(websterInputDTO.getStages());
        System.out.println(websterInputDTO.getRoads());
        return websterService.calculateWebster(websterInputDTO);
    }

}
