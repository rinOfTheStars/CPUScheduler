package com.artificesoft.cpusced;

import java.util.*;

public class PriorityPreemptive extends CPUScheduler {
    @Override
    public void process() {
        this.getRows().sort(Comparator.comparingInt((Object o) -> ((Row) o).getArrivalTime()));

        List<Row> rows = Utility.deepCopy(this.getRows());
        int time = rows.getFirst().getArrivalTime();

        while (!rows.isEmpty()) {
            List<Row> availableRows = new ArrayList<>();

            for (Row row : rows) {
                if (row.getArrivalTime() <= time) {
                    availableRows.add(row);
                }
            }

            availableRows.sort(Comparator.comparingInt((Object o) -> ((Row) o).getPriorityLevel()));

            Row row = availableRows.getFirst();
            this.getTimeline().add(new Event(row.getProcessName(), time, ++time));
            row.setBurstTime(row.getBurstTime() - 1);

            if (row.getBurstTime() == 0) {
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).getProcessName().equals(row.getProcessName())) {
                        rows.remove(i);
                        break;
                    }
                }
            }
        }

        for (int i = this.getTimeline().size() - 1; i > 0; i--) {
            List<Event> timeline = this.getTimeline();

            if (timeline.get(i - 1).getProcessName().equals(timeline.get(i).getProcessName())) {
                timeline.get(i - 1).setFinishTime(timeline.get(i).getFinishTime());
                timeline.remove(i);
            }
        }

        Map<String, Integer> map = new HashMap<>();

        for (Row row : this.getRows()) {
            map.clear();

            for (Event event : this.getTimeline()) {
                if (event.getProcessName().equals(row.getProcessName())) {
                    if (map.containsKey(event.getProcessName())) {
                        int w = event.getStartTime() - map.get(event.getProcessName());
                        row.setWaitingTime(row.getWaitingTime() + w);
                    } else {
                        row.setWaitingTime(event.getStartTime() - row.getArrivalTime());
                    }

                    map.put(event.getProcessName(), event.getFinishTime());
                }
            }

            row.setTurnaroundTime(row.getWaitingTime() + row.getBurstTime());
        }
    }
}
