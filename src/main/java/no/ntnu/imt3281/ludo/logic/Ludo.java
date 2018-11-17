package no.ntnu.imt3281.ludo.logic;

public class Ludo {

    static final int RED = 0;
    static final int BLUE = 1;
    static final int YELLOW = 2;
    static final int GREEN = 3;

    String[] playerNames;
    int[][] piecePositions;

    // Store player names in an array?

    /**
     * Empty constructor initiates a game with no players
     */
    public Ludo() {
        playerNames = new String[] { null, null, null, null };
        piecePositions = new int[4][4];
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
        playerNames = new String[] { player1name, player2name, player3name, player4name };
        piecePositions = new int[4][4];
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
            if (playerNames[i] != null) {
                count++;
            }
        }

        return count;
    }

    // Method getPlayerName should return the name of the given player. Prepended
    // with "inactive: " for players marked as inactive.
    public String getPlayerName(int color) {
        return playerNames[color];
        // TODO: account for inactive players
    }

    // Method addPlayer, up to four players. Throws an exception if 4 players
    // already.
    public void addPlayer(String playerName) throws NoRoomForMorePlayersException {

    }

    // Method removePlayer, does not actually remove player, but marks them as
    // inactive. They still count for nrOfPlayers

    // Method activePlayers, returns number of active players in a game

    // Method getStatus, returns status of the game (Created, initiated, started
    // etc)
}
