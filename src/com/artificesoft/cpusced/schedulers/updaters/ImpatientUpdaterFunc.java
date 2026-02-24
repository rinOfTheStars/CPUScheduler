package com.artificesoft.cpusced.schedulers.updaters;

import com.artificesoft.cpusced.schedulers.model.Row;

import java.util.function.Function;

public class ImpatientUpdaterFunc implements Function<Row, Row> {
    private ImpatientUpdaterFunc() {

    }

    public static ImpatientUpdaterFunc SINGLETON = new ImpatientUpdaterFunc();

    @Override
    public Row apply(Row row) {
        row.rewardProcess();
        row.resetTSLT();
        return row;
    }
}
