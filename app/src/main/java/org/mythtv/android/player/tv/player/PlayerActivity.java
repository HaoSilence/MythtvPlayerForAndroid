package org.mythtv.android.player.tv.player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import org.mythtv.android.R;
import org.mythtv.android.library.core.MainApplication;
import org.mythtv.android.library.core.utils.Utils;
//import VideoDetailsActivity;

import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends Activity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    public static final String FULL_URL_TAG = "full_url";

    private static final int HIDE_CONTROLLER_TIME = 5000;
    private static final int SEEKBAR_DELAY_TIME = 100;
    private static final int SEEKBAR_INTERVAL_TIME = 1000;
    private static final int MIN_SCRUB_TIME = 3000;
    private static final int SCRUB_SEGMENT_DIVISOR = 30;
    private static final double MEDIA_BAR_TOP_MARGIN = 0.8;
    private static final double MEDIA_BAR_RIGHT_MARGIN = 0.2;
    private static final double MEDIA_BAR_BOTTOM_MARGIN = 0.0;
    private static final double MEDIA_BAR_LEFT_MARGIN = 0.2;
    private static final double MEDIA_BAR_HEIGHT = 0.1;
    private static final double MEDIA_BAR_WIDTH = 0.9;

    private VideoView mVideoView;
    private TextView mStartText;
    private TextView mEndText;
    private SeekBar mSeekbar;
    private ImageView mPlayPause;
    private ProgressBar mLoading;
    private View mControllers;
    private View mContainer;
    private Timer mSeekbarTimer;
    private Timer mControllersTimer;
    private Timer mGetLiveStreamTimer;
    private PlaybackState mPlaybackState;
    private final Handler mHandler = new Handler();
    private boolean mShouldStartPlayback;
    private boolean mControlersVisible;
    private int mDuration;
    private DisplayMetrics mMetrics;

    private String mFileUrl;

    /*
     * List of various states that we can be in
     */
    public static enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i( TAG, "onCreate : enter" );

        setContentView( R.layout.activity_tv_player);

        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        loadViews();
        setupController();
        setupControlsCallbacks();

        mFileUrl = getIntent().getStringExtra( FULL_URL_TAG );
        startVideoPlayerHls();
        updateMetadata( true );

    }

    private void startVideoPlayerHls() {
        Log.i( TAG, "startVideoPlayerHls : enter" );

        Bundle b = getIntent().getExtras();
        if (null != b) {
            mShouldStartPlayback = b.getBoolean(getResources().getString(R.string.should_start));
            int startPosition = b.getInt(getResources().getString(R.string.start_position), 0);

            Log.i( TAG, "startVideoPlayerHls : mFileUrl=" + mFileUrl );
            String url = MainApplication.getInstance().getMasterBackendUrl() + mFileUrl.substring( 1 );
            Log.i( TAG, "startVideoPlayerHls : url=" + url );
            mVideoView.setVideoURI( Uri.parse( url ) );

            if (mShouldStartPlayback) {
                mPlaybackState = PlaybackState.PLAYING;
                updatePlayButton(mPlaybackState);
                if (startPosition > 0) {
                    mVideoView.seekTo(startPosition);
                }
                mVideoView.start();
                mPlayPause.requestFocus();
                startControllersTimer();
            } else {
                updatePlaybackLocation();
                mPlaybackState = PlaybackState.PAUSED;
                updatePlayButton(mPlaybackState);
            }
        }
    }

    private void updatePlaybackLocation() {
        Log.i( TAG, "updatePlaybackLocation : enter" );

        if (mPlaybackState == PlaybackState.PLAYING ||
                mPlaybackState == PlaybackState.BUFFERING) {
            startControllersTimer();
        } else {
            stopControllersTimer();
        }
    }

    private void play(int position) {
        Log.i( TAG, "play : enter" );

        startControllersTimer();
        mVideoView.seekTo(position);
        mVideoView.start();
        restartSeekBarTimer();
    }

    private void stopSeekBarTimer() {
        Log.d(TAG, "Stopped TrickPlay Timer");

        if (null != mSeekbarTimer) {
            mSeekbarTimer.cancel();
        }
    }

    private void restartSeekBarTimer() {
        stopSeekBarTimer();
        mSeekbarTimer = new Timer();
        mSeekbarTimer.scheduleAtFixedRate(new UpdateSeekbarTask(), SEEKBAR_DELAY_TIME,
                SEEKBAR_INTERVAL_TIME);
    }

    private void stopControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
    }

    private void startControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
        mControllersTimer = new Timer();
        mControllersTimer.schedule(new HideControllersTask(), HIDE_CONTROLLER_TIME);
    }

    private void updateControlersVisibility(boolean show) {
        if (show) {
            mControllers.setVisibility(View.VISIBLE);
        } else {
            mControllers.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() was called");
        if (null != mSeekbarTimer) {
            mSeekbarTimer.cancel();
            mSeekbarTimer = null;
        }
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
        if( null != mGetLiveStreamTimer ) {
            mGetLiveStreamTimer.cancel();
        }
        mVideoView.pause();
        mPlaybackState = PlaybackState.PAUSED;
        updatePlayButton(PlaybackState.PAUSED);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() was called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() is called");

        stopControllersTimer();
        stopSeekBarTimer();

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart was called");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        super.onResume();
    }

    private class HideControllersTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateControlersVisibility(false);
                    mControlersVisible = false;
                }
            });

        }
    }

    private class UpdateSeekbarTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    int currentPos = 0;
                    currentPos = mVideoView.getCurrentPosition();
                    updateSeekbar(currentPos, mDuration);
                }
            });
        }
    }

