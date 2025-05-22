package com.viettel.webstertool.controller;

import com.viettel.webstertool.dto.algo.PhaseInputDTO;
import com.viettel.webstertool.dto.algo.WebsterCalculationInputDTO;
import com.viettel.webstertool.dto.algo.WebsterCalculationResultDTO;
import com.viettel.webstertool.service.ImprovedWebsterService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebsterController {

    private final ImprovedWebsterService websterService;
    private static final String SESSION_FORM_KEY = "websterFormData";

    /**
     * Display the main input form
     */
    @GetMapping("/")
    public String showInputForm(Model model, HttpSession session, @RequestParam(required = false) String action) {
        WebsterCalculationInputDTO inputDTO = getFormDataFromSession(session);

        if (inputDTO == null) {
            // Initialize with default values for new sessions
            inputDTO = createDefaultInputDTO();
            saveFormDataToSession(session, inputDTO);
        }

        model.addAttribute("websterInput", inputDTO);

        // Add success messages for actions
        if ("added".equals(action)) {
            model.addAttribute("successMessage", "Phase added successfully!");
        } else if ("removed".equals(action)) {
            model.addAttribute("successMessage", "Phase removed successfully!");
        }

        return "webster-input";
    }

    /**
     * Process the calculation and show results
     */
    @PostMapping("/calculate")
    public String calculateTiming(@Valid @ModelAttribute("websterInput") WebsterCalculationInputDTO inputDTO,
                                  BindingResult bindingResult,
                                  Model model,
                                  HttpSession session) {

        // Save current form data to session
        saveFormDataToSession(session, inputDTO);

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
     * Add a new phase
     */
    @PostMapping("/add-phase")
    public String addPhase(@ModelAttribute("websterInput") WebsterCalculationInputDTO inputDTO,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        List<PhaseInputDTO> phases = inputDTO.getPhases();
        if (phases == null) {
            phases = new ArrayList<>();
            inputDTO.setPhases(phases);
        }

        int nextPhaseId = phases.size() + 1;
        phases.add(createDefaultPhase(nextPhaseId, "Phase " + nextPhaseId, 400.0));

        // Save updated form data to session
        saveFormDataToSession(session, inputDTO);

        log.info("Added phase. Total phases now: {}", phases.size());

        return "redirect:/?action=added";
    }

    /**
     * Remove a phase
     */
    @PostMapping("/remove-phase/{index}")
    public String removePhase(@PathVariable int index,
                              @ModelAttribute("websterInput") WebsterCalculationInputDTO inputDTO,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        List<PhaseInputDTO> phases = inputDTO.getPhases();
        if (phases != null && index >= 0 && index < phases.size() && phases.size() > 1) {
            phases.remove(index);

            // Renumber the phase IDs to maintain sequence
            for (int i = 0; i < phases.size(); i++) {
                phases.get(i).setPhaseId(i + 1);
                if (phases.get(i).getPhaseName().startsWith("Phase ")) {
                    phases.get(i).setPhaseName("Phase " + (i + 1));
                }
            }

            // Save updated form data to session
            saveFormDataToSession(session, inputDTO);

            log.info("Removed phase at index {}. Total phases now: {}", index, phases.size());
        }

        return "redirect:/?action=removed";
    }

    /**
     * Clear session and start fresh
     */
    @GetMapping("/reset")
    public String resetForm(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(SESSION_FORM_KEY);
        redirectAttributes.addFlashAttribute("successMessage", "Form reset to default values!");
        return "redirect:/";
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

    // Helper methods

    private WebsterCalculationInputDTO getFormDataFromSession(HttpSession session) {
        return (WebsterCalculationInputDTO) session.getAttribute(SESSION_FORM_KEY);
    }

    private void saveFormDataToSession(HttpSession session, WebsterCalculationInputDTO inputDTO) {
        session.setAttribute(SESSION_FORM_KEY, inputDTO);
        log.debug("Saved form data to session with {} phases",
                inputDTO.getPhases() != null ? inputDTO.getPhases().size() : 0);
    }

    private WebsterCalculationInputDTO createDefaultInputDTO() {
        WebsterCalculationInputDTO inputDTO = new WebsterCalculationInputDTO();
        inputDTO.setSaturationFlowRate(1900.0);
        inputDTO.setMinCycleLength(60);
        inputDTO.setMaxCycleLength(150);

        // Add 2 default phases
        List<PhaseInputDTO> defaultPhases = new ArrayList<>();
        defaultPhases.add(createDefaultPhase(1, "North-South", 800.0));
        defaultPhases.add(createDefaultPhase(2, "East-West", 600.0));
        inputDTO.setPhases(defaultPhases);

        return inputDTO;
    }

    private PhaseInputDTO createDefaultPhase(int id, String name, double flow) {
        return PhaseInputDTO.builder()
                .phaseId(id)
                .phaseName(name)
                .criticalFlow(flow)
                .minGreenTime(15)
                .maxGreenTime(60)
                .yellowTime(3)
                .allRedTime(1)
                .build();
    }
}