package com.artificesoft.cpusced;

import java.util.Comparator;
import java.util.List;

public class FirstComeFirstServe extends CPUScheduler {
    @Override
    public void process() {
        this.getRows().sort(Comparator.comparingInt((Object o) -> ((Row) o).getArrivalTime()));

        List<Event> timeline = this.getTimeline();

        for (Row row : this.getRows()) {
            if (timeline.isEmpty()) {
                timeline.add(new Event(row.getProcessName(), row.getArrivalTime(), row.getArrivalTime() + row.getBurstTime()));
            } else {
                Event event = timeline.getLast();
                timeline.add(new Event(row.getProcessName(), event.getFinishTime(), event.getFinishTime() + row.getBurstTime()));
            }
        }

        for (Row row : this.getRows()) {
            row.setWaitingTime(this.getEvent(row).getStartTime() - row.getArrivalTime());
            row.setTurnaroundTime(row.getWaitingTime() + row.getBurstTime());
        }
    }
}
