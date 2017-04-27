package com.wii.sean.wiimmfiitus.activities;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.wii.sean.wiimmfiitus.R;
import com.wii.sean.wiimmfiitus.adapters.CustomWiiCyclerViewAdapter;
import com.wii.sean.wiimmfiitus.customViews.NintendoTextview;
import com.wii.sean.wiimmfiitus.friendSearch.MkFriendSearch;
import com.wii.sean.wiimmfiitus.friendSearch.SearchAsyncHelper;
import com.wii.sean.wiimmfiitus.helpers.LogHelper;
import com.wii.sean.wiimmfiitus.interfaces.AsyncTaskCompleteListener;
import com.wii.sean.wiimmfiitus.model.MiiCharacter;
import com.wii.sean.wiimmfiitus.model.RoomModel;

import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity implements AsyncTaskCompleteListener {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private ArrayList<MiiCharacter> miiList;
    private CustomWiiCyclerViewAdapter customWiiCyclerViewAdapter;
    private NintendoTextview roomTitle;
    private NintendoTextview regionTitle;
    private NintendoTextview connectionDrops;
    private NintendoTextview raceCount;
    private NintendoTextview lobbyCreatedTime;
    private NintendoTextview lobbyCount;
    private ProgressBar progressBar;
    private Runnable runnable;
    final Handler handler = new Handler();
    private Bundle bundle;
    private MiiCharacter mii;
    private boolean viewTypeChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        viewTypeChange = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        bundle = this.getIntent().getExtras();
        mii = (MiiCharacter) bundle.getSerializable("mii");
        recyclerView = (RecyclerView) findViewById(R.id.lobby_recyclerview);
//        recyclerViewLayoutManager = new LinearLayoutManager(this);
        recyclerViewLayoutManager = new GridLayoutManager(this, 1);
        miiList = new ArrayList<>();
        customWiiCyclerViewAdapter = new CustomWiiCyclerViewAdapter(miiList);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(customWiiCyclerViewAdapter);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_search);
        roomTitle = (NintendoTextview) findViewById(R.id.nintendoSecondaryToolbarTextview);
        regionTitle = (NintendoTextview) findViewById(R.id.nintendoToolbarTextview);
        connectionDrops = (NintendoTextview) findViewById(R.id.connection_drops_value);
        raceCount = (NintendoTextview) findViewById(R.id.race_count);
        lobbyCreatedTime = (NintendoTextview) findViewById(R.id.lobby_created_time);
        lobbyCount = (NintendoTextview) findViewById(R.id.lobby_player_count);
        refreshLobby();
    }

    @Override
    public void onTaskComplete(Object result) {
        if(result != null) {
            progressBar.setVisibility(View.INVISIBLE);
            RoomModel roomModel = (RoomModel) ((ArrayList) result).get(0);
            miiList.clear();
            customWiiCyclerViewAdapter.notifyDataSetChanged();
            miiList.addAll(roomModel.getMiiList());
            // todo hardcode shit here for default friends
//            if(miiList.contains(FriendCodes.PONCHO)) {
            regionTitle.setText(roomModel.getRegionName());
            String connRating = roomModel.getConnectionRating();
            if(connRating.equals(""))
                connRating = "none";
            connectionDrops.setText(connRating);
            raceCount.setText(roomModel.getTimesRaced());
            roomTitle.setText(roomModel.getRoomName());
            lobbyCount.setText(Integer.toString(miiList.size()) + getString(R.string.toolbar_count_playing));
            lobbyCreatedTime.setText(roomModel.getLobbyCreatedTime());
            recyclerView.setAdapter(recyclerView.getAdapter());
            customWiiCyclerViewAdapter.notifyDataSetChanged();
            viewTypeChange = false;
        }



        if(result == null && mii.isFriend()) {
            if(viewTypeChange == false) {
                mii.setType(MiiCharacter.DEFAULT_VIEW);
                miiList.clear();
                miiList.add(mii);
                customWiiCyclerViewAdapter = new CustomWiiCyclerViewAdapter(miiList);
                recyclerView.setLayoutManager(recyclerViewLayoutManager);
                recyclerView.setAdapter(customWiiCyclerViewAdapter);
                viewTypeChange = true;
            }

            roomTitle.setText("waiting for friends...");
        }
        if(result == null && !mii.isFriend()) {
            Toast.makeText(this, getString(R.string.offline), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void refreshLobby() {
        final SearchAsyncHelper searchAsyncHelper = new SearchAsyncHelper(LobbyActivity.this, LobbyActivity.this);
        searchAsyncHelper.execute(mii.getFriendCode(), MkFriendSearch.ROOMENABLED);
        runnable = new Runnable() {
            @Override
            public void run() {
                final SearchAsyncHelper searchAsyncHelper = new SearchAsyncHelper(LobbyActivity.this, LobbyActivity.this);
                searchAsyncHelper.execute(mii.getFriendCode(), MkFriendSearch.ROOMENABLED);
                Log.d(LogHelper.getTag(getClass()), "I am refresh!");
                progressBar.setVisibility(View.VISIBLE);
                handler.postDelayed(this, 15000);
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
