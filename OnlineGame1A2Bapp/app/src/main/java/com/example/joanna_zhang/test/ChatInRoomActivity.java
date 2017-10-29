package com.example.joanna_zhang.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joanna_zhang.test.Mock.MockPlayerListFactory;
import com.ood.clean.waterball.a1a2bsdk.core.model.ChatMessage;
import com.ood.clean.waterball.a1a2bsdk.core.model.Player;
import com.ood.clean.waterball.a1a2bsdk.core.modules.room.model.PlayerStatus;
import com.ood.clean.waterball.a1a2bsdk.core.modules.roomlist.model.GameMode;

import java.util.List;


public class ChatInRoomActivity extends AppCompatActivity implements View.OnClickListener, ChatWindowView.OnClickListener {

    private ChatWindowView chatWindowView;
    private Button gameStartBtn;
    private TextView gameModeTxt;
    private ListView chatRoomPlayerLst;
    private GameMode gameMode ;
    private List<Player> playerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_in_room);

        setupChatWindow();
        findViews();
        setUpThisRoomMode();
        setUpGameModeTxt();
        init();
        setUpPlayerListView();

    }

    private void init() {
        playerList = new MockPlayerListFactory().createPlayerList();
    }

    private void setUpPlayerListView() {
        RoomPlayerList playerListAdapter = new RoomPlayerList();
        chatRoomPlayerLst.setAdapter(playerListAdapter);

    }

    private void setUpThisRoomMode() {
        String modeName = getIntent().getStringExtra("roomGameMode");
        gameMode = modeName.contains("GROUP") ? GameMode.GROUP1A2B : GameMode.DUEL1A2B;
    }

    private void setUpGameModeTxt() {
        String gameModeName = gameMode.toString().contains("GROUP") ? getString(R.string.fight) : getString(R.string.duel);
        gameModeTxt.setText(gameModeName);
    }

    private void setupChatWindow() {
        chatWindowView = new ChatWindowView.Builder(this)
                .addOnSendMessageOnClickListener(this)
                .build();
    }

    private void findViews() {
        gameModeTxt = (TextView) findViewById(R.id.roomModeNameTxt);
        gameStartBtn = (Button) findViewById(R.id.gameStartBtn);
        chatRoomPlayerLst = (ListView) findViewById(R.id.chatRoomPlayersLst);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onClick(ChatMessage chatMessage) {

    }

    private class RoomPlayerList extends BaseAdapter {

        @Override
        public int getCount() {
            return gameMode.getMaxPlayerAmount();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(ChatInRoomActivity.this).inflate(R.layout.chat_room_player_list_item, viewGroup, false);
            Player player = playerList.get(position);
            PlayerStatus.UNREADY.setPlayer(player);

            TextView playerName = view.findViewById(R.id.playerNameTxt);
            ImageView playerReadyOrNot = view.findViewById(R.id.playerReadyOrNotImg);

            playerName.setText(player.getName());
            int imageId = PlayerStatus.UNREADY.getPlayer().getName().equals(player.getName()) ? R.drawable.unready : R.drawable.ready;
            playerReadyOrNot.setImageResource(imageId);
            return view;
        }
    }

}