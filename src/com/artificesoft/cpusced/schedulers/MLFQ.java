package com.artificesoft.cpusced.schedulers;

import com.artificesoft.cpusced.schedulers.model.Event;
import com.artificesoft.cpusced.schedulers.model.Row;
import com.artificesoft.cpusced.schedulers.model.VisitorAwareRow;
import com.artificesoft.cpusced.utility.Utility;

import java.util.*;
import java.util.function.Function;

public class MLFQ extends AbstractSchedulerModel {

    private static final int TIER_MULTIPLIER = 2;
    private final int boostTime;
    private final boolean isArrivalFallbackOrdered;
    private final List<PriorityQueue<VisitorAwareRow>> queues;

    public MLFQ(int quantum, int queueCount, int boostTime, boolean isArrivalFallbackOrdered) {
        this.boostTime = Math.max(boostTime, quantum);
        this.queues = new ArrayList<>();
        this.isArrivalFallbackOrdered = isArrivalFallbackOrdered;

        for (int i = 0; i < queueCount; i++) {
            queues.add(new PriorityQueue<>());
        }
        super.setTimeQuantum(quantum);
    }

    @Override
    public void simulate() {
        Utility.sortRowListByArrivalTime(this.getRows());
        List<VisitorAwareRow> rows = new ArrayList<>();
        for (Row row : this.getRows()) {
            rows.add(VisitorAwareRow.convert(row, isArrivalFallbackOrdered));
        }
        int time = rows.getFirst().getArrivalTime();
        int timeSinceLastBoost = 0;
        // loop should only stop once both the list of not-yet-arrived processes AND all processes in the queues are exhausted
        // inverting that, the loop continues while either of those conditions are false
        while (!rows.isEmpty() || !isComplete()) {
            List<VisitorAwareRow> newArrivals = new ArrayList<>();
            for (VisitorAwareRow v : rows) {
                if (v.getArrivalTime() <= time) {
                    newArrivals.add(v);
                }
            }
            // new arrivals enter at Q0 (first queue in queues)
            if (!newArrivals.isEmpty()) {
                queues.getFirst().addAll(newArrivals);
            }
            int depth = 0;
            VisitorAwareRow current = null;

            for (int i = 0; i < queues.size(); i++) {
                PriorityQueue<VisitorAwareRow> queue = queues.get(i);
                // find first item in the uppermost nonempty queue and then break
                if (!queue.isEmpty()) {
                    depth = i;
                    current = queue.poll();
                    break;
                }
            }

            if (current != null) {
                int actualQuantum = getActualQuantum(depth);
                // the amount of time in this 'tick' is the remaining burst of current or the quantum, whichever is smaller
                int tickDuration = Math.min(current.getBurstTime(), actualQuantum);
                int updatedTime = time + tickDuration;
                Event event = new Event(current.getProcessName(), time, updatedTime);
                time = updatedTime;
                current.setBurstTime(tickDuration - actualQuantum);
                if (current.getBurstTime() > 0) {
                    // send current process to next queue if it's not complete, or back to this queue if this is the lowest queue
                    queues.get(Math.min(depth + 1, queues.size() - 1)).add(current);
                    // visitation is how we maintain round-robin-like behavior
                    current.doVisit();
                }
                timeSinceLastBoost += tickDuration;
                this.getTimeline().add(event);
                if (timeSinceLastBoost >= boostTime) {
                    doBoost();
                }
            } else break; //ran out of processes, skip to clean-up

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

            // magic event manipulation I do not understand
            Map<String, Integer> nameToFinishTimeMap = new HashMap<>();

            for (Row row : this.getRows()) {
                nameToFinishTimeMap.clear();

                for (Event event : this.getTimeline()) {
                    if (event.getProcessName().equals(row.getProcessName())) {
                        if (nameToFinishTimeMap.containsKey(event.getProcessName())) {
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

    private int getActualQuantum(int depth) {
        return Math.max((depth * this.getTimeQuantum() * MLFQ.TIER_MULTIPLIER), this.getTimeQuantum());
    }

    private void doBoost() {
        // gather all processes and put them back in Q0; visitation count is not changed
        List<VisitorAwareRow> rows = new ArrayList<>();
        for (PriorityQueue<VisitorAwareRow> queue : queues) {
            while (!queue.isEmpty()) {
                rows.add(queue.poll());
            }
        }
        queues.getFirst().addAll(rows);
    }

    private boolean isComplete() {
        for (PriorityQueue<VisitorAwareRow> queue : queues) {
            if (!queue.isEmpty()) return false;
        }
        return true;
    }
}