//    private class BackToDetailTask extends TimerTask {
//
//        @Override
//        public void run() {
//            mHandler.post( new Runnable() {
//
//                @Override
//                public void run() {
//
//                    if( null != mSelectedProgram ) {
//                        Intent intent = new Intent( PlayerActivity.this, RecordingDetailsActivity.class );
//                        intent.putExtra( getResources().getString( R.string.recording ), mSelectedProgram );
//                        startActivity(intent);
//                    }
//
//                    if( null != mSelectedVideo ) {
//                        Intent intent = new Intent( PlayerActivity.this, VideoDetailsActivity.class );
//                        intent.putExtra( getResources().getString( R.string.video ), mSelectedVideo );
//                        startActivity( intent );
//                    }
//
//                }
//            });
//
//        }
//    }

    private void setupController() {

        int w = (int) (mMetrics.widthPixels * MEDIA_BAR_WIDTH);
        int h = (int) (mMetrics.heightPixels * MEDIA_BAR_HEIGHT);
        int marginLeft = (int) (mMetrics.widthPixels * MEDIA_BAR_LEFT_MARGIN);
        int marginTop = (int) (mMetrics.heightPixels * MEDIA_BAR_TOP_MARGIN);
        int marginRight = (int) (mMetrics.widthPixels * MEDIA_BAR_RIGHT_MARGIN);
        int marginBottom = (int) (mMetrics.heightPixels * MEDIA_BAR_BOTTOM_MARGIN);
        LayoutParams lp = new LayoutParams(w, h);
        lp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
        mControllers.setLayoutParams(lp);
        mStartText.setText(getResources().getString(R.string.init_text));
        mEndText.setText(getResources().getString(R.string.init_text));
    }

    private void setupControlsCallbacks() {

        mVideoView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String msg = "";
                if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = getString(R.string.video_error_media_load_timeout);
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = getString(R.string.video_error_server_unaccessible);
                } else {
                    msg = getString(R.string.video_error_unknown_error);
                }
                mVideoView.stopPlayback();
                mPlaybackState = PlaybackState.IDLE;
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "onPrepared is reached");
                mDuration = mp.getDuration();
                mEndText.setText(Utils.formatMillis(mDuration));
                mSeekbar.setMax(mDuration);
                restartSeekBarTimer();
            }
        });

        mVideoView.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopSeekBarTimer();
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(PlaybackState.IDLE);
//                mControllersTimer = new Timer();
//                mControllersTimer.schedule(new BackToDetailTask(), HIDE_CONTROLLER_TIME);

                PlayerActivity.this.finish();
            }
        });
    }

    /*
     * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { return
     * super.onKeyDown(keyCode, event); }
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int currentPos = 0;
        int delta = (int) (mDuration / SCRUB_SEGMENT_DIVISOR);
        if (delta < MIN_SCRUB_TIME)
            delta = MIN_SCRUB_TIME;

        Log.v("keycode", "duration " + mDuration + " delta:" + delta);
        if (!mControlersVisible) {
            updateControlersVisibility(true);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                currentPos = mVideoView.getCurrentPosition();
                currentPos -= delta;
                if (currentPos > 0)
                    play(currentPos);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                currentPos = mVideoView.getCurrentPosition();
                currentPos += delta;
                if (currentPos < mDuration)
                    play(currentPos);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void updateSeekbar(int position, int duration) {
        mSeekbar.setProgress(position);
        mSeekbar.setMax(duration);
        mStartText.setText(Utils.formatMillis(position));
        mEndText.setText(Utils.formatMillis(duration));
    }

    private void updatePlayButton(PlaybackState state) {
        switch (state) {
            case PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_pause_playcontrol_normal));
                break;
            case PAUSED:
            case IDLE:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_play_playcontrol_normal));
                break;
            case BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void updateMetadata(boolean visible) {
        mVideoView.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    private void loadViews() {
        mVideoView = (VideoView) findViewById( R.id.videoView );
        mStartText = (TextView) findViewById( R.id.startText );
        mEndText = (TextView) findViewById( R.id.endText );
        mSeekbar = (SeekBar) findViewById( R.id.seekBar );
        mPlayPause = (ImageView) findViewById( R.id.playpause );
        mLoading = (ProgressBar) findViewById( R.id.progressBar );
        mControllers = findViewById( R.id.controllers );
        mContainer = findViewById( R.id.container );

        mVideoView.setOnClickListener( mPlayPauseHandler );
        mSeekbar.setOnSeekBarChangeListener( mSeekBarChangedHandler );
    }

    View.OnClickListener mPlayPauseHandler = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "clicked play pause button");

            if (!mControlersVisible) {
                updateControlersVisibility(true);
            }

            if (mPlaybackState == PlaybackState.PAUSED) {
                mPlaybackState = PlaybackState.PLAYING;
                updatePlayButton(mPlaybackState);
                mVideoView.start();
                startControllersTimer();
            } else {
                mVideoView.pause();
                mPlaybackState = PlaybackState.PAUSED;
                updatePlayButton(PlaybackState.PAUSED);
                stopControllersTimer();
            }
        }
    };

    SeekBar.OnSeekBarChangeListener mSeekBarChangedHandler = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser ) {

        }

        @Override
        public void onStartTrackingTouch( SeekBar seekBar ) {

        }

        @Override
        public void onStopTrackingTouch( SeekBar seekBar ) {

            play( seekBar.getProgress() );

//            int currentPos = 0;
//            int delta = (int) ( mDuration / SCRUB_SEGMENT_DIVISOR );
//            if( delta < MIN_SCRUB_TIME ) {
//                delta = MIN_SCRUB_TIME;
//            }
//
//            currentPos = mVideoView.getCurrentPosition();
//            if( currentPos > seekBar.getProgress() ) {
//
//                Log.i( TAG, "onStopTrackingTouch : currentPos=" + currentPos + ", seek to=" + (currentPos -= delta) );
//                currentPos -= delta;
//                if( currentPos > 0 ) {
//                    play( currentPos );
//                } else {
//                    play( 0 );
//                }
//
//            } else {
//
//                Log.i( TAG, "onStopTrackingTouch : currentPos=" + currentPos + ", seek to=" + (currentPos += delta) );
//                currentPos += delta;
//                if( currentPos < mDuration ) {
//                    play( currentPos );
//                }
//
//            }
//
        }

    };

}
