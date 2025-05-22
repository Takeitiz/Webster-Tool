package com.viettel.webstertool.controller;

import com.viettel.webstertool.dto.algo.PhaseInputDTO;
import com.viettel.webstertool.dto.algo.WebsterCalculationInputDTO;
import com.viettel.webstertool.dto.algo.WebsterCalculationResultDTO;
import com.viettel.webstertool.service.ImprovedWebsterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebsterController {

    private final ImprovedWebsterService websterService;

    /**
     * Display the main input form
     */
    @GetMapping("/")
    public String showInputForm(Model model) {
        // Initialize with default values for better UX
        WebsterCalculationInputDTO inputDTO = new WebsterCalculationInputDTO();
        inputDTO.setSaturationFlowRate(1900.0);
        inputDTO.setMinCycleLength(60);
        inputDTO.setMaxCycleLength(150);

        // Add 2 default phases
        List<PhaseInputDTO> defaultPhases = new ArrayList<>();
        defaultPhases.add(createDefaultPhase(1, "North-South", 800.0));
        defaultPhases.add(createDefaultPhase(2, "East-West", 600.0));
        inputDTO.setPhases(defaultPhases);

        model.addAttribute("websterInput", inputDTO);
        return "webster-input";
    }

    /**
     * Process the calculation and show results
     */
    @PostMapping("/calculate")
    public String calculateTiming(@Valid @ModelAttribute("websterInput") WebsterCalculationInputDTO inputDTO,
                                  BindingResult bindingResult,
                                  Model model) {

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in input: {}", bindingResult.getAllErrors());
            return "webster-input";
        }

        try {
            WebsterCalculationResultDTO result = websterService.calculateOptimalTiming(inputDTO);
            model.addAttribute("result", result);
            model.addAttribute("websterInput", inputDTO);
            return "webster-result";
        } catch (Exception e) {
            log.error("Error calculating Webster timing", e);
            model.addAttribute("errorMessage", "Error calculating timing: " + e.getMessage());
            return "webster-input";
        }
    }

    /**
     * REST API endpoint for external integrations
     */
    @PostMapping("/api/webster/calculate")
    @ResponseBody
    public ResponseEntity<WebsterCalculationResultDTO> calculateTimingAPI(@Valid @RequestBody WebsterCalculationInputDTO inputDTO) {
        try {
            WebsterCalculationResultDTO result = websterService.calculateOptimalTiming(inputDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("API Error calculating Webster timing", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add a new phase via AJAX
     */
    @PostMapping("/add-phase")
    public String addPhase(@ModelAttribute WebsterCalculationInputDTO inputDTO, Model model) {
        List<PhaseInputDTO> phases = inputDTO.getPhases();
        if (phases == null) {
            phases = new ArrayList<>();
            inputDTO.setPhases(phases);
        }

        int nextPhaseId = phases.size() + 1;
        phases.add(createDefaultPhase(nextPhaseId, "Phase " + nextPhaseId, 400.0));

        model.addAttribute("websterInput", inputDTO);
        return "fragments/phase-input :: phase-list";
    }

    /**
     * Remove a phase via AJAX
     */
    @PostMapping("/remove-phase/{index}")
    public String removePhase(@PathVariable int index,
                              @ModelAttribute WebsterCalculationInputDTO inputDTO,
                              Model model) {
        List<PhaseInputDTO> phases = inputDTO.getPhases();
        if (phases != null && index >= 0 && index < phases.size()) {
            phases.remove(index);
        }

        model.addAttribute("websterInput", inputDTO);
        return "fragments/phase-input :: phase-list";
    }

    private PhaseInputDTO createDefaultPhase(int id, String name, double flow) {
        return PhaseInputDTO.builder()
                .phaseId(id)
                .phaseName(name)
                .criticalFlow(flow)
                .minGreenTime(15)
                .maxGreenTime(60)
                .yellowTime(4)
                .allRedTime(2)
                .build();
    }
}