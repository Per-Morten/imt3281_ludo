package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.logic.Ludo;
import no.ntnu.imt3281.ludo.server.Database;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.*;

public class GameAPITests {
    // Should indicate: Pending, Started (as GAME OVER is sent through the game_event(?))
    // Should you be able to sign up for random games? (And in that case, how many should you sign up for?)
    // Or should you send in the user_id to sign up for a game?
    // Tests aren't guaranteed to be run after each other, and doing that would also lead to hard to maintain tests
    // I could have created the users within the tests as well, but that would quickly be a lot of boiler plate.
    // I rather want to have the database set up in the order I need it to be to test the edge cases.
    // Friend Group
    private static final Database.User KARL = new Database.User(1, "Karl", null, "Karl@mail.com", null, null, "KarlPassword");
    private static final Database.User FRIDA = new Database.User(2, "Frida", null, "Frida@mail.com", null, null, "FridaPassword");
    private static final Database.User HENRIK = new Database.User(3, "Henrik", null, "Henrik@mail.com", null, null, "HenrikPassword");

    // Loner who cannot be invited by others.
    private static final Database.User ARIN_LONER = new Database.User(4, "Arin", null, "Arin@mail.com", null, null, "ArinPassword");

    // People who are ignored
    private static final Database.User RALF_IGNORED_BY_KARL = new Database.User(5, "Ralf", null, "Ralf@mail.com", null, null, "RalfPassword");

    private static final Database.User USER_TO_BE_DELETED = new Database.User(6, "UserToBeDeleted", null, "UserToBeDeleted@mail.com", null, null, "password");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Logger.setLogLevel(Logger.Level.DEBUG);

        TestServer.start();

        var users = new Database.User[]{
                KARL, FRIDA, HENRIK, ARIN_LONER, RALF_IGNORED_BY_KARL, USER_TO_BE_DELETED
        };

        // Create existing friendships
        var existingFriends = new Database.User[][]{
                {KARL, FRIDA},
                {HENRIK, KARL},
                {FRIDA, HENRIK},
        };

        // Create existing requests
        var existingFriendRequests = new Database.User[][]{
                {RALF_IGNORED_BY_KARL, KARL},
        };

        // Create existing "breakups"
        var existingBreakups = new Database.User[][]{

        };

        var existingIgnores = new Database.User[][]{

        };

