package com.viettel.webstertool.service;
import com.viettel.webstertool.dto.webster.*;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebsterService {

    // Adaptive control
    private static final int BASE_LOST_TIME = 15;
    private static final int MIN_GREEN_TIME_PER_STAGE = 15;

    public WebsterOutputDTO calculateWebster(WebsterInputDTO input) {
        int numberOfStages = input.getStages().size();
        double saturationVolume = input.getSaturationVolume();

        List<StageDTO> stages = input.getStages();
        double totalYellowAndAllRedTime = calculateTotalYellowAndRedClear(stages);

        List<Map<Long, Double>> stageVolumes = calculateStageVolumes(input.getStages(), input.getRoads());

        double totalVolumeOfAllStages = stageVolumes.stream()
                .flatMap(map -> map.values().stream())
                .mapToDouble(Double::doubleValue)
                .sum();

        System.out.println(totalVolumeOfAllStages);

        double cycle = calculateCycleTime(numberOfStages, totalVolumeOfAllStages, saturationVolume);
        List<StageOutputDTO> effectiveGreenTimes = new ArrayList<>();

        if (cycle <= 0) {
            cycle = 0;
            for (int i = 0; i < numberOfStages; i++) {
                StageOutputDTO stageOutputDTO = new StageOutputDTO();
                stageOutputDTO.setStageId(stages.get(i).getId());
                stageOutputDTO.setRedClearTime(stages.get(i).getRedClear());
                stageOutputDTO.setYellowTime(stages.get(i).getYellow());
                stageOutputDTO.setOldId(stages.get(i).getOldId());
                stageOutputDTO.setGreenTime(stages.get(i).getMaxGreenTime());
                cycle += stages.get(i).getMaxGreenTime();
                cycle += stages.get(i).getRedClear();
                cycle += stages.get(i).getYellow();
                effectiveGreenTimes.add(stageOutputDTO);
            }
            return new WebsterOutputDTO(cycle, effectiveGreenTimes);
        }

        double finalCycle = calculateFinalCycleAndGreenTimes(effectiveGreenTimes, stages, cycle, totalVolumeOfAllStages, numberOfStages * MIN_GREEN_TIME_PER_STAGE, totalYellowAndAllRedTime, stageVolumes);

        return new WebsterOutputDTO(finalCycle, effectiveGreenTimes);
    }

    private Integer calculateTotalYellowAndRedClear(List<StageDTO> stages) {
        int totalYellowAndRedClear = 0;
        for (StageDTO stage : stages) {
            totalYellowAndRedClear += stage.getRedClear() + stage.getYellow();
        }
        return totalYellowAndRedClear;
    }

    public List<Map<Long, Double>> calculateStageVolumes(List<StageDTO> stages, List<RoadDTO> roads) {
        List<Map<Long, Double>> result = new ArrayList<>();

        for (StageDTO stage : stages) {
            List<LampDTO> lamps = stage.getLamps();

            Set<String> lampSet = lamps.stream()
                    .map(lamp -> lamp.getDirection() + " " + lamp.getRoute())
                    .collect(Collectors.toSet());

            Double criticalFlow = 0.0;

            for (RoadDTO road : roads) {
                List<FlowDataDTO> dataFlow = road.getFlows();
                List<FlowDataDTO> matchingFlowData = new ArrayList<>();
                for (FlowDataDTO flowData : dataFlow) {
                    if (lampSet.contains(flowData.getDirection() + " " + flowData.getRoute())) {
                        matchingFlowData.add(flowData);
                    }
                }
                Optional<FlowDataDTO> maxFlowDataElement = matchingFlowData.stream()
                        .max(Comparator.comparing(FlowDataDTO::getFlowData));
                if (maxFlowDataElement.isPresent()) {
                    criticalFlow = Math.max(criticalFlow, maxFlowDataElement.get().getFlowData());
                }
            }

            result.add(Map.of(stage.getId(), criticalFlow));
        }

        return result;
    }

    private double calculateLostTime(int numberOfStages) {
        return 2 * numberOfStages + BASE_LOST_TIME;
    }

    private double calculateCycleTime(int numberOfStages, double totalVolumeOfAllStage, double saturationVolume) {
        double lostTime = calculateLostTime(numberOfStages);

        return (1.5 * lostTime + 5) / (1 - totalVolumeOfAllStage / saturationVolume);
    }

    private double calculateFinalCycleAndGreenTimes(List<StageOutputDTO> effectiveGreenTimes, List<StageDTO> stages, double cycle, double totalVolumeOfAllStages, double totalMinimumGreenTime, double totalYellowAndAllRedTime, List<Map<Long, Double>> stageVolumes) {
        double finalCycle = 0;
        for (int i = 0; i < stages.size(); i++) {
            StageOutputDTO stageOutputDTO = new StageOutputDTO();
            stageOutputDTO.setStageId(stages.get(i).getId());
            stageOutputDTO.setRedClearTime(stages.get(i).getRedClear());
            stageOutputDTO.setYellowTime(stages.get(i).getYellow());
            stageOutputDTO.setOldId(stages.get(i).getOldId());

            double minGreenTime = totalMinimumGreenTime * stages.get(i).getWeight();

            double currentStageVolume = 1;

            for (int j = 0; j < stageVolumes.size(); j++) {
                Map<Long, Double> map = stageVolumes.get(j);
                Long stageId = stages.get(i).getId();

                if (map.containsKey(stageId)) {
                    currentStageVolume = map.get(stageId);
                    break;
                }
            }

            double greenTime = (cycle - totalYellowAndAllRedTime - totalMinimumGreenTime) * currentStageVolume / totalVolumeOfAllStages + minGreenTime;

            // Check not a number
            if (Double.isNaN(greenTime) || Double.isInfinite(greenTime)) {
                greenTime =  (double) (stages.get(i).getMinGreenTime() + stages.get(i).getMaxGreenTime()) / 2;
            }

            stageOutputDTO.setGreenTime((int) Math.round(Math.min(Math.max(greenTime, stages.get(i).getMinGreenTime()), stages.get(i).getMaxGreenTime())));
            effectiveGreenTimes.add(stageOutputDTO);
            finalCycle += stageOutputDTO.getGreenTime();
        }
        return finalCycle + totalYellowAndAllRedTime;
    }
}
