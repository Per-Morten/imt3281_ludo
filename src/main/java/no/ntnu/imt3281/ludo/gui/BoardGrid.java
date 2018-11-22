package no.ntnu.imt3281.ludo.gui;

/**
 * Translate ludo positions to gridx and gridy coordinates
 * Finally translates gridx and gridy to layoutx and layouty coordinates.
 * @see The blac numbers represents the ludo logical position image https://bytebucket.org/Per-Morten/imt3281-project2-2018/raw/efd4711576a1681438538265f2d880e81a04e37f/src/test/java/images/ludo-board-filled.png?token=1ec98982fead5be3afbb04ac020270c21a94af10 22.11.18
 */
public class BoardGrid {

    public static final int top = 0;
    public static final int left = 0;
    public static final int right = 14;
    public static final int bottom = 14;

    public static Map<Integer, float[2]> makeLudo2GridMap() {
        var ludo2GridMap = new HashMap<Integer, float[2]>;
        
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
        final int[15] horizontal0 = new int[15]{54,55,56,57,58,59, -1,73,-1 ,21,22,23,24,25,26};
        final int[15] horizontal1 = new int[15]{53,86,87,88,89,90, 91,-1,79 ,78,77,76,75,74,27};
        final int[15] horizontal2 = new int[15]{52,51,50,49,48,47, -1,85,-1 ,33,32,31,30,29,28};


        float gridX = 0;
        float gridY = 6;
        for (var number: horizontal0) {
            ludo2GridMap.put(number, new float[2]{gridX, gridY});
            gridX++;
        }

        gridX = 0;
        gridY = 7;
        for (var number: horizontal1) {
            ludo2GridMap.put(number, new float[2]{gridX, gridY});
            gridX++;   
        }

        gridX = 0;
        gridY = 8;
        for (var number: horizontal2) {
            ludo2GridMap.put(number, new float[2]{gridX, gridY});
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
        final int[15] vertical0 = new int[15]{64,64,63,62,61,60, -1,91,-1, 46,45,44,43,42,41};
        final int[15] vertical1 = new int[15]{66,68,69,70,71,72, 73,-1,85, 84,83,82,81,80,40};
        final int[15] vertical2 = new int[15]{67,16,17,18,19,20, -1,79,-1, 34,35,36,37,38,39};

        gridX = 6;
        gridY = 0;
        for (var number: vertical0) {
            ludo2GridMap.put(number, new float[2]{gridX, gridY});
            gridY++;
        }

        gridX = 7;
        gridY = 0;
        for (var number: vertical1) {
            ludo2GridMap.put(number, new float[2]{gridX, gridY});
            gridY++;
        }

        gridX = 8;
        gridY = 0;
        for (var number: vertical2) {
            ludo2GridMap.put(number, new float[2]{gridX, gridY});
            gridY++;
        }

        /*
        *  ______________________________________________________
        *
        *          greenbox                       redbox
        *            -                              -  
        *          -   -                          -   -
        *            -                              -  
        *
        *
        *
        *
        *         yellowbox                       bluebox
        *            -                              -  
        *          -   -                          -   -
        *            -                              -  
        * ______________________________________________________
        */

        final int TOP=0,LEFT=1,RIGHT=2,BOTTOM=3;
        final int[4] greenbox  = new int[4]{12,15,13, 14};
        final int[4] redbox    = new int[4]{ 0, 3, 2,  1};
        final int[4] yellowbox = new int[4]{ 8,11, 9, 10};
        final int[4] bluebox   = new int[4]{ 4, 7, 6,  5};

        ludo2GridMap.put(greenbox[TOP],    new float[2]{2.5,1.5});
        ludo2GridMap.put(greenbox[BOTTOM], new float[2]{2.5,3.5});
        ludo2GridMap.put(greenbox[LEFT],   new float[2]{1.5,2.5});
        ludo2GridMap.put(greenbox[BOTTOM], new float[2]{3.5,2.5});

        ludo2GridMap.put(redbox[TOP],    new float[2]{11.5, 1.5});
        ludo2GridMap.put(redbox[BOTTOM], new float[2]{11.5, 3.5});
        ludo2GridMap.put(redbox[LEFT],   new float[2]{10.5, 2.5});
        ludo2GridMap.put(redbox[BOTTOM], new float[2]{12.5, 2.5});

        ludo2GridMap.put(yellowbox[TOP],    new float[2]{2.5, 10.5});
        ludo2GridMap.put(yellowbox[BOTTOM], new float[2]{2.5, 12.5});
        ludo2GridMap.put(yellowbox[LEFT],   new float[2]{1.5, 11.5});
        ludo2GridMap.put(yellowbox[BOTTOM], new float[2]{3.5, 11.5});

        ludo2GridMap.put(bluebox[TOP],    new float[2]{11.5, 10.5});
        ludo2GridMap.put(bluebox[BOTTOM], new float[2]{11.5, 12.5});
        ludo2GridMap.put(bluebox[LEFT],   new float[2]{10.5, 11.5});
        ludo2GridMap.put(bluebox[BOTTOM], new float[2]{12.5, 11.5});
    }

    public final Map<Integer, float[2]> ludo2GridMap = makeLudo2GridMap();
    /**
     * Make a piece
     *
     * @param ludoPosition 1d ludo logical position
     */
    static Circle makePiece(int ludoPosition) {
        var pieceX = ludo2GridMap.get(ludoPosition)[0];
        var pieceX = ludo2GridMap.get(ludoPosition)[1];
    }
} 
