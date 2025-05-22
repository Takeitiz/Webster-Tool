package com.viettel.webstertool.service;

import com.viettel.webstertool.dto.algo.PhaseInputDTO;
import com.viettel.webstertool.dto.algo.PhaseResultDTO;
import com.viettel.webstertool.dto.algo.WebsterCalculationInputDTO;
import com.viettel.webstertool.dto.algo.WebsterCalculationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ImprovedWebsterService {

    // Webster algorithm constants
    private static final double WEBSTER_CONSTANT = 1.5;
    private static final double WEBSTER_OFFSET = 5.0;
    private static final double STARTUP_LOST_TIME_PER_PHASE = 3.0; // seconds per phase

    /**
     * Main method to calculate optimal cycle time and green splits using Webster's method
     */
    public WebsterCalculationResultDTO calculateOptimalTiming(WebsterCalculationInputDTO input) {
        log.info("Starting Webster calculation for {} phases", input.getPhases().size());

        // Step 1: Validate input and calculate flow ratios
        List<PhaseInputDTO> phases = input.getPhases();
        double saturationFlow = input.getSaturationFlowRate();

        // Step 2: Calculate critical flow ratios
        double totalFlowRatio = calculateTotalFlowRatio(phases, saturationFlow);

        // Step 3: Calculate lost time
        double totalLostTime = calculateTotalLostTime(phases);

        // Step 4: Apply Webster's formula
        double optimalCycleLength = calculateWebsterCycleLength(totalFlowRatio, totalLostTime);

        // Step 5: Apply constraints
        double constrainedCycleLength = applyConstraints(optimalCycleLength,
                input.getMinCycleLength(), input.getMaxCycleLength());

        // Step 6: Calculate green times for each phase
        List<PhaseResultDTO> phaseResults = calculatePhaseGreenTimes(phases,
                constrainedCycleLength, totalLostTime, totalFlowRatio, saturationFlow);

        // Step 7: Build result
        return buildResult(constrainedCycleLength, totalLostTime, totalFlowRatio,
                phaseResults, optimalCycleLength, input);
    }

    /**
     * Calculate the sum of flow ratios for all phases
     */
    private double calculateTotalFlowRatio(List<PhaseInputDTO> phases, double saturationFlow) {
        return phases.stream()
                .mapToDouble(phase -> phase.getCriticalFlow() / saturationFlow)
                .sum();
    }

    /**
     * Calculate total lost time based on phase transitions
     */
    private double calculateTotalLostTime(List<PhaseInputDTO> phases) {
        double startupLostTime = phases.size() * STARTUP_LOST_TIME_PER_PHASE;
        double clearanceLostTime = phases.stream()
                .mapToDouble(phase -> phase.getYellowTime() + phase.getAllRedTime())
                .sum();

        return startupLostTime + clearanceLostTime;
    }

    /**
     * Apply Webster's formula: C = (1.5L + 5) / (1 - Y)
     * Where: C = cycle length, L = lost time, Y = sum of flow ratios
     */
    private double calculateWebsterCycleLength(double totalFlowRatio, double totalLostTime) {
        if (totalFlowRatio >= 1.0) {
            log.warn("Total flow ratio {} exceeds capacity (1.0). Intersection is oversaturated.", totalFlowRatio);
            return Double.MAX_VALUE; // Indicates oversaturation
        }

        double numerator = WEBSTER_CONSTANT * totalLostTime + WEBSTER_OFFSET;
        double denominator = 1.0 - totalFlowRatio;

        return numerator / denominator;
    }

    /**
     * Apply cycle length constraints
     */
    private double applyConstraints(double optimalCycle, Integer minCycle, Integer maxCycle) {
        if (optimalCycle == Double.MAX_VALUE) {
            return maxCycle; // Use maximum cycle for oversaturated conditions
        }

        if (minCycle != null && optimalCycle < minCycle) {
            log.info("Optimal cycle {} is below minimum {}. Using minimum.", optimalCycle, minCycle);
            return minCycle;
        }

        if (maxCycle != null && optimalCycle > maxCycle) {
            log.info("Optimal cycle {} exceeds maximum {}. Using maximum.", optimalCycle, maxCycle);
            return maxCycle;
        }

        return optimalCycle;
    }

    /**
     * Calculate green time for each phase based on proportional allocation
     */
    private List<PhaseResultDTO> calculatePhaseGreenTimes(List<PhaseInputDTO> phases,
                                                          double cycleLength,
                                                          double totalLostTime,
                                                          double totalFlowRatio,
                                                          double saturationFlow) {

        List<PhaseResultDTO> results = new ArrayList<>();
        double availableGreenTime = cycleLength - totalLostTime;

        for (PhaseInputDTO phase : phases) {
            double flowRatio = phase.getCriticalFlow() / saturationFlow;
            double proportionalGreenTime = (flowRatio / totalFlowRatio) * availableGreenTime;

            // Apply min/max constraints
            int greenTime = (int) Math.round(Math.max(
                    Math.min(proportionalGreenTime, phase.getMaxGreenTime()),
                    phase.getMinGreenTime()
            ));

            int totalPhaseTime = greenTime + phase.getYellowTime() + phase.getAllRedTime();
            double degreeOfSaturation = phase.getCriticalFlow() / (saturationFlow * greenTime / 3600.0);

            PhaseResultDTO result = PhaseResultDTO.builder()
                    .phaseId(phase.getPhaseId())
                    .phaseName(phase.getPhaseName())
                    .greenTime(greenTime)
                    .yellowTime(phase.getYellowTime())
                    .allRedTime(phase.getAllRedTime())
                    .totalPhaseTime(totalPhaseTime)
                    .flowRatio(flowRatio)
                    .degreeOfSaturation(degreeOfSaturation)
                    .build();

            results.add(result);
        }

        return results;
    }

    /**
     * Build the final result with analysis
     */
    private WebsterCalculationResultDTO buildResult(double cycleLength,
                                                    double totalLostTime,
                                                    double totalFlowRatio,
                                                    List<PhaseResultDTO> phaseResults,
                                                    double optimalCycleLength,
                                                    WebsterCalculationInputDTO input) {

        boolean isOptimal = Math.abs(cycleLength - optimalCycleLength) < 1.0;
        String message = generateAnalysisMessage(totalFlowRatio, isOptimal, cycleLength, optimalCycleLength);

        return WebsterCalculationResultDTO.builder()
                .optimalCycleLength(cycleLength)
                .totalLostTime(totalLostTime)
                .totalFlowRatio(totalFlowRatio)
                .phaseResults(phaseResults)
                .isOptimal(isOptimal)
                .message(message)
                .build();
    }

    /**
     * Generate analysis message for the user
     */
    private String generateAnalysisMessage(double totalFlowRatio, boolean isOptimal,
                                           double finalCycle, double optimalCycle) {
        StringBuilder message = new StringBuilder();

        if (totalFlowRatio >= 0.9) {
            message.append("WARNING: Intersection is near capacity (flow ratio: ")
                    .append(String.format("%.2f", totalFlowRatio))
                    .append("). Consider adding capacity or reducing demand. ");
        }

        if (!isOptimal) {
            message.append("Cycle length adjusted from optimal ")
                    .append(String.format("%.1f", optimalCycle))
                    .append("s to ")
                    .append(String.format("%.1f", finalCycle))
                    .append("s due to constraints. ");
        }

        if (totalFlowRatio < 0.7) {
            message.append("Intersection operates efficiently with low congestion. ");
        }

        if (message.length() == 0) {
            message.append("Optimal timing solution achieved.");
        }

        return message.toString().trim();
    }
}