package no.ntnu.imt3281.ludo.logic;

import java.util.ArrayList;
import java.util.List;
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

    public static final int RED = 0;
    public static final int BLUE = 1;
    public static final int YELLOW = 2;
    public static final int GREEN = 3;

    Player[] mPlayer;
    int[][] mPiecePositions;
    private int mCurrentPlayer;
    private int mRemainingAttempts;
    private boolean mStarted;
    private int mSixesInARow;

    List<DiceListener> mDiceListeners = new ArrayList<>();
    List<PieceListener> mPieceListeners = new ArrayList<>();
    List<PlayerListener> mPlayerListeners = new ArrayList<>();

    /**
     * Empty constructor initiates a game with no players
     */
    public Ludo() {
        mPlayer = new Player[4];
        mPlayer[0] = new Player(null);
        mPlayer[1] = new Player(null);
        mPlayer[2] = new Player(null);
        mPlayer[3] = new Player(null);
        mPiecePositions = new int[4][4];
        mCurrentPlayer = 0;
        mRemainingAttempts = 3;
        mStarted = false;
        mSixesInARow = 0;
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
        mPlayer = new Player[4];
        mPlayer[0] = new Player(player1name);
        mPlayer[1] = new Player(player2name);
        mPlayer[2] = new Player(player3name);
        mPlayer[3] = new Player(player4name);
        mPiecePositions = new int[4][4];
        mCurrentPlayer = 0;
        mRemainingAttempts = 3;
        mStarted = false;
        mSixesInARow = 0;
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
            if (mPlayer[i].name != null) {
                count++;
            }
        }

        return count;
    }

    // Method getPlayerName should return the name of the given player. Prepended
    // with "inactive: " for players marked as inactive.
    public String getPlayerName(int color) {
        String message = mPlayer[color].name;
        if (message != null && !mPlayer[color].active) {
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
        mPlayer[currentPlayers] = new Player(playerName);
    }

    /**
     * Method removePlayer, does not actually remove player, but marks them as
     * inactive. They still count for nrOfPlayers.
     *
     * @param playerName
     */
    public void removePlayer(String playerName) {
        for (int i = 0; i < 4; i++) {
            if (mPlayer[i].name == playerName) {
                mPlayer[i].active = false;
                final var finI = i;
                mPlayerListeners.forEach(value -> value.playerStateChanged(new PlayerEvent(this, finI, PlayerEvent.LEFTGAME)));
                nextPlayersTurn();
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
            if (mPlayer[i].active) {
                count++;
            }
        }

        return count;
    }

    // Method getStatus, returns status of the game (Created, initiated, started
    // etc)

    public int activePlayer() {
        return mCurrentPlayer;
    }

    public int getPosition(int player, int piece) {
        return mPiecePositions[player][piece];
    }

    private void setPosition(int player, int piece, int position) {
        mPiecePositions[player][piece] = position;
    }

    public int throwDice(int result) {
        mStarted = true;
        mDiceListeners.forEach(value -> value.diceThrown(new DiceEvent(this, mCurrentPlayer, result)));

        if (result < 6) {
            mSixesInARow = 0;
            mRemainingAttempts--;
            if (mRemainingAttempts == 0) {
                boolean noPiecesOut = true;
                for (int piece = 0; piece < 4; piece++) {
                    // int positionOfPiece = getPosition(mCurrentPlayer, i);
                    if (getPosition(mCurrentPlayer, piece) != 0) {
                        noPiecesOut = false;
                    }
                }

                if (noPiecesOut) {
                    nextPlayersTurn();
                } else {
                    boolean canMoveSomething = false;
                    for (int piece = 0; piece < 4; piece++) {
                        int position = getPosition(mCurrentPlayer, piece);
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
            mSixesInARow++;
            if (mSixesInARow == 3) {
                mSixesInARow = 0;
                nextPlayersTurn();
            }
        }
        return result;
    }

    public int throwDice() {
        mStarted = true;
        Random random = new Random();
        int result = random.nextInt(5) + 1;
        mDiceListeners.forEach(value -> value.diceThrown(new DiceEvent(this, mCurrentPlayer, result)));

        if (result < 6) {
            mSixesInARow = 0;
            mRemainingAttempts--;
            if (mRemainingAttempts == 0) {
                boolean noPiecesOut = true;
                for (int piece = 0; piece < 4; piece++) {
                    // int positionOfPiece = getPosition(mCurrentPlayer, i);
                    if (getPosition(mCurrentPlayer, piece) != 0) {
                        noPiecesOut = false;
                    }
                }

                if (noPiecesOut) {
                    nextPlayersTurn();
                } else {
                    boolean canMoveSomething = false;
                    for (int piece = 0; piece < 4; piece++) {
                        int position = getPosition(mCurrentPlayer, piece);
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
            mSixesInARow++;
            if (mSixesInARow == 3) {
                nextPlayersTurn();
            }
        }


        return result;
    }

    public boolean movePiece(int color, int from, int to) {
        int conflictingPlayer = -1;
        int conflictingPiece = -1;
        int conflictingTile = -1;
        for (int i = 0; i < 4; i++) {
            if (getPosition(color, i) == from) {
                if (canMoveTo(from, to)) {
                    for (int player = 0; player < 4; player++) {
                        if (player != mCurrentPlayer) {
                            for (int piece = 0; piece < 4; piece++) {
                                int targetTile = userGridToLudoBoardGrid(mCurrentPlayer, to);
                                int tilePieceIsOn = userGridToLudoBoardGrid(player, getPosition(player, piece));
                                if (targetTile == tilePieceIsOn) {
                                    conflictingPiece = piece;
                                    conflictingPlayer = player;
                                    conflictingTile = getPosition(player, piece);

                                    setPosition(player, piece, 0);
                                }
                            }
                        }
                    }
                    final var i2 = i;
                    mPieceListeners.forEach(value -> value.pieceMoved(new PieceEvent(this, color, i2, from, to)));
                    if (conflictingPlayer != -1) {
                        final var finPlayer = conflictingPlayer;
                        final var finPiece = conflictingPiece;
                        final var finFrom = conflictingTile;
                        mPieceListeners.forEach(value -> value.pieceMoved(new PieceEvent(this, finPlayer, finPiece, finFrom, 0)));
                    }

                    setPosition(color, i, to);
                    if (getWinner() != -1) {
                        mPlayerListeners.forEach(value -> value.playerStateChanged(new PlayerEvent(this, color, PlayerEvent.WON)));
                    }

                    if (from == 0 || (to - from) < 6) {
                        nextPlayersTurn();
                    }

                    return true;
                }
            }
        }
        return false;
    }

    private void nextPlayersTurn() {
        final var prevPlayer = mCurrentPlayer;
        mSixesInARow = 0;
        mCurrentPlayer = (mCurrentPlayer + 1) % 4;
        // Skip inactive mPlayer
        while (!mPlayer[mCurrentPlayer].active) {
            mCurrentPlayer = (mCurrentPlayer + 1) % 4;
        }

        // If the player has no pieces out on the board, they have 3 attempts to roll a
        // 6. Otherwise 1.
        boolean noPiecesOut = true;
        for (int i = 0; i < 4; i++) {
            if (getPosition(mCurrentPlayer, i) != 0) {
                noPiecesOut = false;
            }
        }

        if (noPiecesOut) {
            mRemainingAttempts = 3;
        } else {
            mRemainingAttempts = 1;
        }

        final var newPlayer = mCurrentPlayer;
        mPlayerListeners.forEach(value -> value.playerStateChanged(new PlayerEvent(this, prevPlayer, PlayerEvent.WAITING)));
        mPlayerListeners.forEach(value -> value.playerStateChanged(new PlayerEvent(this, newPlayer, PlayerEvent.PLAYING)));
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
            if (mStarted) {
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
        if (to > 59 || (from == 0 && to != 1)) {
            return false; // Can't move outside user grid 59, and can't move to anywhere else than user
            // grid 1 from user grid 0
        }
        for (int i = from + 1; i < to + 1; i++) {
            int piecesOnTile = 0;
            for (int player = 0; player < 4; player++) {
                if (player != mCurrentPlayer) {
                    for (int piece = 0; piece < 4; piece++) {
                        int targetTile = userGridToLudoBoardGrid(mCurrentPlayer, i);
                        int tilePieceIsOn = userGridToLudoBoardGrid(player, getPosition(player, piece));
                        if (targetTile == tilePieceIsOn) {
                            piecesOnTile++;
                        }
                    }
                }
            }
            if (piecesOnTile > 1) {
                return false; // Tower in the way, can't move past it.
            }
        }
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

    public void addDiceListener(DiceListener diceListener) {
        mDiceListeners.add(diceListener);
    }

    public void addPieceListener(PieceListener pieceListener) {
        mPieceListeners.add(pieceListener);
    }

    public void addPlayerListener(PlayerListener playerListener) {
        mPlayerListeners.add(playerListener);
    }
}
