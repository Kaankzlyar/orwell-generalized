package utils;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Benchmark {

    private record StepTiming(String name, long millis) {}

    private final List<StepTiming> stepTimings = new ArrayList<>();

    private long currentStepStartTime;
    private String currentStepName;

    private void addStepTiming(String name, long millis) {
        stepTimings.add(new StepTiming(name, millis));
    }

    public void startTiming(String name) {
        if(currentStepName != null) {
            throw new IllegalStateException("Already timing a step: " + currentStepName);
        }

        currentStepStartTime = System.currentTimeMillis();
        currentStepName = name;
    }

    public void endTiming() {
        if(currentStepName == null) {
            throw new IllegalStateException("No step is currently being timed.");
        }

        long elapsed = System.currentTimeMillis() - currentStepStartTime;
        addStepTiming(currentStepName, elapsed);

        currentStepName = null;
    }

    public void printTimingSummary() {
        System.out.println("\n=== Execution Summary ===");
        long total = stepTimings.stream().mapToLong(StepTiming::millis).sum();
        for (StepTiming step : stepTimings) {
            System.out.printf("%-30s %6d ms%n", step.name(), step.millis());
        }
        System.out.println("-".repeat(44));
        System.out.printf("%-30s %6d ms%n", "Total", total);
    }
}

