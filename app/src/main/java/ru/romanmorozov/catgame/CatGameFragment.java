package ru.romanmorozov.catgame;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * Created by morozovr on 5/31/2016.
 */
public class CatGameFragment extends Fragment {

    private Toolbar toolbar;
    private HighScoreDbHelper helper;

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =
                inflater.inflate(R.layout.fragment_game, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.cat_game_actionbar_toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.subtitle);

        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        helper = new HighScoreDbHelper(getContext());


        return view;
    }


    // set up volume control once Activity is created
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // allow volume keys to set game volume
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.exit:
                halt();
                return true;
            case R.id.highscores:
                showInfoDialog();
                return true;
            case R.id.clearhighscores:
                helper.clearHighScores();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Terminates the application
     */
    private void halt() {
        getActivity().finish();
        System.exit(0);
    }

    // when MainActivity is paused, CatGameFragment terminates the game
    @Override
    public void onPause() {
        super.onPause();

    }

    // when MainActivity is paused, CatGameFragment releases resources
    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    /**
     * Shows "About" dialog
     */

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.highscores);
        StringBuffer message = new StringBuffer();

        ArrayList<HighScore> highscores = helper.getHighscores();

        if (highscores.isEmpty()) {
            message.append(getActivity().getString(R.string.msg_hs_list_is_empty));
        } else {

            for (HighScore highScore : highscores) {
                message.append(highScore.toString()).append("\n");
            }
        }
        builder.setMessage(message);


        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

}
