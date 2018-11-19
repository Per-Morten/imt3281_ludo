package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.HashSet;

public class OverviewController extends BaseController {

    @FXML
    Button mBtnUser;
    @FXML
    Button mBtnLive;
    @FXML
    Button mBtnLogout;

    @FXML
    Button mSendGameInvite;
    @FXML
    Button mAcceptGameInvite;
    @FXML
    Button mLeaveGame;

    @FXML
    Button mSendChatInvite;
    @FXML
    Button mAcceptChatInvite;
    @FXML
    Button mLeaveChat;

    @FXML
    Button mAcceptFriendRequest;
    @FXML
    Button mIgnoreFriend;
    @FXML
    Button mUnfriend;

    @FXML
    Button mSendFriendRequest;
    @FXML
    Button mIgnoreUser;
    @FXML
    Button mUnignore;

    @FXML
    VBox mListGames;
    @FXML
    VBox mListChats;
    @FXML
    VBox mListFriends;
    @FXML
    VBox mListUsers;

    private HashSet<Integer> mSelectedGames = new HashSet<>();
    private HashSet<Integer> mSelectedGameInvites = new HashSet<>();
    private HashSet<Integer> mSelectedChats = new HashSet<>();
    private HashSet<Integer> mSelectedChatInvites = new HashSet<>();
    private HashSet<Integer> mSelectedFriends = new HashSet<>();
    private HashSet<Integer> mSelectedFriendsPending = new HashSet<>();
    private HashSet<Integer> mSelectedUsers = new HashSet<>();
    private HashSet<Integer> mSelectedIgnored = new HashSet<>();

    private static final String textSendGameInvite = "Send Game Invite"; // TODO i18n;
    private static final String textAcceptGameInvite = "Join Game"; // TODO i18n;
    private static final String textLeaveGame = "Leave Game"; // TODO i18n;
    private static final String textSendChatInvite = "Send Chat Invite"; // TODO i18n;
    private static final String textAcceptChatInvite = "Join Chat"; // TODO i18n;
    private static final String textLeaveChat = "Leave Chat"; // TODO i18n;
    private static final String textAcceptRequest = "Accept Friend Request"; // TODO i18n;
    private static final String textSendRequest = "Send Friend request"; // TODO i18n;
    private static final String textUnfriend = "Unfriend"; // TODO i18n;
    private static final String textIgnore = "Ignore"; // TODO i18n;
    private static final String textUnignore = "Unignore"; // TODO i18n;

    void onItemSelectChange(ListItemType type, int id, boolean selected) {
        switch (type) {
            case GAME:
                if(selected) mSelectedGames.add(id);
                else         mSelectedGames.remove(id);
                break;

            case GAME_INVITE:
                if(selected) mSelectedGameInvites.add(id);
                else         mSelectedGameInvites.remove(id);
                break;

            case CHAT:
                if(selected) mSelectedChats.add(id);
                else         mSelectedChats.remove(id);
                break;

            case CHAT_INVITE:
                if(selected) mSelectedChatInvites.add(id);
                else         mSelectedChatInvites.remove(id);
                break;

            case FRIEND:
                if(selected) mSelectedFriends.add(id);
                else         mSelectedFriends.remove(id);
                break;

            case FRIEND_REQUEST:
                if(selected) mSelectedFriendsPending.add(id);
                else         mSelectedFriendsPending.remove(id);
                break;

            case USER:
                if(selected) mSelectedUsers.add(id);
                else         mSelectedUsers.remove(id);
                break;

            case USER_IGNORED:
                if(selected) mSelectedIgnored.add(id);
                else         mSelectedIgnored.remove(id);
                break;

        }
        this.renderButtonTexts();
    }

    void renderButtonTexts() {
        // ------------ Game list -----------
        // Invite friends to active games
        mSendGameInvite.setText(textSendGameInvite + " " + mSelectedGames.size() + " -> " + mSelectedFriends.size());

        // Leave selected games
        mLeaveGame.setText(textLeaveGame + " " + (mSelectedGames.size() + mSelectedGameInvites.size()));

        // Accept game invites
        mAcceptGameInvite.setText(textAcceptGameInvite + " " + mSelectedGameInvites.size());


        // ------------ Chat list -------------
        // Invite friends to active chats
        mSendChatInvite.setText(textSendChatInvite + " " + mSelectedChats.size() + " ->  " + mSelectedFriends.size());

        // Leave selected chats
        mLeaveChat.setText(textLeaveChat + " " + mSelectedChats.size());

        // Accept chat invites
        mAcceptChatInvite.setText(textAcceptChatInvite + " " + mSelectedChatInvites.size());


        // ----------- Friend list ----------
        // Accept pending friends
        mAcceptFriendRequest.setText(textAcceptRequest + " " + mSelectedFriendsPending.size());

        // Ignore pending and friends
        mIgnoreFriend.setText(textIgnore + " " + (mSelectedFriends.size() + mSelectedFriendsPending.size()));

        // Remove friends, pending freinds
        mUnfriend.setText(textUnfriend + " " + (mSelectedFriends.size() + mSelectedFriendsPending.size()));


        // ----------- User list ------------
        // Send friend request to normal users and ignored users.
        mSendFriendRequest.setText(textSendRequest + " " + (mSelectedUsers.size() + mSelectedIgnored.size()));

        // Ignore normal users, friends and pending friends
        mIgnoreUser.setText(textIgnore + " " + (mSelectedUsers.size()));

        // Unignore ignored users
        mUnignore.setText(textUnignore + " " + (mSelectedIgnored.size()));
    }

