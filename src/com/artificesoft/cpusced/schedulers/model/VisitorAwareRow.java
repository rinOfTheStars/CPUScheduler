package com.artificesoft.cpusced.schedulers.model;

public class VisitorAwareRow extends Row implements Comparable<VisitorAwareRow> {

    private final boolean isArrivalFallbackOrdered;

    private int visitationCount = 0;

    public VisitorAwareRow(String processName, int arrivalTime, int burstTime, int priorityLevel, boolean isArrivalFallbackOrdered) {
        this.isArrivalFallbackOrdered = isArrivalFallbackOrdered;
        super(processName, arrivalTime, burstTime, priorityLevel);
    }
    public VisitorAwareRow(String processName, int arrivalTime, int burstTime, int priorityLevel) {
        super(processName, arrivalTime, burstTime, priorityLevel);
        isArrivalFallbackOrdered = true;
    }

    public VisitorAwareRow(String processName, int arrivalTime, int burstTime) {
        super(processName, arrivalTime, burstTime);
        isArrivalFallbackOrdered = true;
    }

    public static VisitorAwareRow convert(Row row, boolean isArrivalFallbackOrdered) {
        return new VisitorAwareRow(row.getProcessName(), row.getArrivalTime(), row.getBurstTime(), row.getPriorityLevel(), isArrivalFallbackOrdered);
    }

    public void doVisit() {
        this.visitationCount++;
    }

    @Override
    public int compareTo(VisitorAwareRow row) {
        int score;

        if (this.visitationCount == row.visitationCount) {
            if (isArrivalFallbackOrdered) {
                score = this.getArrivalTime() - row.getArrivalTime();
            } else {
                score = this.getPriorityLevel() - row.getPriorityLevel();
            }
        } else {
            score = this.visitationCount - row.visitationCount;
        }
        return (score != 0) ? score : (this.getProcessName().compareTo(row.getProcessName()));
    }
}
