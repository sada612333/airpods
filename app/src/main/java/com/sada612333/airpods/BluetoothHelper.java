package com.sada612333.airpods;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.receiver.listener.BluetoothBondListener;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static com.sada612333.airpods.Constants.BOND_STATE;
import static com.sada612333.airpods.Constants.FREQ;
import static com.sada612333.airpods.Constants.TAG;


public class BluetoothHelper {

    private static BluetoothClient mClient = null;
    private static Context context;

    private static Thread deviceThread = new Thread(new DeviceThread());


    private static final BluetoothStateListener mBluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            if(openOrClosed){
                Log.i(TAG,"蓝牙已打开.");
                DeviceThread.bluetoothOpened = true;
                deviceThread.interrupt();
            }else {
                Log.i(TAG,"蓝牙已关闭.");
                DeviceThread.bluetoothOpened = false;
            }
        }

    };
    private static final BluetoothBondListener mBluetoothBondListener = new BluetoothBondListener() {
        @Override
        public void onBondStateChanged(String mac, int bondState) {
            Log.i(TAG,String.format("蓝牙%s绑定状态%s.",mac,bondState));
            if(BOND_STATE == bondState){
                Log.i(TAG,String.format("蓝牙设备%s已绑定.",mac));
            }
        }
    };

    public static void openBluetooth(){
        if(mClient == null){
            Log.i(TAG,"初始化失敗.");
            return;
        }
        Log.i(TAG,String.format("设备支持LE:%s.",mClient.isBleSupported()));
        if(!mClient.isBluetoothOpened()){
            mClient.openBluetooth();
            Log.i(TAG,"正在打开蓝牙...");
        }else{
            DeviceThread.bluetoothOpened = true;
            Log.i(TAG,"蓝牙已打开...");
        }
        if(deviceThread != null){
            if(!deviceThread.isAlive()){
                deviceThread.start();
            }
        }
    }

    public synchronized static void init(Context context){
        if(null == mClient){
            mClient = new BluetoothClient(context);
        }
        registerBluetoothStateListener(true);
        registerBluetoothBondListener(true);
        //scanDevices();
    }



    public static void registerBluetoothStateListener(boolean registerOrUnRegister){
        if(mClient == null){
            return;
        }
        if(registerOrUnRegister){
            mClient.registerBluetoothStateListener(mBluetoothStateListener);
        }else {
            mClient.unregisterBluetoothStateListener(mBluetoothStateListener);
        }
    }



    public static void registerBluetoothBondListener(boolean registerOrUnRegister){
        if(mClient == null){
            return;
        }
        if(registerOrUnRegister){
            mClient.registerBluetoothBondListener(mBluetoothBondListener);
        }else {
            mClient.unregisterBluetoothBondListener(mBluetoothBondListener);
        }
    }

    @Deprecated
    public static void scanDevices(){
        if(mClient == null){
            return;
        }
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Log.i(TAG,"正在扫描蓝牙设备...");
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                Beacon beacon = new Beacon(device.scanRecord);
                //BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));
                Log.i(TAG,String.format("已扫到蓝牙设备:\n%s\t%s\n.",device.getAddress(),device.getName()));
                if(mClient.getBondState(device.getAddress())==BOND_STATE){
                    Log.i(TAG,String.format("设备已绑定."));
                    //Log.i(TAG,String.format("蓝牙设备连接状态:\n%s\t%s\n.",device.getAddress(),mClient.getConnectStatus(device.getAddress())));
                }

            }

            @Override
            public void onSearchStopped() {
                Log.i(TAG,"扫描蓝牙设备已停止...");
            }

            @Override
            public void onSearchCanceled() {
                Log.i(TAG,"扫描蓝牙设备已取消...");
            }
        });
    }


    static class  DeviceThread implements Runnable{

        public static boolean bluetoothOpened = false;

        public DeviceThread(){
            super();
            Log.i(TAG,"检查连接设备线程初始化...");
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(TAG,"检查连接设备线程正在运行...");
            while(true){
                if(bluetoothOpened){
                    Log.i(TAG,"\n检查已连接设备...\n");
                    Log.i(TAG,"\n<------ClassDevices-----\n");
                    List<BluetoothDevice> classDeviceList = BluetoothUtils.getBondedBluetoothClassicDevices();
                    this.printInfo(classDeviceList);
                    Log.i(TAG,"\n-----ClassDevices----->\n");

                    Log.i(TAG,"\n<-----LeDevices-----\n");
                    List<BluetoothDevice> deviceList = BluetoothUtils.getConnectedBluetoothLeDevices();
                    this.printInfo(deviceList);
                    Log.i(TAG,"\n-------LeDevices---->\n");
                    Log.i(TAG,"\n检查已连接设备完成.\n");
                    try {
                        Thread.sleep(FREQ);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.i(TAG,"蓝牙未打开，进入休眠状态...");
                    try {
                        Thread.sleep(100000);
                    } catch (InterruptedException e) {
                        Log.i(TAG,"休眠被打断...");
                        e.printStackTrace();
                    }
                }
            }
        }

        private void printInfo(List<BluetoothDevice> list){
            Optional.ofNullable(list).get().stream().forEach(o -> {
                Log.i(TAG,String.format("type:%s\n",o.getType()));
                Log.i(TAG,String.format("UUIDs length:%s\n",o.getUuids().length));
                Log.i(TAG,String.format("UUIDs:%s\n", Stream.of(o.getUuids()).map(i -> i.toString()).collect(Collectors.joining(","))));
                Log.i(TAG,String.format("bondStat:%s\n",o.getBondState()));
                if(BOND_STATE == o.getBondState()){
                    Log.i(TAG,String.format("connectStat:%s\n",BluetoothUtils.getConnectStatus(o.getAddress())));
                }
                //getGattInfo(o);
                Log.i(TAG,"\n>>>>>>>>>>>>>>>>\n");

            });
        }

        private void getGattInfo(BluetoothDevice device){
            Log.i(TAG,String.format("正在检查设备:%s\n",device.getAddress()));
            Stream.of(Optional.ofNullable(device).get().getUuids()).forEach(o -> {
                Log.i(TAG,String.format("正在检查uuid:%s\n",o.getUuid().toString()));
                mClient.read(device.getAddress(), o.getUuid(), o.getUuid(), new BleReadResponse() {
                    @Override
                    public void onResponse(int code, byte[] data) {
                        Log.i(TAG,String.format("response code:%s\n",code));
                        Log.i(TAG,String.format("response data:%s\n",data));
                        if (code == REQUEST_SUCCESS) {
                        }
                    }
                });
            });
        }
    }
}