        TestUtility.setupRelationshipEnvironment(users, existingFriends, existingFriendRequests, existingBreakups, existingIgnores);
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestServer.stop();
    }

    @Test
    public void canCreateGame() {
        var callback = TestUtility.createJSONCallback((context, msg) -> {
           if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
               TestUtility.sendMessage(context.socket, TestUtility.createNewGameRequest(context.user.id, "My new game", context.user.token));
           }

           if (TestUtility.isOfType(ResponseType.CREATE_GAME_RESPONSE, msg)) {
               assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
               context.finishedThreads.getAndIncrement();
           }
        });
        TestUtility.testWithLoggedInUsers(List.of(KARL), List.of(callback));
    }

    @Test
    public void canGetGame() {
        String gameName = "GameToGet";
        var gameID = new AtomicInteger();
        var callback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createNewGameRequest(context.user.id, gameName, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.CREATE_GAME_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                gameID.set(msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.GAME_ID));
                TestUtility.sendMessage(context.socket, TestUtility.createGetGameRequest(context.user.id, gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.GET_GAME_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                var game = msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0);
                assertEquals(0, game.getInt(FieldNames.ID));
                assertEquals(gameID.get(), game.getInt(FieldNames.GAME_ID));
                assertEquals(gameName, game.getString(FieldNames.NAME));
                assertEquals(GameStatus.IN_LOBBY.toInt(), game.getInt(FieldNames.STATUS));
                assertEquals(context.user.id, game.getInt(FieldNames.OWNER_ID));
                assertEquals(context.user.id, game.getJSONArray(FieldNames.PLAYER_ID).getInt(0));
                assertEquals(0, game.getJSONArray(FieldNames.PENDING_ID).length());
                assertFalse(game.getBoolean(FieldNames.ALLOW_RANDOMS));
                context.finishedThreads.incrementAndGet();
            }
        });
        TestUtility.testWithLoggedInUsers(List.of(KARL), List.of(callback));
    }

    @Test
    public void canJoinGameYouAreInvitedTo() {
        // Just check multiple things in this test
        String gameName = "GameToJoinIfYouAreInvitedTo";
        var gameID = new AtomicInteger();
        var fridaIsFinished = new AtomicBoolean(false);

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createNewGameRequest(context.user.id, gameName, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.CREATE_GAME_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                gameID.set(msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.GAME_ID));
                TestUtility.sendMessage(context.socket, TestUtility.createGameInviteRequest(context.user.id, FRIDA.id, gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.SEND_GAME_INVITE_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                //context.finishedThreads.incrementAndGet();
            }

            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                assertEquals(gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                context.count.incrementAndGet();
            }

            // One update on sending out invite, one for joining the game.
            if (context.count.get() >= 2) {
                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_INVITE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Got invite");
                assertEquals(gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                assertEquals(KARL.id, msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.USER_ID));
                TestUtility.sendMessage(context.socket, TestUtility.createJoinGameRequest(context.user.id, gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_GAME_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Got join game response");
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
            }

            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Got join game update");
                assertEquals(gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));

                // We get two updates, once when you are invited and one when you join.
                if (context.count.getAndIncrement() >= 1) {
                    TestUtility.sendMessage(context.socket, TestUtility.createGetGameRequest(context.user.id, gameID.get(), context.user.token));
                }
            }

            if (TestUtility.isOfType(ResponseType.GET_GAME_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Yes, got correct message: %s", msg);
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                var game = msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0);
                assertEquals(0, game.getInt(FieldNames.ID));
                assertEquals(gameID.get(), game.getInt(FieldNames.GAME_ID));
                assertEquals(gameName, game.getString(FieldNames.NAME));
                assertEquals(GameStatus.IN_LOBBY.toInt(), game.getInt(FieldNames.STATUS));
                assertEquals(KARL.id, game.getInt(FieldNames.OWNER_ID));
                assertEquals(context.user.id, game.getJSONArray(FieldNames.PLAYER_ID).getInt(1));
                assertEquals(0, game.getJSONArray(FieldNames.PENDING_ID).length());
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA, KARL), List.of(fridaCallback, karlCallback));
    }

    @Test
    public void canStartGameIfOwner() {
        String gameName = "GameToStartIfYouAreOwner";
        var gameID = new AtomicInteger();

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Karl Got game update: %s, count: %d", msg, context.count.get());
                assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                context.count.incrementAndGet();

                    Logger.log(Logger.Level.DEBUG, "Karl is starting game");
                    TestUtility.sendMessage(context.socket, TestUtility.createUserIDAndGameIDGameRequest(RequestType.START_GAME_REQUEST, context.user.id, gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.START_GAME_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Got start game response: %s", msg);
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
            }

            if (TestUtility.isOfType(EventType.GAME_STATE_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Karl got game state update");
                assertEquals(1, msg.getJSONArray(FieldNames.PAYLOAD).length());

                var gameState = msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0);
                assertEquals(gameID.get(), gameState.getInt(FieldNames.GAME_ID));

                var playerOrder = gameState.getJSONArray(FieldNames.PLAYER_ORDER);
                assertEquals(context.user.id, playerOrder.getInt(0));
                assertEquals(FRIDA.id, playerOrder.getInt(1));
                assertEquals(context.user.id, gameState.getInt(FieldNames.CURRENT_PLAYER_ID));

                assertEquals(ActionType.THROW_DICE.toInt(), gameState.getInt(FieldNames.NEXT_ACTION));
                assertEquals(-1, gameState.getInt(FieldNames.PREVIOUS_DICE_THROW));

                // Check piece positions
                var piecePositions = gameState.getJSONArray(FieldNames.PIECE_POSITIONS);
                int counter = 0;
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        assertEquals(counter++, piecePositions.getJSONArray(i).getInt(j));
                    }
                }

                assertEquals(GameStatus.IN_SESSION.toInt(), gameState.getInt(FieldNames.STATUS));
                assertEquals(Ludo.UNASSIGNED, gameState.getInt(FieldNames.WINNER));

                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                assertEquals(gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                //context.count.incrementAndGet();
                Logger.log(Logger.Level.DEBUG, "Frida got game update, count is: %d", context.count.get());
            }

            if (TestUtility.isOfType(EventType.GAME_STATE_UPDATE, msg)) {
                //context.count.incrementAndGet();
                Logger.log(Logger.Level.DEBUG, "Frida got game State Update, count is: %d", context.count.get());
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithUsersInGame(List.of(KARL, FRIDA), List.of(karlCallback, fridaCallback), gameName);

    }

    @Test
    public void gameIsRemovedIfEmpty() {
        String gameName = "GametoRemoveWhenEmpty";
        var gameID = new AtomicInteger();

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Everyone has joined, time to leave", msg, context.count.get());
                gameID.set(context.gameID.get());
                assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                context.count.incrementAndGet();

                Logger.log(Logger.Level.DEBUG, "Karl is Leaving the game game");
                TestUtility.sendMessage(context.socket, TestUtility.createUserIDAndGameIDGameRequest(RequestType.LEAVE_GAME_REQUEST, context.user.id, context.gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.LEAVE_GAME_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                Logger.log(Logger.Level.DEBUG, "Frida is leaving the game is Leaving the game game");
                TestUtility.sendMessage(context.socket, TestUtility.createUserIDAndGameIDGameRequest(RequestType.LEAVE_GAME_REQUEST, context.user.id, context.gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.LEAVE_GAME_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithUsersInGame(List.of(KARL, FRIDA), List.of(karlCallback, fridaCallback), gameName);


        var callback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetGameRequest(context.user.id, gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.GET_GAME_RESPONSE, msg)) {
                TestUtility.assertError(Error.GAME_ID_NOT_FOUND, msg);
                context.finishedThreads.getAndIncrement();
            }
        });
        TestUtility.testWithLoggedInUsers(List.of(KARL), List.of(callback));
    }

    @Test
    public void errorOnTryingToStartGameWithTooFewPlayers() {
        String gameName = "GameToCheckStartingWithTooFewPlayers";

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Karl is alone in game, time to try and start", msg, context.count.get());
                TestUtility.sendMessage(context.socket, TestUtility.createUserIDAndGameIDGameRequest(RequestType.START_GAME_REQUEST, context.user.id, context.gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.START_GAME_RESPONSE, msg)) {
                TestUtility.assertError(Error.NOT_ENOUGH_PLAYERS, msg);
                context.finishedThreads.incrementAndGet();
            }
        });
    }

    @Test
    public void ownerIsTransferredToNextPlayerOnOwnerLeave() {
        String gameName = "GameToCheckOwnershipTransferred";
        var gameID = new AtomicInteger();

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Everyone has joined, time to leave", msg, context.count.get());
                gameID.set(context.gameID.get());
                assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));

                Logger.log(Logger.Level.DEBUG, "Karl is Leaving the game game");
                TestUtility.sendMessage(context.socket, TestUtility.createUserIDAndGameIDGameRequest(RequestType.LEAVE_GAME_REQUEST, context.user.id, context.gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.LEAVE_GAME_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Frida is in game");
                    assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                    Logger.log(Logger.Level.DEBUG, "Frida is now supposed to be owner");
                    TestUtility.sendMessage(context.socket, TestUtility.createGetGameRequest(context.user.id, context.gameID.get(), context.user.token));

            }

            if (TestUtility.isOfType(ResponseType.GET_GAME_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Frida got get game response");
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                var game = msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0);
                assertEquals(context.gameID.get(), game.getInt(FieldNames.GAME_ID));
                assertEquals(GameStatus.IN_LOBBY.toInt(), game.getInt(FieldNames.STATUS));
                assertEquals(context.user.id, game.getInt(FieldNames.OWNER_ID));
                assertEquals(context.user.id, game.getJSONArray(FieldNames.PLAYER_ID).getInt(0));
                assertEquals(0, game.getJSONArray(FieldNames.PENDING_ID).length());
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithUsersInGame(List.of(KARL, FRIDA), List.of(karlCallback, fridaCallback), gameName);
    }

    @Test
    public void startingGameWithoutBeingOwnerReturnsError() {
        String gameName = "GameToStartWithoutYouBeingOwner";
        var fridaIsFinished = new AtomicBoolean(false);


        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Karl Got game update: %s, count: %d", msg, context.count.get());
                while (!fridaIsFinished.get()) {
                    // Just spin until Frida has tried her thing.
                }

                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                //context.count.incrementAndGet();
                Logger.log(Logger.Level.DEBUG, "Frida Joined game her time to start", context.count.get());
                TestUtility.sendMessage(context.socket, TestUtility.createUserIDAndGameIDGameRequest(RequestType.START_GAME_REQUEST, context.user.id, context.gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.START_GAME_RESPONSE, msg)) {
                TestUtility.assertError(Error.USER_IS_NOT_OWNER, msg);
                fridaIsFinished.set(true);
            }
        });
    }

    @Test
    public void throwingDiceOutsideOfYourTurnReturnsError() {
        String gameName = "GameToCheckThrowingDiceOutsideOfYourTurn";
        var fridaIsFinished = new AtomicBoolean(false);

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Karl Got game update: %s, count: %d", msg, context.count.get());
                assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                context.count.incrementAndGet();

                Logger.log(Logger.Level.DEBUG, "Karl is starting game");
                TestUtility.sendMessage(context.socket, TestUtility.createUserIDAndGameIDGameRequest(RequestType.START_GAME_REQUEST, context.user.id, context.gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.START_GAME_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Got start game response: %s", msg);
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
            }

            if (TestUtility.isOfType(EventType.GAME_STATE_UPDATE, msg)) {
                while (!fridaIsFinished.get()){

                }

                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                //context.count.incrementAndGet();
                Logger.log(Logger.Level.DEBUG, "Frida got game update, count is: %d", context.count.get());
            }

            if (TestUtility.isOfType(EventType.GAME_STATE_UPDATE, msg)) {
                //context.count.incrementAndGet();
                Logger.log(Logger.Level.DEBUG, "Frida got game state and will now try to throw dice, even though it isn't her turn.", context.count.get());
                TestUtility.sendMessage(context.socket, TestUtility.createUserIDAndGameIDGameRequest(RequestType.ROLL_DICE_REQUEST, context.user.id, context.gameID.get(), context.user.token));

            }

            if (TestUtility.isOfType(ResponseType.ROLL_DICE_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Got roll dice response");
                fridaIsFinished.set(true);
                TestUtility.assertError(Error.OUT_OF_TURN, msg);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithUsersInGame(List.of(KARL, FRIDA), List.of(karlCallback, fridaCallback), gameName);
    }

    @Test
    public void movingPieceOutsideOfYourTurnReturnsError() {
        String gameName = "GameToCheckThrowingDiceOutsideOfYourTurn";
        var fridaIsFinished = new AtomicBoolean(false);

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Karl Got game update: %s, count: %d", msg, context.count.get());
                assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                context.count.incrementAndGet();

                Logger.log(Logger.Level.DEBUG, "Karl is starting game");
                TestUtility.sendMessage(context.socket, TestUtility.createUserIDAndGameIDGameRequest(RequestType.START_GAME_REQUEST, context.user.id, context.gameID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.START_GAME_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Got start game response: %s", msg);
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
            }

            if (TestUtility.isOfType(EventType.GAME_STATE_UPDATE, msg)) {
                while (!fridaIsFinished.get()){

                }

                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.GAME_UPDATE, msg)) {
                assertEquals(context.gameID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.GAME_ID));
                //context.count.incrementAndGet();
                Logger.log(Logger.Level.DEBUG, "Frida got game update, count is: %d", context.count.get());
            }

            if (TestUtility.isOfType(EventType.GAME_STATE_UPDATE, msg)) {
                //context.count.incrementAndGet();
                Logger.log(Logger.Level.DEBUG, "Frida got game state and will now try to throw dice, even though it isn't her turn.", context.count.get());
                TestUtility.sendMessage(context.socket, TestUtility.createMovePieceRequest(context.user.id, context.gameID.get(), 0, context.user.token));

            }

            if (TestUtility.isOfType(ResponseType.MOVE_PIECE_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Got roll dice response");
                fridaIsFinished.set(true);
                TestUtility.assertError(Error.OUT_OF_TURN, msg);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithUsersInGame(List.of(KARL, FRIDA), List.of(karlCallback, fridaCallback), gameName);
    }
}
