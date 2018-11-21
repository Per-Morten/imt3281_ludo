package no.ntnu.imt3281.ludo.logic;

public class PieceEvent {
    private Ludo mLudo;
    private int mPlayer;
    private int mPiece;
    private int mFrom;
    private int mTo;

    public PieceEvent(Ludo ludo, int player, int piece, int from, int to) {
        mLudo = ludo;
        mPlayer = player;
        mPiece = piece;
        mFrom = from;
        mTo = to;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PieceEvent)) {
            return false;
        }
        var other = (PieceEvent)o;
        return other.mLudo == mLudo && other.mPlayer == mPlayer && other.mPiece == mPiece && other.mFrom == mFrom && other.mTo == mTo;

    }
}
