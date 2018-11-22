package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.logic.Ludo;
import no.ntnu.imt3281.ludo.logic.PlayerEvent;

import java.util.ArrayList;

public class GameTabController extends BaseController {

    int mId;

    @FXML
    Label player1Name;

    @FXML
    ImageView player1Active;

    @FXML
    Label player2Name;

    @FXML
    ImageView player2Active;

    @FXML
    Label player3Name;

    @FXML
    ImageView player3Active;

    @FXML
    Label player4Name;

    @FXML
    ImageView player4Active;

    @FXML
    ImageView diceThrown;

    @FXML
    TextArea chatArea;

    @FXML
    TextField textToSay;

    @FXML
    Button sendTextButton;

    @FXML
    Pane mBoard;


    @FXML
    private Circle mGreenPiece0;

    @FXML
    private Circle mGreenPiece1;

    @FXML
    private Circle mGreenPiece2;

    @FXML
    private Circle mGreenPiece3;

    @FXML
    private Circle mRedPiece0;

    @FXML
    private Circle mRedPiece1;

    @FXML
    private Circle mRedPiece2;

    @FXML
    private Circle mRedPiece3;

    @FXML
    private Circle mYellowPiece0;

    @FXML
    private Circle mYellowPiece1;

    @FXML
    private Circle mYellowPiece2;

    @FXML
    private Circle mYellowPiece3;

    @FXML
    private Circle mBluePiece0;

    @FXML
    private Circle mBluePiece1;

    @FXML
    private Circle mBluePiece2;

    @FXML
    private Circle mBluePiece3;

    @FXML
    private CheckBox mCheckboxAllowRandoms;

    @FXML
    private ChoiceBox<String> mChoicePiece;

    @FXML
    private Button mBtnMove;

    @FXML
    private Button mBtnThrowDice;

    @FXML
    Button mBtnStartGame;


    private Circle[] mRedPieces;
    private Circle[] mBluePieces;
    private Circle[] mYellowPieces;
    private Circle[] mGreenPieces;
    private Label[] mPlayerLabels;

    int mColor = -1;

