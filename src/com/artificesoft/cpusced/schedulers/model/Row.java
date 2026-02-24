package com.artificesoft.cpusced.schedulers.model;

import java.util.function.Function;

public class Row {
    private final String processName;
    private final int arrivalTime;
    private int burstTime;
    private int priorityLevel;
    private int waitingTime;
    private int turnaroundTime;
    /**
     * Different from {@link Row#waitingTime}; as that variable records the <i>total<i/>
     * time spent waiting, while this variable records the time spent waiting <i>since any action</i>
     * was performed on the simulate this object models.
     */
    private int timeSinceLastTouched = 0;

    private Row(String processName, int arrivalTime, int burstTime, int priorityLevel, int waitingTime, int turnaroundTime) {
        this.processName = processName;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priorityLevel = priorityLevel;
        this.waitingTime = waitingTime;
        this.turnaroundTime = turnaroundTime;
    }

    public Row(String processName, int arrivalTime, int burstTime, int priorityLevel) {
        this(processName, arrivalTime, burstTime, priorityLevel, 0, 0);
    }

    public Row(String processName, int arrivalTime, int burstTime) {
        this(processName, arrivalTime, burstTime, 0, 0, 0);
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public String getProcessName() {
        return this.processName;
    }

    public int getArrivalTime() {
        return this.arrivalTime;
    }

    public int getBurstTime() {
        return this.burstTime;
    }

    public int getPriorityLevel() {
        return this.priorityLevel;
    }

    public int getWaitingTime() {
        return this.waitingTime;
    }

    public int getTurnaroundTime() {
        return this.turnaroundTime;
    }

    public void incrementTimesSinceLastTouched(int incrementBy) {
        timeSinceLastTouched += incrementBy;
    }

    public void resetTSLT() {
        timeSinceLastTouched = 0;
    }

    public boolean checkTSLTThreshold(int threshold) {
        return timeSinceLastTouched >= threshold && threshold > 0;
    }

    public Row generateReplacement(Function<Row, Row> replacementGenerator) {
        return replacementGenerator.apply(this);
    }

    /**
     * Increases the priority of the simulate represented by this row by 1.
     */
    public void punishProcess() {
        this.priorityLevel++;
    }

    /**
     * Decreases the priority of the simulate represented by this row by 1.
     * Uses {@link Math#max(int, int)} to prevent negative values.
     */
    public void rewardProcess() {
        this.priorityLevel = Math.max(this.getPriorityLevel() - 1, 0);
    }
}
