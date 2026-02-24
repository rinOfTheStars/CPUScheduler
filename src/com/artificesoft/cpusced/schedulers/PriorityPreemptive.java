package com.artificesoft.cpusced.schedulers;

import com.artificesoft.cpusced.utility.Utility;
import com.artificesoft.cpusced.schedulers.model.Event;
import com.artificesoft.cpusced.schedulers.model.Row;

import java.util.*;
import java.util.function.Function;

public class PriorityPreemptive extends AbstractSchedulerModel {
    private final boolean isPunishing;
    private final boolean isImpatient;
    private final int impatientThreshold;

    private final Function<Row, Row> updater;

    /**
     * Creates a priority preemptive scheduler (PSP) that is neither punishing nor impatient.
     */
    public PriorityPreemptive() {
        this(false, false, 0, null);
    }

    public PriorityPreemptive(boolean isPunishing) {
        this(isPunishing, false, 0, null);
    }

    public PriorityPreemptive(boolean isImpatient, int impatientThreshold, Function<Row, Row> updater) {
        this(false, isImpatient, impatientThreshold, updater);
    }

    public PriorityPreemptive(boolean isPunishing, boolean isImpatient, int impatientThreshold, Function<Row, Row> updater) {
        this.isPunishing = isPunishing;
        this.isImpatient = isImpatient;
        this.impatientThreshold = impatientThreshold;
        this.updater = updater;
    }

    @Override
    public void simulate() {
        Utility.sortRowListByArrivalTime(this.getRows());

        List<Row> rows = Utility.deepCopy(this.getRows());
        int time = rows.getFirst().getArrivalTime();
        String lastProcessName = "";
        // primary loop
        while (!rows.isEmpty()) {
            List<Row> availableRows = new ArrayList<>();

            for (Row row : rows) {
                if (row.getArrivalTime() <= time) {
                    availableRows.add(row);
                }
            }

            Utility.sortRowListByPriority(availableRows);
            // adds entries per tick to the timeline list instead of per simulate,
            // allowing for context switching between entries
            Row row = availableRows.getFirst();
            this.getTimeline().add(new Event(row.getProcessName(), time, ++time));
            row.setBurstTime(row.getBurstTime() - 1);
            // punishing mood block
            if (!row.getProcessName().equals(lastProcessName) && isPunishing && !lastProcessName.isEmpty()) {
                // a context switch occurred this tick; 'punish' the previously active simulate by incrementing its priority
                try {
                    Row last = Utility.findRowByName(availableRows, lastProcessName);
                    if (last != null) {
                        last.punishProcess();
                        if (isImpatient) last.resetTSLT();
                    }
                } catch (IllegalArgumentException _) {
                    System.err.println("Malformed simulate list! This will probably break other logic!");
                }
            }
            lastProcessName = row.getProcessName();

            if (row.getBurstTime() == 0) {
                rows.remove(row);
            }

            // impatient mode block
            if (impatientThreshold > 0 && updater != null) {
                HashMap<Row, Row> updateMap = new HashMap<>();
                for (Row otherRow : rows) {
                    // time is only ever incremented here in units of 1
                    otherRow.incrementTimesSinceLastTouched(1);
                    if (otherRow.checkTSLTThreshold(impatientThreshold)) {
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

        // cleanup additional entries
        for (int i = this.getTimeline().size() - 1; i > 0; i--) {
            List<Event> timeline = this.getTimeline();

            // get the entry in the timeline directly 'ahead' (earlier in the list) of 'this' one
            // if it has the same name as this timeline entry, merge this entry into that one
            if (timeline.get(i - 1).getProcessName().equals(timeline.get(i).getProcessName())) {
                timeline.get(i - 1).setFinishTime(timeline.get(i).getFinishTime());
                timeline.remove(i);
            }
        }

        Map<String, Integer> nameToFinishTimeMap = new HashMap<>();

        for (Row row : this.getRows()) {
            nameToFinishTimeMap.clear();

            for (Event event : this.getTimeline()) {
                if (event.getProcessName().equals(row.getProcessName())) {
                    if (nameToFinishTimeMap.containsKey(event.getProcessName())) {
                        // I do not understand in the slightest what this is doing tbqh
                        int delta = event.getStartTime() - nameToFinishTimeMap.get(event.getProcessName());
                        row.setWaitingTime(row.getWaitingTime() + delta);
                    } else {
                        row.setWaitingTime(event.getStartTime() - row.getArrivalTime());
                    }

                    nameToFinishTimeMap.put(event.getProcessName(), event.getFinishTime());
                }
            }
            row.setTurnaroundTime(row.getWaitingTime() + row.getBurstTime());
        }
    }
}
