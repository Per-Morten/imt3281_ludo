package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Apparently List.copyOf does not take a copy of the list (because why would it do that?)
 * Working concurrently with references is a crazy dangerous idea, I want proper deep copies of my collections
 *
 */
public class DeepCopy {
    public static List<Integer> copy(List<Integer> src) {
        var list = new ArrayList<Integer>();
        for (var i : src) {
            list.add(Integer.valueOf(i));
        }
        return list;
    }

    public static List<Integer> copy(Queue<Integer> src) {
        var list = new ArrayList<Integer>();
        for (var i : src) {
            list.add(Integer.valueOf(i));
        }
        return list;
    }

    public static List<Integer> copy(int[] playerOrder) {
        var list = new ArrayList<Integer>();
        for (var i : playerOrder) {
            list.add(Integer.valueOf(i));
        }
        return list;
    }
}
