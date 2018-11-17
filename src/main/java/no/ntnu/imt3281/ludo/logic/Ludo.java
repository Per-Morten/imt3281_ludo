package no.ntnu.imt3281.ludo.logic;

import java.util.Random;

class Player {
    public String name;
    public boolean active;

    public Player(String playerName) {
        name = playerName;
        if (name != null) {
            active = true;
        } else {
            active = false;
        }
    }
}

public class Ludo {

    static final int RED = 0;
    static final int BLUE = 1;
    static final int YELLOW = 2;
    static final int GREEN = 3;

    Player[] players;
    int[][] piecePositions;
    private int currentPlayer;
    private int remainingAttempts;
    private boolean started;
    private int sixesInARow;

    /**
     * Empty constructor initiates a game with no players
     */
    public Ludo() {
        players = new Player[4];
        players = new Player[4];
        players[0] = new Player(null);
        players[1] = new Player(null);
        players[2] = new Player(null);
        players[3] = new Player(null);
        piecePositions = new int[4][4];
        currentPlayer = 0;
        remainingAttempts = 3;
        started = false;
        sixesInARow = 0;
    }

    /**
     * Constructor with parameters requires at least two players, or else throw an
     * exception.
     *
     * @param player1name
     * @param player2name
     * @param player3name
     * @param player4name
     * @throws NotEnoughPlayersException
     */
    public Ludo(String player1name, String player2name, String player3name, String player4name)
            throws NotEnoughPlayersException {
        if (player1name == null || player2name == null) {
            throw new NotEnoughPlayersException();
        }
        players = new Player[4];
        players[0] = new Player(player1name);
        players[1] = new Player(player2name);
        players[2] = new Player(player3name);
        players[3] = new Player(player4name);
        piecePositions = new int[4][4];
        currentPlayer = 0;
        remainingAttempts = 3;
        started = false;
        sixesInARow = 0;
    }

    /**
     * Method nrOfPlayers should return the number of players registered for the
     * game
     *
     * @return
     */
    public int nrOfPlayers() {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (players[i].name != null) {
                count++;
            }
        }

