package com.artificesoft.cpusced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PriorityNonPreemptive extends CPUScheduler {

    private Function<Row, Row> updater = null;
    private int threshold = -1;

    public void setUpdater(Function<Row, Row> updater) {
        this.updater = updater;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
    @Override
    public void process() {
        Utility.sortRowListByArrivalTime(this.getRows());
        
        List<Row> rows = Utility.deepCopy(this.getRows());
        int time = rows.getFirst().getArrivalTime();
        
        while (!rows.isEmpty()) {
            List<Row> availableRows = new ArrayList<>();
            
            for (Row row : rows) {
                if (row.getArrivalTime() <= time) {
                    availableRows.add(row);
                }
            }
            
            Utility.sortRowListByPriority(availableRows);
            
            Row row = availableRows.getFirst();
            this.getTimeline().add(new Event(row.getProcessName(), time, time + row.getBurstTime()));
            int timeIncrease = row.getBurstTime();
            time += timeIncrease;
            
            rows.remove(row);

            for (Row otherRow : rows) {
                otherRow.incrementTimesSinceLastTouched(timeIncrease);
                if (this.updater != null && otherRow.checkTSLTThreshold(threshold)) {
                    Row replacement = otherRow.generateReplacement(updater);
                    rows.remove(row);
                    rows.add(replacement);
                }
            }
        }

        for (Row row : this.getRows()) {
            row.setWaitingTime(this.getEvent(row).getStartTime() - row.getArrivalTime());
            row.setTurnaroundTime(row.getWaitingTime() + row.getBurstTime());
        }
    }
}
