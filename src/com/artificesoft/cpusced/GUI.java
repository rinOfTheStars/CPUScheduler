package com.artificesoft.cpusced;

import com.artificesoft.cpusced.schedulers.*;
import com.artificesoft.cpusced.schedulers.model.Event;
import com.artificesoft.cpusced.schedulers.model.Row;
import com.artificesoft.cpusced.schedulers.updaters.ImpatientUpdaterFunc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class GUI {
    private final CustomPanel chartPanel;
    private final JTable table;
    private final JLabel wtResultLabel;
    private final JLabel tatResultLabel;
    private final JComboBox<SchedulerVariants> option;
    private final DefaultTableModel model;

    public GUI() {
        model = new DefaultTableModel(new String[]{"Process", "AT", "BT", "Priority", "WT", "TAT"}, 0);

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane tablePane = new JScrollPane(table);
        tablePane.setBounds(25, 25, 450, 250);

        JButton addBtn = new JButton("Add");
        addBtn.setBounds(300, 280, 85, 25);
        addBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addBtn.addActionListener(_ -> model.addRow(new String[]{"", "", "", "", "", ""}));

        JButton removeBtn = new JButton("Remove");
        removeBtn.setBounds(390, 280, 85, 25);
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        removeBtn.addActionListener(_ -> {
            int row = table.getSelectedRow();

            if (row > -1) {
                model.removeRow(row);
            }
        });

        chartPanel = new CustomPanel();
//        chartPanel.setPreferredSize(new Dimension(700, 10));
        chartPanel.setBackground(Color.WHITE);
        JScrollPane chartPane = new JScrollPane(chartPanel);
        chartPane.setBounds(25, 310, 450, 100);

        JLabel wtLabel = new JLabel("Average Waiting Time:");
        wtLabel.setBounds(25, 425, 180, 25);
        JLabel tatLabel = new JLabel("Average Turn Around Time:");
        tatLabel.setBounds(25, 450, 180, 25);
        wtResultLabel = new JLabel();
        wtResultLabel.setBounds(215, 425, 180, 25);
        tatResultLabel = new JLabel();
        tatResultLabel.setBounds(215, 450, 180, 25);

        option = new JComboBox<>(SchedulerVariants.values());
        option.setBounds(390, 420, 85, 20);

        JButton computeBtn = new JButton("Compute");
        computeBtn.setBounds(390, 450, 85, 25);
        computeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        computeBtn.addActionListener(_ -> {
            SchedulerVariants selected = (SchedulerVariants) option.getSelectedItem();
            AbstractSchedulerModel scheduler;

            switch (selected) {
                case FCFS:
                    scheduler = new FirstComeFirstServe();
                    break;
                case SJF:
                    scheduler = new ShortestJobFirst();
                    break;
                case SRT:
                    scheduler = new ShortestRemainingTime();
                    break;
                case PSN:
                    scheduler = new PriorityNonPreemptive();
                    break;
                case PSP:
                    scheduler = new PriorityPreemptive();
                    break;
                case RR:
                    String tq = JOptionPane.showInputDialog("Time Quantum");
                    if (tq == null) {
                        return;
                    }
                    scheduler = new RoundRobin();
                    scheduler.setTimeQuantum(Integer.parseInt(tq));
                    break;
                // "modded" PSP and PSN variants:
                case IMPATIENT_PSN:
                    scheduler = new PriorityNonPreemptive(3, ImpatientUpdaterFunc.SINGLETON);
                    break;
                case IMPATIENT_PSP:
                    scheduler = new PriorityPreemptive(true, 3, ImpatientUpdaterFunc.SINGLETON);
                    break;
                case PUNISHING_PSP:
                    scheduler = new PriorityPreemptive(true);
                    break;
                // is both impatient and punishing
                case FULLMOD_PSP:
                    scheduler = new PriorityPreemptive(true, true, 3, ImpatientUpdaterFunc.SINGLETON);
                    break;
                case null, default:
                    return;
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                String process = (String) model.getValueAt(i, 0);
                int at = Integer.parseInt((String) model.getValueAt(i, 1));
                int bt = Integer.parseInt((String) model.getValueAt(i, 2));
                int pl;

                // using instanceof accounts for modified versions of these two schedulers
                if (scheduler instanceof PriorityPreemptive || scheduler instanceof PriorityNonPreemptive) {
                    if (!model.getValueAt(i, 3).equals("")) {
                        pl = Integer.parseInt((String) model.getValueAt(i, 3));
                    } else {
                        pl = 1;
                    }
                } else {
                    pl = 1;
                }
                scheduler.add(new Row(process, at, bt, pl));
            }

            scheduler.simulate();

            for (int i = 0; i < model.getRowCount(); i++) {
                String process = (String) model.getValueAt(i, 0);
                Row row = scheduler.getRow(process);
                model.setValueAt(row.getWaitingTime(), i, 4);
                model.setValueAt(row.getTurnaroundTime(), i, 5);
            }

            wtResultLabel.setText(Double.toString(scheduler.getAverageWaitingTime()));
            tatResultLabel.setText(Double.toString(scheduler.getAverageTurnAroundTime()));

            chartPanel.setTimeline(scheduler.getTimeline());
        });

        JPanel mainPanel = new JPanel(null);
        mainPanel.setPreferredSize(new Dimension(500, 500));
        mainPanel.add(tablePane);
        mainPanel.add(addBtn);
        mainPanel.add(removeBtn);
        mainPanel.add(chartPane);
        mainPanel.add(wtLabel);
        mainPanel.add(tatLabel);
        mainPanel.add(wtResultLabel);
        mainPanel.add(tatResultLabel);
        mainPanel.add(option);
        mainPanel.add(computeBtn);

        JFrame frame = new JFrame("CPU Scheduler Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.add(mainPanel);
        frame.pack();
    }

    static void main() {
        new GUI();
    }

    static class CustomPanel extends JPanel {
        private List<Event> timeline;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (timeline != null) {
//                int width = 30;

                for (int i = 0; i < timeline.size(); i++) {
                    Event event = timeline.get(i);
                    int x = 30 * (i + 1);
                    int y = 20;

                    g.drawRect(x, y, 30, 30);
                    g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g.drawString(event.getProcessName(), x + 10, y + 20);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g.drawString(Integer.toString(event.getStartTime()), x - 5, y + 45);

                    if (i == timeline.size() - 1) {
                        g.drawString(Integer.toString(event.getFinishTime()), x + 27, y + 45);
                    }

//                    width += 30;
                }

//                this.setPreferredSize(new Dimension(width, 75));
            }
        }

        public void setTimeline(List<Event> timeline) {
            this.timeline = timeline;
            repaint();
        }
    }

    enum SchedulerVariants {
        FCFS,
        SJF,
        SRT,
        PSN,
        IMPATIENT_PSN,
        PSP,
        PUNISHING_PSP,
        IMPATIENT_PSP,
        FULLMOD_PSP,
        RR
    }
}
