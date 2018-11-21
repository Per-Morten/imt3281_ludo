package no.ntnu.imt3281.ludo.logic;

public class PlayerEvent {
    public static final int WAITING = 0;
    public static final int PLAYING = 1;
    public static final int WON = 2;
    public static final int LEFTGAME = 3;

    private Ludo mLudo;
    private int mPlayer;
    private int mStatus;

    public PlayerEvent(Ludo ludo, int player, int status) {
        mLudo = ludo;
        mPlayer = player;
        mStatus = status;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PlayerEvent)) {
            return false;
        }
        var other = (PlayerEvent)o;
        return mLudo == other.mLudo && mPlayer == other.mPlayer && mStatus == other.mStatus;
    }
}
