package com.artificesoft.cpusced;

import com.artificesoft.cpusced.schedulers.*;
import com.artificesoft.cpusced.schedulers.model.Event;
import com.artificesoft.cpusced.schedulers.model.Row;

import java.util.List;

public class Main {
    static void main() {
        System.out.println("-----------------FCFS----------------");
        fcfs();
        System.out.println("-----------------SJF-----------------");
        sjf();
        System.out.println("-----------------SRT-----------------");
        srt();
        System.out.println("-----------------PSN-----------------");
        psn();
        System.out.println("-----------------PSP-----------------");
        psp();
        System.out.println("-----------------RR------------------");
        rr();
    }

    public static void fcfs() {
        AbstractSchedulerModel fcfs = new FirstComeFirstServe();
        fcfs.add(new Row("P1", 0, 5));
        fcfs.add(new Row("P2", 2, 4));
        fcfs.add(new Row("P3", 4, 3));
        fcfs.add(new Row("P4", 6, 6));
        fcfs.simulate();
        display(fcfs);
    }

    public static void sjf() {
        AbstractSchedulerModel sjf = new ShortestJobFirst();
        sjf.add(new Row("P1", 0, 5));
        sjf.add(new Row("P2", 2, 3));
        sjf.add(new Row("P3", 4, 2));
        sjf.add(new Row("P4", 6, 4));
        sjf.add(new Row("P5", 7, 1));
        sjf.simulate();
        display(sjf);
    }

    public static void srt() {
        AbstractSchedulerModel srt = new ShortestRemainingTime();
        srt.add(new Row("P1", 8, 1));
        srt.add(new Row("P2", 5, 1));
        srt.add(new Row("P3", 2, 7));
        srt.add(new Row("P4", 4, 3));
        srt.add(new Row("P5", 2, 8));
        srt.add(new Row("P6", 4, 2));
        srt.add(new Row("P7", 3, 5));
        srt.simulate();
        display(srt);
    }

    public static void psn() {
        AbstractSchedulerModel psn = new PriorityNonPreemptive();
        psn.add(new Row("P1", 8, 1));
        psn.add(new Row("P2", 5, 1));
        psn.add(new Row("P3", 2, 7));
        psn.add(new Row("P4", 4, 3));
        psn.add(new Row("P5", 2, 8));
        psn.add(new Row("P6", 4, 2));
        psn.add(new Row("P7", 3, 5));
        psn.simulate();
        display(psn);
    }

    public static void psp() {
        AbstractSchedulerModel psp = new PriorityPreemptive();
        psp.add(new Row("P1", 8, 1));
        psp.add(new Row("P2", 5, 1));
        psp.add(new Row("P3", 2, 7));
        psp.add(new Row("P4", 4, 3));
        psp.add(new Row("P5", 2, 8));
        psp.add(new Row("P6", 4, 2));
        psp.add(new Row("P7", 3, 5));
        psp.simulate();
        display(psp);
    }

    public static void rr() {
        AbstractSchedulerModel rr = new RoundRobin();
        rr.setTimeQuantum(2);
        rr.add(new Row("P1", 0, 4));
        rr.add(new Row("P2", 1, 5));
        rr.add(new Row("P3", 2, 6));
        rr.add(new Row("P4", 4, 1));
        rr.add(new Row("P5", 6, 3));
        rr.add(new Row("P6", 7, 2));
        rr.simulate();
        display(rr);
    }

    public static void display(AbstractSchedulerModel object) {
        System.out.println("Process\tAT\tBT\tWT\tTAT");

        for (Row row : object.getRows()) {
            System.out.println(row.getProcessName() + "\t" + row.getArrivalTime() + "\t" + row.getBurstTime() + "\t" + row.getWaitingTime() + "\t" + row.getTurnaroundTime());
        }

        System.out.println();

        for (int i = 0; i < object.getTimeline().size(); i++) {
            List<Event> timeline = object.getTimeline();
            System.out.print(timeline.get(i).getStartTime() + "(" + timeline.get(i).getProcessName() + ")");

            if (i == object.getTimeline().size() - 1) {
                System.out.print(timeline.get(i).getFinishTime());
            }
        }

        System.out.println("\n\nAverage WT: " + object.getAverageWaitingTime() + "\nAverage TAT: " + object.getAverageTurnAroundTime());
    }
}
