package com.artificesoft.cpusced;

import java.util.function.Function;

public class Mod1UpdaterFunction implements Function<Row, Row> {
    private Mod1UpdaterFunction() {

    }

    public static Mod1UpdaterFunction SINGLETON = new Mod1UpdaterFunction();

    @Override
    public Row apply(Row row) {
        int newPriority = Math.max(row.getPriorityLevel() - 1, 0);
        Row newRow = new Row(row.getProcessName(), row.getArrivalTime(), row.getBurstTime(), newPriority);
        newRow.setWaitingTime(0); //pretty sure this starts at zero but I'm doing this as a sanity check anyway
        return newRow;
    }
}
