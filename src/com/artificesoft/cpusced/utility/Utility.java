package com.artificesoft.cpusced.utility;

import com.artificesoft.cpusced.schedulers.model.Row;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Utility {
    public static List<Row> deepCopy(List<Row> oldList) {
        List<Row> newList = new ArrayList<>();

        for (Row row : oldList) {
            newList.add(new Row(row.getProcessName(), row.getArrivalTime(), row.getBurstTime(), row.getPriorityLevel()));
        }

        return newList;
    }

    public static void sortRowListByArrivalTime(List<Row> list) {
        list.sort(Comparator.comparingInt((Object o) -> ((Row) o).getArrivalTime()));
    }

    public static void sortRowListByPriority(List<Row> list) {
        list.sort(Comparator.comparingInt((Object o) -> ((Row) o).getPriorityLevel()));
    }

    /**
     * Finds a {@link Row} by its {@link Row#getProcessName()} value.
     * @param list The {@link List} of rows.
     * @param name The name of the row.
     * @return The row of that name, or {@code null} if such an entry is not present.
     * @throws IllegalArgumentException If more than one entry exists in the provided list with the same name,
     *      as this may cause unexpected behavior.
     */
    public static Row findRowByName(List<Row> list, String name) throws IllegalArgumentException {
        // afaik the 'best' way to do this is to just build a temporary HashMap and use that to find our entry
        HashMap<String, Row> temp = new HashMap<>();
        for (Row r : list) {
            if (temp.containsKey(r.getProcessName())) throw new IllegalArgumentException("Bad row list!");
            temp.put(r.getProcessName(), r);
        }
        return temp.getOrDefault(name, null);
    }
}
