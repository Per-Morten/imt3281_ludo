package no.ntnu.imt3281.ludo.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import no.ntnu.imt3281.ludo.api.ActionType;


public class Ludo {

    public static final int UNASSIGNED = -1;
    public static final int MAX_PLAYERS = 4;
    public static final int MIN_PLAYERS = 2;

    public static final int RED = 0;
    public static final int BLUE = 1;
    public static final int YELLOW = 2;
    public static final int GREEN = 3;

    int[] mPlayer;
    int[][] mPiecePositions;
    private int mCurrentPlayer;
    private int mRemainingAttempts;
    private boolean mStarted;
    private int mSixesInARow;
    private int mLastDiceResult;
    private ActionType mNextAction;

    List<DiceListener> mDiceListeners = new ArrayList<>();
    List<PieceListener> mPieceListeners = new ArrayList<>();
    List<PlayerListener> mPlayerListeners = new ArrayList<>();

    /**
     * Empty constructor initiates a game with no players
     */
    public Ludo() {
        mPlayer = new int[4];
        for (int i = 0; i < mPlayer.length; i++) {
            mPlayer[i] = UNASSIGNED;
        }

        setUp();
    }

    /**
     * Constructor with parameters requires at least two players, or else throw an
     * exception.
     *
     * @param player1ID
     * @param player2ID
     * @param player3ID
     * @param player4ID
     * @throws NotEnoughPlayersException
     */
    public Ludo(int player1ID, int player2ID, int player3ID, int player4ID)
            throws NotEnoughPlayersException {
        if (player1ID == UNASSIGNED || player2ID == UNASSIGNED) {
            throw new NotEnoughPlayersException();
        }
        mPlayer = new int[4];
        mPlayer[0] = player1ID;
        mPlayer[1] = player2ID;
        mPlayer[2] = player3ID;
        mPlayer[3] = player4ID;

        setUp();
    }

    /**
     * Initializes member variables. Called in all constructors.
     */
    private void setUp() {
        mPiecePositions = new int[4][4];
        mCurrentPlayer = 0;
        mRemainingAttempts = 3;
        mStarted = false;
        mSixesInARow = 0;
        mLastDiceResult = -1;
        mNextAction = ActionType.THROW_DICE;
    }

    /**
     * Method nrOfPlayers should return the number of players registered for the
     * game
     *
     * @return
     */
    public int nrOfPlayers() {
        return (int)Arrays.stream(mPlayer).filter(item -> item != UNASSIGNED).count();
    }

    // Method getPlayerName should return the name of the given player. Prepended
    // with "inactive: " for players marked as inactive.
    public int getPlayerID(int color) {
        return mPlayer[color];
    }

    /**
     * Method addPlayer, up to four players. Throws an exception if 4 players
     * already.
     *
     * @param playerID
     * @throws NoRoomForMorePlayersException
     */
    public void addPlayer(int playerID) throws NoRoomForMorePlayersException {
        int currentPlayers = nrOfPlayers();

        if (currentPlayers >= 4) {
            throw new NoRoomForMorePlayersException();
        }

        mPlayer[currentPlayers] = playerID;
    }

    /**
     * Method removePlayer, does not actually remove player, but marks them as
     * inactive and moves all their pieces back to 0. They still count for
     * nrOfPlayers.
     *
     * @param playerID
     */
    public void removePlayer(int playerID) {
        for (int i = 0; i < 4; i++) {
            if (mPlayer[i] == playerID) {
                for (int piece = 0; piece < 4; piece++) {
                    mPiecePositions[i][piece] = 0;
                }
                mPlayer[i] = UNASSIGNED;
                final var finI = i;
                mPlayerListeners.forEach(value -> value.playerStateChanged(new PlayerEvent(this, finI, PlayerEvent.LEFTGAME)));

                if (mCurrentPlayer == i) {
                    nextPlayersTurn();
                }
            }
        }
    }

    /**
     * Method activePlayers, returns number of active players in a game
     *
     * @return
     */
    public int activePlayers() {
        return (int)Arrays.stream(mPlayer).filter(item -> item != UNASSIGNED).count();
    }


