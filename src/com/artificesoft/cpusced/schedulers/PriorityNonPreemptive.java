package com.artificesoft.cpusced.schedulers;

import com.artificesoft.cpusced.utility.Utility;
import com.artificesoft.cpusced.schedulers.model.Event;
import com.artificesoft.cpusced.schedulers.model.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PriorityNonPreemptive extends AbstractSchedulerModel {
    /**
     * Creates a PSN scheduler model that updates processes that have been waiting in queue
     * longer than a given threshold, "rewarding" them.
     *
     * @param updateThreshold The number of simulated ticks a simulate needs to wait since it was last
     *                        'touched' (in this case, since it entered the simulated scheduler) before
     *                        it is passed through {@code rowUpdater}.
     * @param rowUpdater      A {@link Function} object (preferably a singleton) which modifies {@link Row}s passed to it.
     * @throws IllegalArgumentException If a null value is passed to {@code rowUpdater},
     *                                  or if a value less than or equal to 0 is past to {@code updateThreshold}.
     */
    public PriorityNonPreemptive(int updateThreshold, Function<Row, Row> rowUpdater) throws IllegalArgumentException {
        if (rowUpdater == null) {
            throw new IllegalArgumentException("rowUpdater cannot be null");
        }
        if (updateThreshold <= 0) {
            throw new IllegalArgumentException("updateThreshold must be a positive integer greater than zero");
        }
        this.updater = rowUpdater;
        this.threshold = updateThreshold;
    }

    /**
     * Creates a 'standard' PSN scheduler model.
     *
     * @see PriorityNonPreemptive#PriorityNonPreemptive(int, Function)
     */
    public PriorityNonPreemptive() {

    }

    private Function<Row, Row> updater = null;
    private int threshold = -1;

    @Override
    public void simulate() {
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

            // impatient mode block
            if (threshold > 0 && this.updater != null) {
                HashMap<Row, Row> updateMap = new HashMap<>();
                for (Row otherRow : rows) {
                    otherRow.incrementTimesSinceLastTouched(timeIncrease);
                    if (otherRow.checkTSLTThreshold(threshold)) {
                        Row updated = otherRow.generateReplacement(updater);
                        updateMap.put(otherRow, updated);
                    }
                }
                for (Map.Entry<Row, Row> mapping : updateMap.entrySet()) {
                    rows.remove(mapping.getKey());
                    rows.add(mapping.getValue());
                }
            }
        }

        for (Row row : this.getRows()) {
            row.setWaitingTime(this.getEvent(row).getStartTime() - row.getArrivalTime());
            row.setTurnaroundTime(row.getWaitingTime() + row.getBurstTime());
        }
    }
}