    @FXML
    void initialize() {
        assert mBoard != null : "fx:id=\"mBoard\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert player1Active != null : "fx:id=\"player1Active\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert player1Name != null : "fx:id=\"player1Name\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert player2Active != null : "fx:id=\"player2Active\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert player2Name != null : "fx:id=\"player2Name\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert player3Active != null : "fx:id=\"player3Active\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert player3Name != null : "fx:id=\"player3Name\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert player4Active != null : "fx:id=\"player4Active\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert player4Name != null : "fx:id=\"player4Name\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert diceThrown != null : "fx:id=\"diceThrown\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mBtnThrowDice != null : "fx:id=\"mBtnThrowDice\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mBtnStartGame != null : "fx:id=\"mBtnStartGame\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mChoicePiece != null : "fx:id=\"mChoicePiece\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mBtnMove != null : "fx:id=\"mBtnMove\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert chatArea != null : "fx:id=\"chatArea\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert textToSay != null : "fx:id=\"textToSay\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert sendTextButton != null : "fx:id=\"sendTextButton\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mGreenPiece0 != null : "fx:id=\"mGreenPiece0\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mGreenPiece1 != null : "fx:id=\"mGreenPiece1\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mGreenPiece2 != null : "fx:id=\"mGreenPiece2\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mGreenPiece3 != null : "fx:id=\"mGreenPiece3\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mRedPiece0 != null : "fx:id=\"mRedPiece0\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mRedPiece1 != null : "fx:id=\"mRedPiece1\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mRedPiece2 != null : "fx:id=\"mRedPiece2\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mRedPiece3 != null : "fx:id=\"mRedPiece3\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mYellowPiece0 != null : "fx:id=\"mYellowPiece0\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mYellowPiece1 != null : "fx:id=\"mYellowPiece1\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mYellowPiece2 != null : "fx:id=\"mYellowPiece2\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mYellowPiece3 != null : "fx:id=\"mYellowPiece3\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mBluePiece0 != null : "fx:id=\"mBluePiece0\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mBluePiece1 != null : "fx:id=\"mBluePiece1\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mBluePiece2 != null : "fx:id=\"mBluePiece2\" was not injected: check your FXML file 'GameTab.fxml'.";
        assert mBluePiece3 != null : "fx:id=\"mBluePiece3\" was not injected: check your FXML file 'GameTab.fxml'.";

        mRedPieces = new Circle[]{mRedPiece0, mRedPiece1, mRedPiece2, mRedPiece3};
        mBluePieces = new Circle[]{ mBluePiece0, mBluePiece1, mBluePiece2, mBluePiece3 };
        mYellowPieces = new Circle[]{mYellowPiece0, mYellowPiece1, mYellowPiece2, mYellowPiece3};
        mGreenPieces = new Circle[]{mGreenPiece0, mGreenPiece1, mGreenPiece2, mGreenPiece3};
        mPlayerLabels = new Label[]{player1Name, player2Name, player3Name, player4Name};



        final var pink = new Color(0, 0, 0,0);
        mChoicePiece.getSelectionModel()
                .selectedItemProperty()
                .addListener( (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {


                    var pieceIndex = Integer.valueOf(newValue);

                    // Note: displays which piece is selected at any given time
                    Platform.runLater(() -> {
                        switch (mColor) {
                            case Ludo.RED: {
                                for(var piece: mRedPieces) {
                                    piece.setFill(Color.RED);
                                }
                                mRedPieces[pieceIndex].setFill(Color.PINK);
                            } break;
                            case Ludo.BLUE: {

                                for(var piece: mBluePieces) {
                                    piece.setFill(Color.BLUE);
                                }
                                mBluePieces[pieceIndex].setFill(Color.PINK);
                            } break;
                            case Ludo.YELLOW: {

                                for(var piece: mYellowPieces) {
                                    piece.setFill(Color.YELLOW);
                                }
                                mYellowPieces[pieceIndex].setFill(Color.PINK);
                            } break;
                            case Ludo.GREEN: {

                                for(var piece: mGreenPieces) {
                                    piece.setFill(Color.GREEN);
                                }
                                mGreenPieces[pieceIndex].setFill(Color.PINK);
                            } break;
                        }
                    });
                });


        ObservableList<String> pieceOptions = FXCollections.observableArrayList("0","1","2","3");
        mChoicePiece.setItems(pieceOptions);
        mChoicePiece.setValue("0");

    }


    void setPlayerLabels(ArrayList<String> playerNames) {

        int i = 0;
        for (var name: playerNames) {
            var label = mPlayerLabels[i];
            label.setText(name);
            i++;
        }
    }

    void setPiecePositions(ArrayList<ArrayList<Integer>> piecePositions) {

        final int PIECE_PER_COLOR = 4;

        //
        // GREEN PIECES
        //
        {
            var greenPositions = piecePositions.get(Ludo.GREEN);
            for (int i = 0; i < PIECE_PER_COLOR; ++i) {
                var piecePosition = greenPositions.get(i);
                var layoutPosition = LudoBoard.mapToLayoutPosition(piecePosition);
                mGreenPieces[i].setLayoutX(layoutPosition.x);
                mGreenPieces[i].setLayoutY(layoutPosition.y);
            }
        }
        //
        // RED PIECES
        //
        {
            var redPositions = piecePositions.get(Ludo.RED);
            for (int i = 0; i < PIECE_PER_COLOR; ++i) {
                var piecePosition = redPositions.get(i);
                var layoutPosition = LudoBoard.mapToLayoutPosition(piecePosition);
                mRedPieces[i].setLayoutX(layoutPosition.x);
                mRedPieces[i].setLayoutY(layoutPosition.y);
            }
        }
        //
        // BLUE PIECES
        //
        {
            var bluePositions = piecePositions.get(Ludo.BLUE);
            for (int i = 0; i < PIECE_PER_COLOR; ++i) {
                var piecePosition = bluePositions.get(i);
                var layoutPosition = LudoBoard.mapToLayoutPosition(piecePosition);
                mBluePieces[i].setLayoutX(layoutPosition.x);
                mBluePieces[i].setLayoutY(layoutPosition.y);
            }
        }
        //
        // YELLOW PIECES
        //
        {
            var yellowPositions = piecePositions.get(Ludo.YELLOW);
            for (int i = 0; i < PIECE_PER_COLOR; ++i) {
                var piecePosition = yellowPositions.get(i);
                var layoutPosition = LudoBoard.mapToLayoutPosition(piecePosition);
                mYellowPieces[i].setLayoutX(layoutPosition.x);
                mYellowPieces[i].setLayoutY(layoutPosition.y);
            }
        }
    }

    void setRolledDiceImage(int latestRolledDice) {
        switch (latestRolledDice) {
            case 1: {
                diceThrown.setImage(new Image("no/ntnu/imt3281/ludo/gui/images/dice1.png"));
            } break;
            case 2: {
                diceThrown.setImage(new Image("no/ntnu/imt3281/ludo/gui/images/dice2.png"));
            } break;
            case 3: {
                diceThrown.setImage(new Image("no/ntnu/imt3281/ludo/gui/images/dice3.png"));
            } break;
            case 4: {
                diceThrown.setImage(new Image("no/ntnu/imt3281/ludo/gui/images/dice4.png"));
            } break;
            case 5: {
                diceThrown.setImage(new Image("no/ntnu/imt3281/ludo/gui/images/dice5.png"));
            } break;
            case 6: {
                diceThrown.setImage(new Image("no/ntnu/imt3281/ludo/gui/images/dice6.png"));
            } break;
        }
    }

    @FXML
    void onMovePiece() {
        mActions.movePiece(mId, Integer.valueOf(mChoicePiece.getValue()));
    }

    @FXML
    void onThrowDice(ActionEvent event) {
        mActions.throwDice(mId);
    }

    @FXML
    void onStartGame(ActionEvent event) {
        mActions.startGame(mId);
    }

    @FXML
    void onAllowRandoms(ActionEvent event) {
        mActions.allowRandoms(mId, mCheckboxAllowRandoms.isSelected());
    }
}