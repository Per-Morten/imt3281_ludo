package no.ntnu.imt3281.ludo.gui;

import no.ntnu.imt3281.ludo.logic.Ludo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Translate ludo positions to gridx and gridy coordinates
 * Finally translates gridx and gridy to layoutx and layouty coordinates.
 *     See image -> https://bytebucket.org/Per-Morten/imt3281-project2-2018/raw/efd4711576a1681438538265f2d880e81a04e37f/src/test/java/images/ludo-board-filled.png?token=1ec98982fead5be3afbb04ac020270c21a94af10 22.11.18
 *         Black numbers represents the ludo logical positions
 */
class LudoBoard {
     static class Point {
        float x;
        float y;
        Point(float inX, float inY) {
            x = inX;
            y = inY;
        }
    }
    /**
     * Get initial logical positions of each ludo piece in a 2d array
     *
     * @return 2d array of piece positions
     */
    static ArrayList<ArrayList<Integer>> getInitialPositions(){
        return sInitialPositions;
    }

    /**
     * Map ludo position to layout position x,y
     *
     * @param ludoPosition 1D ludo logical position
     *
     * @return point x,y in layout
     */
     static Point mapToLayoutPosition(int ludoPosition) {
        var gridPosition = sLudoToGridMap.get(ludoPosition);
        return gridToLayoutPosition(gridPosition);
     }

     private static final ArrayList<ArrayList<Integer>> sInitialPositions = makeInitialPositions();
     private static final Map<Integer, Point> sLudoToGridMap = makeLudo2GridMap();

    private final static float offsetX = 25.0f;
    private final static float offsetY = 25.0f;
    private final static float cellWidth = 48.0f;
    private final static float cellHeight = 48.0f;

    private final static int TOP=0,LEFT=1,RIGHT=2,BOTTOM=3;


    private static Point gridToLayoutPosition(Point gridPosition) {

        var layoutX = gridPosition.x*cellWidth + offsetX;
        var layoutY = gridPosition.y*cellHeight + offsetY;

        return new Point(layoutX, layoutY);
    }

