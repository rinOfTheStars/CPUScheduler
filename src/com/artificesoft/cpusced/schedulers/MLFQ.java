package com.artificesoft.cpusced.schedulers;

import com.artificesoft.cpusced.schedulers.model.Row;
import com.artificesoft.cpusced.schedulers.model.VisitorAwareRow;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MLFQ extends AbstractSchedulerModel {

    private final int quantum;
    private final int boostTime;
    private final boolean isArrivalFallbackOrdered;
    private final List<PriorityQueue<VisitorAwareRow>> queues;

    private final List<VisitorAwareRow> rows = new ArrayList<>();
    private int tickCounter = 0;

    public MLFQ(int quantum, int queueCount, int boostTime, boolean isArrivalFallbackOrdered) {
        this.quantum = quantum;
        this.boostTime = Math.max(boostTime, this.quantum);
        this.queues = new ArrayList<>();
        this.isArrivalFallbackOrdered = isArrivalFallbackOrdered;

        for (int i = 0; i < queueCount; i++) {
            queues.add(new PriorityQueue<>());
        }
        super.setTimeQuantum(this.quantum);
    }

    @Override
    public boolean add(Row row) {
        if (row instanceof VisitorAwareRow) {
            this.rows.add((VisitorAwareRow) row);
        } else {
            VisitorAwareRow converted = VisitorAwareRow.convert(row, isArrivalFallbackOrdered);
            this.rows.add(converted);
        }
        return true;
    }

    @Override
    public void simulate() {

    }
}
