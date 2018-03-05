package com.google.sample.echo;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.media.AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED;
import static android.media.AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE;
import static android.media.AudioManager.EXTRA_SCO_AUDIO_STATE;
import static android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED;
import static android.media.AudioManager.SCO_AUDIO_STATE_DISCONNECTED;

public class BluetoothScoConnector {
    private static final String TAG = "ASR";

    private final AudioManager audioManager;

    @Nullable
    private final BluetoothAdapter bluetoothAdapter;

    @Nullable
    private OnScoAttemptedListener listener;

    public BluetoothScoConnector(Context context,
                                 AudioManager audioManager,
                                 @Nullable BluetoothAdapter bluetoothAdapter) {
        this.audioManager = audioManager;
        this.bluetoothAdapter = bluetoothAdapter;
        // this.audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        BroadcastReceiver broadcastReceiver = new AudioStateChangedBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_SCO_AUDIO_STATE_UPDATED);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void startBluetoothSco(OnScoAttemptedListener listener) {
        this.listener = listener;

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            audioManager.startBluetoothSco();
        } else {
            Log.e(TAG, "Bluetooth not available; skipping.");
            this.listener.onBluetoothScoAttempted(false);
            this.listener = null;
        }
    }

    public void stopBluetoothSco() {
        if (bluetoothAdapter != null) {
            audioManager.stopBluetoothSco();
        }
    }

    private void scoConnected() {
        if (listener != null) {
            listener.onBluetoothScoAttempted(true);
        } else {
            Log.d(TAG, "Bluetooth SCO connected.");
        }

        listener = null;
    }

    private void scoDisconnected() {
        if (listener != null) {
            listener.onBluetoothScoAttempted(false);
        } else {
            Log.d(TAG, "Bluetooth SCO disconnected.");
        }

        listener = null;
    }

    class AudioStateChangedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int previousState = intent.getIntExtra(EXTRA_SCO_AUDIO_PREVIOUS_STATE, SCO_AUDIO_STATE_DISCONNECTED);
            int state = intent.getIntExtra(EXTRA_SCO_AUDIO_STATE, SCO_AUDIO_STATE_DISCONNECTED);
            Log.d(TAG, "Bluetooth SCO state change received. Previous: " + previousState + ", current: " + state);

            switch (state) {
                case SCO_AUDIO_STATE_CONNECTED:
                    scoConnected();
                    break;
                case SCO_AUDIO_STATE_DISCONNECTED:
                    scoDisconnected();
                    break;
            }
        }
    }

    public interface OnScoAttemptedListener {
        void onBluetoothScoAttempted(boolean succeeded);
    }
}