    private static Map<Integer, Point> makeLudo2GridMap() {

        var ludo2GridMap = new HashMap<Integer, Point>();

        /* _________________________________________________________
         * |
         * |
         * |
         * |
         * |
         * |
         * |
         * | ----------horizontal0 ------X-X--------------------------
         * | ----------horizontal1 -------X---------------------------
         * | ----------horizontal2 ------X-X--------------------------
         * |
         * |
         * |
         * |
         * |
         * |
         * |__________________________________________________________
         */
        var horizontal0 = new int[]{54,55,56,57,58,59, -1,73,-1 ,21,22,23,24,25,26};
        var horizontal1 = new int[]{53,86,87,88,89,90, 91,-1,79 ,78,77,76,75,74,27};
        var horizontal2 = new int[]{52,51,50,49,48,47, -1,85,-1 ,33,32,31,30,29,28};


        float gridX = 0;
        float gridY = 6;
        for (var number: horizontal0) {
            ludo2GridMap.put(number, new Point(gridX, gridY));
            gridX++;
        }

        gridX = 0;
        gridY = 7;
        for (var number: horizontal1) {
            ludo2GridMap.put(number, new Point(gridX, gridY));
            gridX++;
        }

        gridX = 0;
        gridY = 8;
        for (var number: horizontal2) {
            ludo2GridMap.put(number, new Point(gridX, gridY));
            gridX++;
        }

        /*
         *  ______________________________________________________
         *                   vert0  vert1  vert2
         *                       |    |   |
         *                       |    |   |
         *                       |    |   |
         *                       |    |   |
         *                       x    |   x
         *                       |    x   |
         *                       x    |   x
         *                       |    |   |
         *                       |    |   |
         *                       |    |   |
         *                       |    |   |
         *                       |    |   |
         * ______________________________________________________
         */
        var vertical0 = new int[]{64,64,63,62,61,60, -1,91,-1, 46,45,44,43,42,41};
        var vertical1 = new int[]{66,68,69,70,71,72, 73,-1,85, 84,83,82,81,80,40};
        var vertical2 = new int[]{67,16,17,18,19,20, -1,79,-1, 34,35,36,37,38,39};

        gridX = 6;
        gridY = 0;
        for (var number: vertical0) {
            ludo2GridMap.put(number, new Point(gridX, gridY));
            gridY++;
        }

        gridX = 7;
        gridY = 0;
        for (var number: vertical1) {
            ludo2GridMap.put(number, new Point(gridX, gridY));
            gridY++;
        }

        gridX = 8;
        gridY = 0;
        for (var number: vertical2) {
            ludo2GridMap.put(number, new Point(gridX, gridY));
            gridY++;
        }

        /*
         *  ______________________________________________________
         *
         *          greenBox                       redBox
         *            -                              -
         *          -   -                          -   -
         *            -                              -
         *
         *
         *
         *
         *         yellowBox                       blueBox
         *            -                              -
         *          -   -                          -   -
         *            -                              -
         * ______________________________________________________
         */

        var initial = makeInitialPositions();

        ludo2GridMap.put(initial.get(Ludo.GREEN).get(TOP), new Point(2.5f, 1.5f));
        ludo2GridMap.put(initial.get(Ludo.GREEN).get(BOTTOM), new Point(2.5f, 3.5f));
        ludo2GridMap.put(initial.get(Ludo.GREEN).get(LEFT), new Point(1.5f, 2.5f));
        ludo2GridMap.put(initial.get(Ludo.GREEN).get(RIGHT), new Point(3.5f, 2.5f));

        ludo2GridMap.put(initial.get(Ludo.RED).get(TOP), new Point(11.5f, 1.5f));
        ludo2GridMap.put(initial.get(Ludo.RED).get(BOTTOM), new Point(11.5f, 3.5f));
        ludo2GridMap.put(initial.get(Ludo.RED).get(LEFT), new Point(10.5f, 2.5f));
        ludo2GridMap.put(initial.get(Ludo.RED).get(RIGHT), new Point(12.5f, 2.5f));

        ludo2GridMap.put(initial.get(Ludo.YELLOW).get(TOP), new Point(2.5f, 10.5f));
        ludo2GridMap.put(initial.get(Ludo.YELLOW).get(BOTTOM), new Point(2.5f, 12.5f));
        ludo2GridMap.put(initial.get(Ludo.YELLOW).get(LEFT), new Point(1.5f, 11.5f));
        ludo2GridMap.put(initial.get(Ludo.YELLOW).get(RIGHT), new Point(3.5f, 11.5f));

        ludo2GridMap.put(initial.get(Ludo.BLUE).get(TOP), new Point(11.5f, 10.5f));
        ludo2GridMap.put(initial.get(Ludo.BLUE).get(BOTTOM), new Point(11.5f, 12.5f));
        ludo2GridMap.put(initial.get(Ludo.BLUE).get(LEFT), new Point(10.5f, 11.5f));
        ludo2GridMap.put(initial.get(Ludo.BLUE).get(RIGHT), new Point(12.5f, 11.5f));

        return ludo2GridMap;
    }

     private static ArrayList<ArrayList<Integer>> makeInitialPositions() {

        var greenInitial = new ArrayList<Integer>(4);
        greenInitial.add(TOP, 12);
        greenInitial.add(LEFT, 15);
        greenInitial.add(RIGHT, 13);
        greenInitial.add(BOTTOM, 14);

        var redInitial = new ArrayList<Integer>(4);
         redInitial.add(TOP, 0);
         redInitial.add(LEFT, 3);
         redInitial.add(RIGHT, 2);
         redInitial.add(BOTTOM, 1);

        var yellowInitial = new ArrayList<Integer>(4);
         yellowInitial.add(TOP, 8);
         yellowInitial.add(LEFT, 11);
         yellowInitial.add(RIGHT, 9);
         yellowInitial.add(BOTTOM, 10);

        var blueInitial   = new ArrayList<Integer>(4);
         blueInitial.add(TOP, 4);
         blueInitial.add(LEFT, 7);
         blueInitial.add(RIGHT, 6);
         blueInitial.add(BOTTOM, 5);

        var outerArray = new ArrayList<ArrayList<Integer>>(4);

        outerArray.add(Ludo.RED, redInitial);
        outerArray.add(Ludo.BLUE, blueInitial);
        outerArray.add(Ludo.YELLOW, yellowInitial);
        outerArray.add(Ludo.GREEN, greenInitial);

        return outerArray;
    }
}