        return count;
    }

    // Method getPlayerName should return the name of the given player. Prepended
    // with "inactive: " for players marked as inactive.
    public String getPlayerName(int color) {
        String message = players[color].name;
        if (message != null && !players[color].active) {
            message = "Inactive: " + message;
        }
        return message;
    }

    /**
     * Method addPlayer, up to four players. Throws an exception if 4 players
     * already.
     *
     * @param playerName
     * @throws NoRoomForMorePlayersException
     */
    public void addPlayer(String playerName) throws NoRoomForMorePlayersException {
        int currentPlayers = nrOfPlayers();

        if (currentPlayers == 4) {
            throw new NoRoomForMorePlayersException();
        }
        players[currentPlayers] = new Player(playerName);
    }

    /**
     * Method removePlayer, does not actually remove player, but marks them as
     * inactive. They still count for nrOfPlayers.
     *
     * @param playerName
     */
    public void removePlayer(String playerName) {
        for (int i = 0; i < 4; i++) {
            if (players[i].name == playerName) {
                players[i].active = false;
            }
        }
    }

    /**
     * Method activePlayers, returns number of active players in a game
     *
     * @return
     */
    public int activePlayers() {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (players[i].active) {
                count++;
            }
        }

        return count;
    }

    // Method getStatus, returns status of the game (Created, initiated, started
    // etc)

    public int activePlayer() {
        return currentPlayer;
    }

    public int getPosition(int player, int piece) {
        return piecePositions[player][piece];
    }

    private void setPosition(int player, int piece, int position) {
        piecePositions[player][piece] = position;
    }

    public int throwDice(int result) {
        started = true;
        if (result < 6) {
            sixesInARow = 0;
            remainingAttempts--;
            if (remainingAttempts == 0) {
                boolean noPiecesOut = true;
                for (int piece = 0; piece < 4; piece++) {
                    // int positionOfPiece = getPosition(currentPlayer, i);
                    if (getPosition(currentPlayer, piece) != 0) {
                        noPiecesOut = false;
                    }
                }

                if (noPiecesOut) {
                    nextPlayersTurn();
                } else {
                    boolean canMoveSomething = false;
                    for (int piece = 0; piece < 4; piece++) {
                        int position = getPosition(currentPlayer, piece);
                        if (canMoveTo(position, position + result)) {
                            canMoveSomething = true;
                        }
                    }
                    if (!canMoveSomething) {
                        nextPlayersTurn();
                    }
                }
            }
        } else {
            sixesInARow++;
            if (sixesInARow == 3) {
                sixesInARow = 0;
                nextPlayersTurn();
            }
        }
        return result;
    }

    public int throwDice() {
        started = true;
        Random random = new Random();
        int result = random.nextInt(5) + 1;
        if (result < 6) {
            sixesInARow = 0;
            remainingAttempts--;
            if (remainingAttempts == 0) {
                boolean noPiecesOut = true;
                for (int piece = 0; piece < 4; piece++) {
                    // int positionOfPiece = getPosition(currentPlayer, i);
                    if (getPosition(currentPlayer, piece) != 0) {
                        noPiecesOut = false;
                    }
                }

                if (noPiecesOut) {
                    nextPlayersTurn();
                } else {
                    boolean canMoveSomething = false;
                    for (int piece = 0; piece < 4; piece++) {
                        int position = getPosition(currentPlayer, piece);
                        if (canMoveTo(position, position + result)) {
                            canMoveSomething = true;
                        }
                    }
                    if (!canMoveSomething) {
                        nextPlayersTurn();
                    }
                }
            }
        } else {
            sixesInARow++;
            if (sixesInARow == 3) {
                sixesInARow = 0;
                nextPlayersTurn();
            }
        }
        return result;
    }

    public boolean movePiece(int color, int from, int to) {
        for (int i = 0; i < 4; i++) {
            if (getPosition(color, i) == from) {
                setPosition(color, i, to);
                if (from == 0 || (to - from) < 6) {
                    nextPlayersTurn();
                }
                return true;
            }
        }
        return false;
    }

    private void nextPlayersTurn() {
        currentPlayer = (currentPlayer + 1) % 4;
        // Skip inactive players
        while (!players[currentPlayer].active) {
            currentPlayer = (currentPlayer + 1) % 4;
        }

        // If the player has no pieces out on the board, they have 3 attempts to roll a
        // 6. Otherwise 1.
        boolean noPiecesOut = true;
        for (int i = 0; i < 4; i++) {
            if (getPosition(currentPlayer, i) != 0) {
                noPiecesOut = false;
            }
        }

        if (noPiecesOut) {
            remainingAttempts = 3;
        } else {
            remainingAttempts = 1;
        }
    }

    public String getStatus() {
        if (activePlayers() == 0) {
            return "Created";
        } else {
            for (int player = 0; player < 4; player++) {
                boolean finished = true;
                for (int piece = 0; piece < 4; piece++) {
                    if (getPosition(player, piece) != 59) {
                        finished = false;
                    }
                }
                if (finished) {
                    return "Finished";
                }
            }
            if (started) {
                return "Started";
            }
            return "Initiated";
        }
    }

    public int getWinner() {
        if (getStatus() == "Finished") {
            for (int player = 0; player < 4; player++) {
                boolean finished = true;
                for (int piece = 0; piece < 4; piece++) {
                    if (getPosition(player, piece) != 59) {
                        finished = false;
                    }
                }
                if (finished) {
                    return player;
                }
            }
        }
        return -1;
    }

    private boolean canMoveTo(int from, int to) {
        if (to > 59 || from == 0) {
            return false;
        }
        /*
         * int piecesThere = 0; for (int player = 0; player < 4; player++) { for (int
         * piece = 0; piece < 4; piece++) { if (player != currentPlayer &&
         * getPosition(player, piece) == position) { piecesThere++; } } } if
         * (piecesThere > 1) { return false; }
         */
        return true;
    }

    public int userGridToLudoBoardGrid(int playerColor, int localGrid) {
        if (localGrid == 0) {
            return playerColor * 4;
        }
        int boardGrid = (15 + localGrid + (13 * playerColor));
        if (boardGrid < 68) {
            return boardGrid;
        }
        if (boardGrid > 67 && localGrid < 54) {
            return boardGrid - 52;
        }
        return localGrid + 14 + (6 * playerColor);
    }
}
