package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.StateManager;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

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
    Button mSendChatInvite;
    @FXML
    Button mAcceptChatInvite;
    @FXML
    Button mAcceptFriendRequest;
    @FXML
    Button mRemove;
    @FXML
    Button mSendFriendRequest;
    @FXML
    Button mIgnoreUser;
    @FXML
    VBox mBoxGames;
    @FXML
    VBox mBoxChats;
    @FXML
    VBox mBoxFriends;
    @FXML
    VBox mBoxUsers;

    private Set<Integer> mSelectedGames = new HashSet<>();
    private Set<Integer> mSelectedGameInvites = new HashSet<>();
    private Set<Integer> mSelectedChats = new HashSet<>();
    private Set<Integer> mSelectedChatInvites = new HashSet<>();
    private Set<Integer> mSelectedFriends = new HashSet<>();
    private Set<Integer> mSelectedFriendRequests = new HashSet<>();
    private Set<Integer> mSelectedUsers = new HashSet<>();
    private Set<Integer> mSelectedUsersIgnored = new HashSet<>();

    private static final String textSendInvite = "Send invite"; // TODO i18n;
    private static final String textAcceptInvite = "Accept invite"; // TODO i18n;
    private static final String textAcceptRequest = "Accept Friend Request"; // TODO i18n;
    private static final String textSendRequest = "Send Friend request"; // TODO i18n;
    private static final String textRemove = "Remove"; // TODO i18n;
    private static final String textIgnore = "Ignore"; // TODO i18n;

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
                if(selected) mSelectedFriendRequests.add(id);
                else         mSelectedFriendRequests.remove(id);
                break;

            case USER:
                if(selected) mSelectedUsers.add(id);
                else         mSelectedUsers.remove(id);
                break;

            case USER_IGNORED:
                if(selected) mSelectedUsersIgnored.add(id);
                else         mSelectedUsersIgnored.remove(id);
                break;

        }
        this.renderButtonTexts();
    }

    void renderButtonTexts() {
        // ------------ Game list -----------
        // Invite friends to active games
        mSendGameInvite.setText(textSendInvite + " " + mSelectedGames.size() + " <- " + mSelectedFriends.size());

        // Accept game invites
        mAcceptGameInvite.setText(textAcceptInvite + " " + mSelectedGameInvites.size());


        // ------------ Chat list -------------
        // Invite friends to active chats
        mSendChatInvite.setText(textSendInvite + " " + mSelectedChats.size() + " <- " + mSelectedFriends.size());

        // Accept chat invites
        mAcceptChatInvite.setText(textAcceptInvite + " " + mSelectedChatInvites.size());


        // ----------- Friend list ----------
        // Remove friends, pending freinds, and ignored users
        mRemove.setText(textRemove + " " + (mSelectedFriends.size() + mSelectedFriendRequests.size() + mSelectedUsersIgnored.size()));

        // Accept pending friends
        mAcceptFriendRequest.setText(textAcceptRequest + " " + mSelectedFriendRequests.size());


        // ----------- User list ------------
        // Send friend request to normal users and ignored users.
        mSendFriendRequest.setText(textSendRequest + " " + (mSelectedUsers.size() + mSelectedUsersIgnored.size()));

        // Ignore normal users, friends and pending friends
        mIgnoreUser.setText(textIgnore + " " + (mSelectedUsers.size() + mSelectedFriendRequests.size() + mSelectedFriends.size()));
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
    void onAcceptChatInvite(ActionEvent event) {

    }

    @FXML
    void onAcceptFriendRequest(ActionEvent event) {

    }

    @FXML
    void onAcceptGameInvite(ActionEvent event) {

    }

    @FXML
    void onSendChatInvite(ActionEvent event) {

    }

    @FXML
    void onSendFriendRequest(ActionEvent event) {

    }

    @FXML
    void onSendGameInvite(ActionEvent event) {

    }

    @FXML
    void onUnfriend(ActionEvent event) {

    }

    @FXML
    void onIgnore(ActionEvent event) {

    }

    @FXML
    void onUnignore(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert mBtnUser != null : "fx:id=\"mBtnUser\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBtnLive != null : "fx:id=\"mBtnLive\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Overview.fxml'.";

        assert mBoxGames != null : "fx:id=\"mBoxGames\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBoxChats != null : "fx:id=\"mBoxChats\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBoxFriends != null : "fx:id=\"mBoxFriends\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBoxUsers != null : "fx:id=\"mBoxUsers\" was not injected: check your FXML file 'Overview.fxml'.";

        assert mSendGameInvite != null : "fx:id=\"mSendGameInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mAcceptGameInvite != null : "fx:id=\"mAcceptGameInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mSendChatInvite != null : "fx:id=\"mSendChatInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mAcceptChatInvite != null : "fx:id=\"mAcceptChatInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mAcceptFriendRequest != null : "fx:id=\"mAcceptFriendRequest\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mRemove != null : "fx:id=\"mRemove\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mSendFriendRequest != null : "fx:id=\"mSendFriendRequest\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mIgnoreUser != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Overview.fxml'.";
    }
}
