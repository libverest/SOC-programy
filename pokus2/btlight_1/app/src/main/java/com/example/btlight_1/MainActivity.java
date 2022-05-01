package com.example.btlight_1;

import android.Manifest;
import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter BA;
    public String[] permListLegacy;
    public String[] permListNew;
    public String[] permListAll;
    public ImageButton button;
    public boolean lightOn;
    public boolean debugFlag;
    public boolean discoveryFlag = false;
    public boolean permissionFlag = true;
    public boolean receiverReadyFlag = false;
    public boolean screenOffFlag = false;
    public BluetoothDevice device;
    public ArrayList<BluetoothDevice> deviceList;
    public TextView debugTextView;
    public TextView logo;
    public ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        lightOn = false;
        button = findViewById(R.id.button);
        debugTextView = findViewById(R.id.debug);
        logo = findViewById(R.id.text);
        listView = findViewById(R.id.setuplist);
        deviceList = new ArrayList<BluetoothDevice>();
        permListLegacy = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        permListNew = new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
        permListAll = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lightOn = !lightOn;
                if (lightOn) {
                    button.setImageResource(R.drawable.ic_light_bulb_on);
                    Log.d("COCKSFUCKS", "yo");
                }
                else {
                    button.setImageResource(R.drawable.ic_light_bulb_off);
                }
            }
        });
        debugFlag = false;
        logo.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (debugFlag) {
                        debugTextView.setVisibility(View.VISIBLE);
                        return true;
                    }
                    else {
                        debugTextView.setVisibility(View.INVISIBLE);
                        return true;
                    }
                }
                debugFlag = !debugFlag;
                return false;
            }
        });
        debugTextView.setMovementMethod(new ScrollingMovementMethod());
        SpannableString spannable = new SpannableString()
        onCheckPermissions();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 & (Arrays.equals(permissions, permListLegacy) | Arrays.equals(permissions, permListAll))  & !Arrays.asList(grantResults).contains(PackageManager.PERMISSION_DENIED)) {
            Log.d("COCKSFUCKS", "why");
            logDebug("granted!");
            onGranted();
        }
        else {
            Log.d("COCKSFUCKS", "loop");
            logDebug("not granted!");
            Log.d("COCKSFUCKS", Boolean.toString(!Arrays.asList(grantResults).contains(PackageManager.PERMISSION_DENIED)));

            onCheckPermissions();
        }
    }
    public void onCheckPermissions() {
        Log.d("COCKSFUCKS", "0");
        for (int i = 0; i < permListLegacy.length; i++) {
            if (ActivityCompat.checkSelfPermission(this, permListLegacy[i]) == PackageManager.PERMISSION_DENIED) {
                permissionFlag = false;
                Log.d("COCKSFUCKS", "1a");
            }
            else {
                Log.d("COCKSFUCKS", "2a");
            }
            Log.d("COCKSFUCKS", Integer.toString(permListLegacy.length));
        }
        logDebug(Integer.toString(Build.VERSION.SDK_INT));
        if (Build.VERSION.SDK_INT > 30) {
            for (int i = 0; i < permListNew.length; i++) {
                if (ActivityCompat.checkSelfPermission(this, permListNew[i]) == PackageManager.PERMISSION_DENIED) {
                    permissionFlag = false;
                    Log.d("COCKSFUCKS", "1b");
                }
                else {
                    Log.d("COCKSFUCKS", "2b");
                }
            }
        }
        if (!permissionFlag) {
            if (Build.VERSION.SDK_INT > 30) {
                ActivityCompat.requestPermissions(this, permListAll, 1);
            }
            else {
                ActivityCompat.requestPermissions(this, permListLegacy, 1);
            }
        }
        else {
            logDebug("granted!");
            onGranted();
        }
    }
    public void onGranted() {
        BA = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);
        receiverReadyFlag = true;
        Log.d("COCKSFUCKS", "receiving");
        logDebug("receiving");
        onReceiving();
    }
    public void logDebug(String s) {
        debugTextView.setText(debugTextView.getText() + s + "\n");
    }

    public void logDebugArray(String[] s) {
        for (int i = 0; i < s.length; i++) {
            debugTextView.setText(debugTextView.getText() + s[i] + "\n");
        }
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                logDebug("FOund device");
                logDebugDevices(new BluetoothDevice[]{device});
                addDevice(device);
                addDevicesToUI();
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action) & BA.getState() == BluetoothAdapter.STATE_ON) {
                onBluetooth();
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                logDebug(action);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                logDebug("Discovery started!");
                discoveryFlag = true;
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                logDebug("Discovery finished!");
                addDevicesToUI();
                discoveryFlag = false;
            }
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                logDebug("screen off");
                screenOffFlag = true;
            }
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                logDebug("screen on");
                screenOffFlag = false;
            }
        }
    };
    public void onReceiving() {
        Log.d("COCKSFUCKS", "no");
        if (receiverReadyFlag) {
            if (!BA.isEnabled()) {
                BA.enable();
                Log.d("COCKSFUCKS", "bro");
            }
            else {
                onBluetooth();
                Log.d("COCKSFUCKS", "supp");
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onResume() {
        super.onResume();
        onReceiving();
    }
    private void addDevicesToUI() {
        String[] devNames = new String[deviceList.size()];
        for (int i = 0; i < deviceList.size(); i++) {
            devNames[i] = deviceList.get(i).getName();
        }
        logDebugArray(devNames);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, devNames);
        listView.setAdapter(adapter);

    }

    public void addDevice(BluetoothDevice d) {
        if (!deviceList.contains(d)) {
            deviceList.add(d);
        }
    }
    private View dividerLine() {
        View v = (View) findViewById(R.id.line);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        relativeParams.removeRule(RelativeLayout.ABOVE);
        v.setLayoutParams(relativeParams);
        RelativeLayout setup = (RelativeLayout) findViewById(R.id.setup);
        setup.addView(v);
        return v;
    }
    private void logDebugDevices(BluetoothDevice[] d) {
        for (int i = 0; i < d.length; i++) {
            logDebug(d[i].getName() + d[i].getAddress() + "L");
        }
    }

    private void onBluetooth() {
        logDebug("BT on!");
        Log.d("COCKSFUCKS", "lol");
        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
        logDebug(pairedDevices.toString());
        Log.d("COCKSFUCKS", pairedDevices.toString());

        logDebugDevices(pairedDevices.toArray(new BluetoothDevice[0]));

        try {
            if(!discoveryFlag) {
                BA.startDiscovery();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            logDebug("Discovery error!");

            logDebug(e.toString());
        }
    }

    private void onDiscovered() {

    }

    @Override
    public void onPause() {
        super.onPause();
        logDebug(Boolean.toString(screenOffFlag));
        if(!screenOffFlag) {
            BA.disable();
            Log.d("COCKSFUCKS", "bye");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

}