    public int activePlayer() {
        return mCurrentPlayer;
    }

    public int getPosition(int player, int piece) {
        return mPiecePositions[player][piece];
    }

    private void setPosition(int player, int piece, int position) {
        mPiecePositions[player][piece] = position;
    }

    /**
     * Records the predetermined result of a dice roll, and FOR SOME REASON has to
     * return the result that was literally sent in as a parameter
     *
     * @param result - the predetermined result
     * @return
     */
    public int throwDice(int result) {
        mStarted = true;
        mDiceListeners.forEach(value -> value.diceThrown(new DiceEvent(this, mCurrentPlayer, result)));

        if (result < 6) {
            mSixesInARow = 0;
            mRemainingAttempts--;
            if (mRemainingAttempts == 0) {
                mNextAction = ActionType.MOVE_PIECE;
                boolean noPiecesOut = true;
                for (int piece = 0; piece < 4; piece++) {
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
            mNextAction = ActionType.MOVE_PIECE;
            if (mSixesInARow == 3) {
                mSixesInARow = 0;
                nextPlayersTurn();
            }
        }
        mLastDiceResult = result;
        return result;
    }

    /**
     * Rolls a pseudo-random dice, records the result, and returns it
     *
     * @return
     */
    public int throwDice() {
        mStarted = true;
        Random random = new Random();
        int result = random.nextInt(6) + 1;
        mDiceListeners.forEach(value -> value.diceThrown(new DiceEvent(this, mCurrentPlayer, result)));

        if (result < 6) {
            mSixesInARow = 0;
            mRemainingAttempts--;
            if (mRemainingAttempts == 0) {
                mNextAction = ActionType.MOVE_PIECE;
                boolean noPiecesOut = true;
                for (int piece = 0; piece < 4; piece++) {
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
            mNextAction = ActionType.MOVE_PIECE;
            if (mSixesInARow == 3) {
                nextPlayersTurn();
            }
        }

        mLastDiceResult = result;
        return result;
    }

    /**
     * Moves a piece belonging to player "color" from localspace position "from" to
     * localspace position "to" if it can, and returns whether that is a valid move.
     *
     * @param color
     * @param from
     * @param to
     * @return
     */
    public boolean movePiece(int color, int from, int to) {

        // Go through the pieces belonging to this player until we find one in the right
        // position that can legally make the desired move (no towers in the way).
        for (int i = 0; i < 4; i++) {
            if (getPosition(color, i) == from && canMoveTo(from, to)) {

                // Move the piece, and notify listeners that the piece has been moved. If this
                // was a winning move, notify them of that as well.
                setPosition(color, i, to);
                final var i2 = i;
                mPieceListeners.forEach(value -> value.pieceMoved(new PieceEvent(this, color, i2, from, to)));
                if (getWinner() != -1) {
                    mPlayerListeners
                            .forEach(value -> value.playerStateChanged(new PlayerEvent(this, color, PlayerEvent.WON)));
                }

                // We already know (from canMoveTo()) that the aren't any towers in the tiles up
                // to and including where we want to move, but is there a single piece there? We
                // check all pieces belonging to other, active players.
                int targetTile = userGridToLudoBoardGrid(mCurrentPlayer, to);
                int piecesToCheck = (activePlayers() - 1) * 4;
                for (int j = 0; j < piecesToCheck; j++) {
                    int otherPlayer = ((j / 4) + mCurrentPlayer + 1) % activePlayers();
                    int otherPiece = j % 4;
                    int tilePieceIsOn = userGridToLudoBoardGrid(otherPlayer, getPosition(otherPlayer, otherPiece));

                    // If there is a piece there belonging to someone else, we move it back to its
                    // starting position and notify listeners about it.
                    if (targetTile == tilePieceIsOn) {
                        mPieceListeners.forEach(value -> value.pieceMoved(new PieceEvent(this, otherPlayer, otherPiece,
                                getPosition(otherPlayer, otherPiece), 0)));
                        setPosition(otherPlayer, otherPiece, 0);
                    }
                }

                // If the move was out from the starting area, or the roll was less than 6, it's
                // the next players turn.
                if (from == 0 || (to - from) < 6) {
                    nextPlayersTurn();
                }
                // If the roll was a 6, this player gets to roll again.
                if ((to - from) == 6) {
                    mNextAction = ActionType.THROW_DICE;
                }

                // We have successfully moved.
                return true;
            }
        }
        // We could not legally make this move.
        return false;
    }

    public boolean movePiece(int userID, int piece) {
        int color = 0;
        for (int i = 0; i < mPlayer.length; i++) {
            if (mPlayer[i] == userID) {
                color = i;
            }
        }
        var from = getPosition(color, piece);
        return movePiece(color, from, from + mLastDiceResult);
    }

    /**
     * Passes the turn onto the next player.
     */
    private void nextPlayersTurn() {
        final var prevPlayer = mCurrentPlayer;
        mSixesInARow = 0;
        mCurrentPlayer = (mCurrentPlayer + 1) % 4;
        // Skip inactive mPlayer
        int count = 0;
        while (mPlayer[mCurrentPlayer] == UNASSIGNED) {
            count++;
            if (count > Ludo.MAX_PLAYERS * 2) {
                throw new RuntimeException("Have iterated over the game too many times, will livelock, shutting down ludo game");
            }

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

        mNextAction = ActionType.THROW_DICE;
    }

    /**
     * Returns whether the game is created, initiated, started or finished.
     *
     * @return
     */
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

    /**
     * Returns the color of the player that won, if someone has won. Otherwise -1.
     *
     * @return
     */
    public int getWinner() {
        if (getStatus().equals("Finished")) {
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

    /**
     * Checks whether a given move is possible, and returns the result. Accounts for
     * towers in the way.
     *
     * @param from - Where you want to move from
     * @param to   - Where you want to move to
     * @return
     */
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

    /**
     * Converts a position in a given users localspace to the worldspace, and
     * returns it.
     *
     * @param playerColor
     * @param localGrid
     * @return
     */
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

    public int getCurrentPlayerID() {
        return mPlayer[mCurrentPlayer];
    }


    public int[] getPlayerOrder() {
        return mPlayer;
    }

    // MUST RETURN THIS IN GLOBAL POSITION
    public int[][] getPiecePositions() {
        var globalPiecePositions = new int[4][4];
        for (int i = 0; i < globalPiecePositions.length; i++) {
            for (int j = 0; j < globalPiecePositions[i].length; j++) {
                var pos = userGridToLudoBoardGrid(i, getPosition(i, j));
                if (pos < 16) {
                    pos = (i * Ludo.MAX_PLAYERS) + j;
                }
                globalPiecePositions[i][j] = pos;
            }
        }

        return globalPiecePositions;
    }

    /**
     * Adds a DiceListener to the list of DiceListeners to be notified whenever a
     * dice is rolled.
     *
     * @param diceListener
     */
    public void addDiceListener(DiceListener diceListener) {
        mDiceListeners.add(diceListener);
    }

    /**
     * Adds a PieceListener to the list of PieceListeners to be notified whenever a
     * piece is moved.
     *
     * @param pieceListener
     */
    public void addPieceListener(PieceListener pieceListener) {
        mPieceListeners.add(pieceListener);
    }

    /**
     * Adds a PlayerListener to the list of PlayerListeners to be notified whenever
     * someone finishes their turn.
     *
     * @param playerListener
     */
    public void addPlayerListener(PlayerListener playerListener) {
        mPlayerListeners.add(playerListener);
    }

    /**
     * Predicts what the next action will be, and returns it as a String. Result
     * will be either "throw" or "move".
     *
     * @return
     */
    public ActionType getNextAction() {
        return mNextAction;
    }

    /**
     * Returns the result of the previous dice rolled.
     *
     * @return
     */
    public int previousRoll() {
        return mLastDiceResult;
    }
}
