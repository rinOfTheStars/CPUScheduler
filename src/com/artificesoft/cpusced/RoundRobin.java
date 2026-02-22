package com.artificesoft.cpusced;

import java.util.*;

public class RoundRobin extends CPUScheduler {
    @Override
    public void process() {
        this.getRows().sort(Comparator.comparingInt((Object o) -> ((Row) o).getArrivalTime()));

        List<Row> rows = Utility.deepCopy(this.getRows());
        int time = rows.getFirst().getArrivalTime();
        int timeQuantum = this.getTimeQuantum();

        while (!rows.isEmpty()) {
            Row row = rows.getFirst();
            int bt = (Math.min(row.getBurstTime(), timeQuantum));
            this.getTimeline().add(new Event(row.getProcessName(), time, time + bt));
            time += bt;
            rows.removeFirst();

            if (row.getBurstTime() > timeQuantum) {
                row.setBurstTime(row.getBurstTime() - timeQuantum);

                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).getArrivalTime() > time) {
                        rows.add(i, row);
                        break;
                    } else if (i == rows.size() - 1) {
                        rows.add(row);
                        break;
                    }
                }
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
