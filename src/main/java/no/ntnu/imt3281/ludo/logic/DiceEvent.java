package no.ntnu.imt3281.ludo.logic;

public class DiceEvent {
    private Ludo mLudo;
    private int mPlayer;
    private int mNumber;

    public DiceEvent(Ludo ludo, int player, int number) {
        mLudo = ludo;
        mPlayer = player;
        mNumber = number;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DiceEvent)) {
            return false;
        }
        var other = (DiceEvent)o;
        return other.mLudo == mLudo && other.mNumber == mNumber && other.mPlayer == mPlayer;

    }
}
