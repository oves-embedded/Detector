package com.ov.producer.service;

import static com.ov.producer.enums.EventBusTagEnum.BLE_INIT_ERROR;
import static com.ov.producer.enums.EventBusTagEnum.NOT_ENABLE_LE;
import static com.ov.producer.enums.EventBusTagEnum.NOT_SUPPORT_LE;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.ov.producer.application.MyApplication;
import com.ov.producer.constants.ReturnResult;
import com.ov.producer.dao.BleCheckRecordDao;
import com.ov.producer.entity.BleCheckRecord;
import com.ov.producer.entity.BleServiceDataDto;
import com.ov.producer.entity.CharacteristicDataDto;
import com.ov.producer.entity.DataConvert;
import com.ov.producer.entity.DescriptorDataDto;
import com.ov.producer.entity.EventBusMsg;
import com.ov.producer.entity.OvAttrDto;
import com.ov.producer.enums.EventBusTagEnum;
import com.ov.producer.enums.ServiceNameEnum;
import com.ov.producer.utils.BleDeviceUtil;
import com.ov.producer.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BleService extends Service {

    private Map<String, BluetoothDevice> bleDeviceMap = new ConcurrentHashMap<>();

    private Map<String, BleCheckRecord> bleDeviceInfoMap = new ConcurrentHashMap<>();

    private BluetoothAdapter bluetoothAdapter;
    //low power ble
    private BluetoothLeScanner bluetoothLeScanner;

    @Override
    public void onCreate() {
        super.onCreate();
        initBleConfig();
    }


    public void initBleConfig() {
        try {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                EventBus.getDefault().post(new EventBusMsg(NOT_SUPPORT_LE, null));
                return;
            }
            if (!bluetoothAdapter.isEnabled()) {
                EventBus.getDefault().post(new EventBusMsg(NOT_ENABLE_LE, null));
                return;
            }
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        } catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new EventBusMsg(BLE_INIT_ERROR, e.getMessage()));
        }
    }

    public BleCheckRecord findSuffKeywordBle(String suffKeyword){
        Collection<BleCheckRecord> values = bleDeviceInfoMap.values();
        Iterator<BleCheckRecord> iterator = values.iterator();
        while(iterator.hasNext()){
            BleCheckRecord next = iterator.next();
            if(next.getBleName().endsWith(suffKeyword)){
                return next;
            }
        }
        return null;
    }


    public BleDeviceUtil connectBle(String mac) throws Exception {
        BluetoothDevice bleDevice = bleDeviceMap.get(mac);
        if (bleDevice != null) {
            BleDeviceUtil bleDeviceUtil = new BleDeviceUtil(bleDevice, BleService.this);
            boolean b = bleDeviceUtil.connectGatt();
            if (b) {
                bleDeviceUtil.setMtu(100);
                return bleDeviceUtil;
            }
            bleDeviceUtil.destroy();
        }
        return null;
    }

    public Map<String, OvAttrDto> checkData(BleDeviceUtil bleDeviceUtil) {
        Map<String, OvAttrDto> dtoMap = null;
        if (bleDeviceUtil != null) {
            Map<String, BleServiceDataDto> serviceDataDtoMap = bleDeviceUtil.getServiceDataDtoMap();
            LogUtil.info("!!!!!!!!!!checkData:" + JSON.toJSONString(serviceDataDtoMap));
            Set<String> keySet = serviceDataDtoMap.keySet();
            for (String serviceUUID : keySet) {
                BleServiceDataDto bleServiceDataDto = serviceDataDtoMap.get(serviceUUID);
                if (serviceUUID.startsWith(ServiceNameEnum.DIA_SERVICE_NAME.getPrefixCode()) || serviceUUID.startsWith(ServiceNameEnum.ATT_SERVICE_NAME.getPrefixCode())) {
                    Map<String, CharacteristicDataDto> characteristicDataMap = bleServiceDataDto.getCharacteristicDataMap();
                    if (characteristicDataMap != null) {
                        Collection<CharacteristicDataDto> values = characteristicDataMap.values();
                        for (CharacteristicDataDto characteristicDataDto : values) {
                            ReturnResult<CharacteristicDataDto> characteristicDataDtoReturnResult = bleDeviceUtil.readCharacteristic(serviceUUID, characteristicDataDto.getCharacteristicUUID());
                            Map<String, DescriptorDataDto> descriptorDataMap = characteristicDataDto.getDescriptorDataMap();
                            if (descriptorDataMap != null) {
                                Collection<DescriptorDataDto> descriptorDataDtos = descriptorDataMap.values();
                                for (DescriptorDataDto descriptorDataDto : descriptorDataDtos) {
                                    ReturnResult<DescriptorDataDto> descriptorDataDtoReturnResult = bleDeviceUtil.readDescriptor(serviceUUID, characteristicDataDto.getCharacteristicUUID(), descriptorDataDto.getDescriptorUUID());

                                    if (descriptorDataDtoReturnResult.ok()) {
                                        byte[] data = descriptorDataDto.getData();
                                        if (data != null && data.length > 0) {
                                            String valStr = new String(data, StandardCharsets.US_ASCII);
                                            if (valStr != null && valStr.trim().length() > 0 && valStr.contains(":")) {
                                                String[] split = valStr.split(":");
                                                if (split.length >= 3) {
                                                    characteristicDataDto.setName(split[0]);
                                                    characteristicDataDto.setValType(Integer.valueOf(split[1]));
                                                    characteristicDataDto.setDesc(split[2]);
//                                                    if(characteristicDataDtoReturnResult.ok()&&characteristicDataDtoReturnResult.getData()!=null&&characteristicDataDtoReturnResult.getData().getData().length>0){
                                                    if (characteristicDataDto.getData() != null && characteristicDataDto.getData().length > 0) {
                                                        Object o = DataConvert.convert2Obj(characteristicDataDto.getData(), characteristicDataDto.getValType());
                                                        characteristicDataDto.setRealVal(o);
                                                    }
                                                    if (dtoMap == null) {
                                                        dtoMap = new HashMap<>();
                                                    }
                                                    OvAttrDto ovAttrDto = new OvAttrDto();
                                                    ovAttrDto.setName(characteristicDataDto.getName());
                                                    ovAttrDto.setValue(characteristicDataDto.getRealVal());
                                                    ovAttrDto.setDesc(characteristicDataDto.getDesc());
                                                    ovAttrDto.setValType(characteristicDataDto.getValType());
                                                    ovAttrDto.setServiceUUID(characteristicDataDto.getParentServiceUUID());
                                                    ovAttrDto.setCharacteristicUUID(characteristicDataDto.getCharacteristicUUID());
                                                    dtoMap.put(characteristicDataDto.getName(), ovAttrDto);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            LogUtil.info(serviceUUID + ">" + characteristicDataDto.getCharacteristicUUID() + ":" + JSON.toJSONString(dtoMap));
                        }
                    }
                }
            }
        }
        return dtoMap;
    }


    public List<BleCheckRecord> getBleList() {
        return new ArrayList<>(bleDeviceInfoMap.values());
    }

    @SuppressLint("MissingPermission")
    public void startBleScan() {
        bleDeviceMap.clear();
        bleDeviceInfoMap.clear();
        EventBus.getDefault().post(new EventBusMsg(EventBusTagEnum.BLE_FIND, new ArrayList<>(bleDeviceInfoMap.values())));
        if (bluetoothLeScanner == null) {
            initBleConfig();
        }
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.startScan(scanCallback);
        }
    }


    public ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
//            Log.e("scanBle->onScanStarted", device.getAddress() + "," + device.getName());
            String bleName = device.getName();
            if (!TextUtils.isEmpty(bleName)) {
                bleName = bleName.trim();
                if (!bleName.startsWith("OV")) {
                    return;
                }
                String typeStr = "Unknown";
                if (device.getType() == 1) {
                    typeStr = "Classic";
                } else if (device.getType() == 2) {
                    typeStr = "Low Energy";
                } else if (device.getType() == 3) {
                    typeStr = "DUAL";
                }
                BleCheckRecord checkRecord = new BleCheckRecord();
                checkRecord.setMac(device.getAddress());
                checkRecord.setBleName(bleName);
                String[] nameArr = bleName.split(" ");
                if (nameArr.length >= 3) {
                    checkRecord.setProductName(nameArr[1]);
                    checkRecord.setSuffOpid(nameArr[2]);
                } else if (nameArr.length == 2) {
                    checkRecord.setProductName(nameArr[1]);
                    checkRecord.setSuffOpid("");
                }
                bleDeviceMap.put(device.getAddress(), device);
                if (bleDeviceInfoMap.containsKey(device.getAddress())) {
//                    LogUtil.info("==========find device/update device info" + JSON.toJSONString(checkRecord) + "==========");
                } else {
//                    LogUtil.info("==========find new device" + JSON.toJSONString(checkRecord) + "==========");
                }
                bleDeviceInfoMap.put(device.getAddress(), checkRecord);
                EventBus.getDefault().post(new EventBusMsg(EventBusTagEnum.BLE_FIND, new ArrayList<>(bleDeviceInfoMap.values())));
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            LogUtil.debug("ScanCallback==>onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            LogUtil.debug("ScanCallback==>onScanFailed");
        }
    };

    @SuppressLint("MissingPermission")
    public void stopScan() {
        try {
            bluetoothLeScanner.stopScan(scanCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new BleServiceBinder();
    }

    public class BleServiceBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }
}