    @FXML
    void onClickLive(ActionEvent event) {
        mActions.gotoLive();
    }

    @FXML
    void onClickLogout(ActionEvent event) {
        mActions.logout( );
    }

    @FXML
    void onClickUser(ActionEvent event) {
        mActions.gotoUser();
    }


    @FXML
    void onSendGameInvite(ActionEvent event) {
        if (mSelectedGames.size() == 0 || mSelectedFriends.size() == 0) return;
        mActions.sendGameInvite(mSelectedGames, mSelectedFriends);
    }

    @FXML
    void onAcceptGameInvite(ActionEvent event) {
        if (mSelectedGameInvites.size() == 0) return;
        mActions.joinGame(mSelectedGameInvites);
    }

    @FXML
    void onLeaveGame(ActionEvent event) {
        if (mSelectedGames.size() > 0) mActions.leaveGame(mSelectedGames);
        if (mSelectedGameInvites.size() > 0) mActions.declineGameInvite(mSelectedGameInvites);
    }

    @FXML
    void onSendChatInvite(ActionEvent event) {
        if (mSelectedChats.size() == 0 || mSelectedFriends.size() == 0) return;
        mActions.sendChatInvite(mSelectedChats, mSelectedFriends);
    }

    @FXML
    void onAcceptChatInvite(ActionEvent event) {
        if (mSelectedChatInvites.size() == 0) return;
        mActions.joinChat(mSelectedChatInvites);
    }

    @FXML
    void onLeaveChat(ActionEvent event) {
        if (mSelectedChats.size() == 0) return;
        mActions.leaveChat(mSelectedChats);
    }

    @FXML
    void onSendFriendRequest(ActionEvent event) {
        var selectedUsers = new HashSet<Integer>();
        selectedUsers.addAll(mSelectedUsers);
        selectedUsers.addAll(mSelectedIgnored);
        selectedUsers.addAll(mSelectedFriendsPending);
        if (selectedUsers.size() == 0) return;
        mActions.friend(selectedUsers);
    }

    @FXML
    void onAcceptFriendRequest(ActionEvent event) {
        if (mSelectedFriendsPending.size() == 0) return;
        mActions.friend(mSelectedFriendsPending);
    }

    @FXML
    void onUnfriend(ActionEvent event) {
        var selectedFriends = new HashSet<Integer>();
        selectedFriends.addAll(mSelectedFriends);
        selectedFriends.addAll(mSelectedFriendsPending);
        if (selectedFriends.size() == 0) return;
        mActions.unfriend(selectedFriends);
    }

    @FXML
    void onIgnoreUsers(ActionEvent event) {
        if (mSelectedUsers.size() == 0) return;
        mActions.ignore(mSelectedUsers);
    }

    @FXML
    void onIgnoreFriends(ActionEvent event) {
        var selectedFriends = new HashSet<Integer>();
        selectedFriends.addAll(mSelectedFriends);
        selectedFriends.addAll(mSelectedFriendsPending);
        if (selectedFriends.size() == 0) return;
        mActions.ignore(selectedFriends);
    }

    @FXML void onUnignore(ActionEvent event) {
        if (mSelectedIgnored.size() == 0) return;
        mActions.unignore(mSelectedIgnored);
    }

    @FXML
    void initialize() {
        assert mSendGameInvite != null : "fx:id=\"mSendGameInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mAcceptGameInvite != null : "fx:id=\"mAcceptGameInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mLeaveGame != null : "fx:id=\"mLeaveGame\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mListGames != null : "fx:id=\"mListGames\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mSendChatInvite != null : "fx:id=\"mSendChatInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mAcceptChatInvite != null : "fx:id=\"mAcceptChatInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mLeaveChat != null : "fx:id=\"mLeaveChat\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mListChats != null : "fx:id=\"mListChats\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mAcceptFriendRequest != null : "fx:id=\"mAcceptFriendRequest\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mIgnoreFriend != null : "fx:id=\"mIgnoreFriend\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mUnfriend != null : "fx:id=\"mUnfriend\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mListFriends != null : "fx:id=\"mListFriends\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mSendFriendRequest != null : "fx:id=\"mSendFriendRequest\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mIgnoreUser != null : "fx:id=\"mIgnoreUser\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mUnignore != null : "fx:id=\"mUnignore\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mListUsers != null : "fx:id=\"mListUsers\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBtnUser != null : "fx:id=\"mBtnUser\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBtnLive != null : "fx:id=\"mBtnLive\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Overview.fxml'.";
    }
}
