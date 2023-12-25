package com.ov.producer.utils;

import static android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;


import com.ov.producer.constants.ReturnResult;
import com.ov.producer.entity.BleServiceDataDto;
import com.ov.producer.entity.CharacteristicDataDto;
import com.ov.producer.entity.DescriptorDataDto;


import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public class BleDeviceUtil {

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));

    /**
     * Created by dsr on 2023/10/11.
     */
    private Context context;
    private boolean connected;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;
    private static final long TIME_OUT = 3000;
    private CountDownLatch countDownLatch;
    private Map<String, BleServiceDataDto> serviceDataDtoMap = null;

    private StringBuffer notifyBuff = new StringBuffer();

    public BleDeviceUtil(BluetoothDevice bluetoothDevice, Context context) {
        this.bluetoothDevice = bluetoothDevice;
        this.context = context;
        this.connected = false;
        serviceDataDtoMap = new ConcurrentHashMap<>();
    }

    /**
     * 连接蓝牙
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public synchronized boolean connectGatt() throws Exception {
        if (serviceDataDtoMap == null) throw new Exception("connectGatt");
        countDownLatch = new CountDownLatch(1);
        //autoConnect==false,表示立即发起连接，否则等蓝牙空闲才会连接
        bluetoothDevice.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_AUTO);
        try {
            countDownLatch.await(10000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connected;
    }

    @SuppressLint("MissingPermission")
    public synchronized void setMtu(int maxByteSize) throws ExecutionException, InterruptedException {
        bluetoothGatt.requestMtu(maxByteSize);
        try {
            countDownLatch = new CountDownLatch(1);
            countDownLatch.await(TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public synchronized ReturnResult<Void> writeCharacteristic(String serviceUUID, String characteristicUUID, byte[] value) throws ExecutionException, InterruptedException {
        notifyBuff.delete(0, notifyBuff.length());
        if (connected) {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
                if (characteristic != null) {
                    characteristic.setValue(value);
                    CompletableFuture<ReturnResult<Void>> future = CompletableFuture.supplyAsync(new Supplier<ReturnResult<Void>>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public ReturnResult<Void> get() {
                            countDownLatch = new CountDownLatch(1);
                            bluetoothGatt.writeCharacteristic(characteristic);
                            try {
                                countDownLatch.await(TIME_OUT, TimeUnit.MILLISECONDS);
                                return ReturnResult.success();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return ReturnResult.fail(e.getMessage());
                            }
                        }
                    }, executor);
                    return future.get();
                } else {
                    return ReturnResult.fail("未查找到对应的GattCharacteristic！");
                }
            } else {
                return ReturnResult.fail("未查找到对应的GattService！");
            }
        } else {
            return ReturnResult.fail("蓝牙已断开，请重新连接！");
        }
    }

    @SuppressLint("MissingPermission")
    public synchronized ReturnResult<CharacteristicDataDto> readCharacteristic(String serviceUUID, String characteristicUUID) {
        if (connected) {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
                if (characteristic != null) {
                    countDownLatch = new CountDownLatch(1);
                    bluetoothGatt.readCharacteristic(characteristic);
                    try {
                        countDownLatch.await(TIME_OUT, TimeUnit.MILLISECONDS);
                        CharacteristicDataDto characteristicDataDto = serviceDataDtoMap.get(serviceUUID).getCharacteristicDataMap().get(characteristicUUID);
                        return ReturnResult.success(characteristicDataDto);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return ReturnResult.fail(e.getMessage());
                    }
                } else {
                    return ReturnResult.fail("未查找到对应的GattCharacteristic！");
                }
            } else {
                return ReturnResult.fail("未查找到对应的GattService！");
            }
        } else {
            return ReturnResult.fail("蓝牙已断开，请重新连接！");
        }
    }

    @SuppressLint("MissingPermission")
    public synchronized ReturnResult<CharacteristicDataDto> setCharacteristicNotification(String serviceUUID, String characteristicUUID, String descriptorUUID, Boolean enable) {
        if (connected) {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
                if (characteristic != null) {
                    try {
                        boolean b = bluetoothGatt.setCharacteristicNotification(characteristic, enable);
                        if (b) {
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(descriptorUUID));
                            if (descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                countDownLatch = new CountDownLatch(1);
                                bluetoothGatt.writeDescriptor(descriptor);
                                countDownLatch.await(TIME_OUT, TimeUnit.MILLISECONDS);
                            }
                            CharacteristicDataDto characteristicDataDto = serviceDataDtoMap.get(serviceUUID).getCharacteristicDataMap().get(characteristicUUID);
                            int properties = characteristic.getProperties();
                            characteristicDataDto.setProperties(properties);
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                characteristicDataDto.setEnableRead(true);
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                characteristicDataDto.setEnableWrite(true);
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                characteristicDataDto.setEnableNotify(true);
                            }
                            if ((properties & BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) > 0) {
                                characteristicDataDto.setEnableWriteNoResp(true);
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                                characteristicDataDto.setEnableIndicate(true);
                            }
                            return ReturnResult.success(characteristicDataDto);
                        } else {
                            return ReturnResult.fail("设置Notify失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ReturnResult.fail(e.getMessage());
                    }
                } else {
                    return ReturnResult.fail("未查找到对应的GattCharacteristic！");
                }
            } else {
                return ReturnResult.fail("未查找到对应的GattService！");
            }
        } else {
            return ReturnResult.fail("蓝牙已断开，请重新连接！");
        }
    }

    /**
     * 启用指令通知
     */
    @SuppressLint("MissingPermission")
    public synchronized ReturnResult<CharacteristicDataDto> enableIndicateNotification(String serviceUUID, String characteristicUUID, Boolean enable) {
        if (connected) {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
                if (characteristic != null) {
                    try {
                        boolean b = bluetoothGatt.setCharacteristicNotification(characteristic, enable);
                        if (b) {
                            CharacteristicDataDto characteristicDataDto = serviceDataDtoMap.get(serviceUUID).getCharacteristicDataMap().get(characteristicUUID);
                            int properties = characteristic.getProperties();
                            characteristicDataDto.setProperties(properties);
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                characteristicDataDto.setEnableRead(true);
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                characteristicDataDto.setEnableWrite(true);
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                characteristicDataDto.setEnableNotify(true);
                            }
                            if ((properties & BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) > 0) {
                                characteristicDataDto.setEnableWriteNoResp(true);
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                                characteristicDataDto.setEnableIndicate(true);
                            }
                            return ReturnResult.success(characteristicDataDto);
                        } else {
                            return ReturnResult.fail("设置Notify失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ReturnResult.fail(e.getMessage());
                    }
                } else {
                    return ReturnResult.fail("未查找到对应的GattCharacteristic！");
                }
            } else {
                return ReturnResult.fail("未查找到对应的GattService！");
            }
        } else {
            return ReturnResult.fail("蓝牙已断开，请重新连接！");
        }
    }

    @SuppressLint("MissingPermission")
    public synchronized ReturnResult<DescriptorDataDto> readDescriptor(String serviceUUID, String characteristicUUID, String descriptorUUID) {
        if (connected) {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
                if (characteristic != null) {
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(descriptorUUID));
                    if (descriptor != null) {
                        bluetoothGatt.readDescriptor(descriptor);
                        countDownLatch = new CountDownLatch(1);
                        try {
                            countDownLatch.await(TIME_OUT, TimeUnit.MILLISECONDS);
                            DescriptorDataDto descriptorDataDto = serviceDataDtoMap.get(serviceUUID).getCharacteristicDataMap().get(characteristicUUID).getDescriptorDataMap().get(descriptorUUID);
                            return ReturnResult.success(descriptorDataDto);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return ReturnResult.fail(e.getMessage());
                        }
                    } else {
                        return ReturnResult.fail("未查找到对应的Descriptor！");
                    }
                } else {
                    return ReturnResult.fail("未查找到对应的GattCharacteristic！");
                }
            } else {
                return ReturnResult.fail("未查找到对应的GattService！");
            }
        } else {
            return ReturnResult.fail("蓝牙已断开，请重新连接！");
        }
    }


    @SuppressLint("MissingPermission")
    public synchronized ReturnResult<DescriptorDataDto> writeDescriptor(String serviceUUID, String characteristicUUID, String descriptorUUID, byte[] data) {
        if (connected) {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
                if (characteristic != null) {
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(descriptorUUID));
                    if (descriptor != null) {
                        descriptor.setValue(data);
                        bluetoothGatt.writeDescriptor(descriptor);
                        countDownLatch = new CountDownLatch(1);
                        try {
                            countDownLatch.await(TIME_OUT, TimeUnit.MILLISECONDS);
                            DescriptorDataDto descriptorDataDto = serviceDataDtoMap.get(serviceUUID).getCharacteristicDataMap().get(characteristicUUID).getDescriptorDataMap().get(descriptorUUID);
                            return ReturnResult.success(descriptorDataDto);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return ReturnResult.fail(e.getMessage());
                        }
                    } else {
                        return ReturnResult.fail("未查找到对应的Descriptor！");
                    }
                } else {
                    return ReturnResult.fail("未查找到对应的GattCharacteristic！");
                }
            } else {
                return ReturnResult.fail("未查找到对应的GattService！");
            }
        } else {
            return ReturnResult.fail("蓝牙已断开，请重新连接！");
        }
    }


    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        /**
         * 物理层改变回调 触发这个方法需要使用gatt.setPreferredPhy()方法设置接收和发送的速率，然后蓝牙设备给我回一个消息，就触发onPhyUpdate（）方法了
         * 设置 2M
         * gatt.setPreferredPhy(BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_OPTION_NO_PREFERRED);
         *
         * @param gatt
         * @param txPhy
         * @param rxPhy
         * @param status
         */
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.e("BluetoothGattCallback", "onPhyUpdate");
        }

        /**
         * 设备物理层读取回调
         *
         * @param gatt
         * @param txPhy
         * @param rxPhy
         * @param status
         */
        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.e("BluetoothGattCallback", "onPhyRead");

        }

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e("BluetoothGattCallback", "onConnectionStateChange:" + newState);
            if (newState == BluetoothGattServer.STATE_CONNECTED) {
                connected = true;
                //此处开启读写Service操作
                //发现服务,执行成功后会执行下面的《onServicesDiscovered》
                gatt.discoverServices();
            } else {//蓝牙断开
//                    EventBusDto<String> eventBusDto = new EventBusDto<>();
//                    eventBusDto.setCode(2);
//                    eventBusDto.setT("蓝牙链接失败:" + newState);
//                    EventBus.getDefault().post(eventBusDto);
                connected = false;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //可以开始进行service读写了
            Log.e("BluetoothGattCallback", "onServicesDiscovered:" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                if (services != null && services.size() > 0) {
                    for (int i = 0; i < services.size(); i++) {
                        BluetoothGattService bluetoothGattService = services.get(i);
                        BleServiceDataDto bleServiceDataDto = serviceDataDtoMap.get(bluetoothGattService.getUuid().toString());
                        if (bleServiceDataDto == null) {
                            bleServiceDataDto = new BleServiceDataDto(bluetoothGattService.getUuid().toString());
                            serviceDataDtoMap.put(bluetoothGattService.getUuid().toString(), bleServiceDataDto);
                        }
                        bleServiceDataDto.setServiceType(bluetoothGattService.getType() == SERVICE_TYPE_PRIMARY ? "PRIMARY SERVICE" : "SECONDARY SERVICE");
                        List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                        if (characteristics != null && characteristics.size() > 0) {
                            for (int j = 0; j < characteristics.size(); j++) {
                                BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristics.get(j);
                                Map<String, CharacteristicDataDto> characteristicDataMap = bleServiceDataDto.getCharacteristicDataMap();
                                if (characteristicDataMap == null) {
                                    characteristicDataMap = new ConcurrentHashMap<>();
                                    bleServiceDataDto.setCharacteristicDataMap(characteristicDataMap);
                                }
                                CharacteristicDataDto characteristicDataDto = characteristicDataMap.get(bluetoothGattCharacteristic.getUuid().toString());
                                if (characteristicDataDto == null) {
                                    characteristicDataDto = new CharacteristicDataDto(bluetoothGattCharacteristic.getUuid().toString(), bluetoothGattService.getUuid().toString());
                                    characteristicDataMap.put(bluetoothGattCharacteristic.getUuid().toString(), characteristicDataDto);
                                }

                                int properties = bluetoothGattCharacteristic.getProperties();
                                characteristicDataDto.setProperties(properties);
                                if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                    characteristicDataDto.setEnableRead(true);
                                }
                                if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                    characteristicDataDto.setEnableWrite(true);
                                }
                                if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                    characteristicDataDto.setEnableNotify(true);
                                }
                                if ((properties & BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) > 0) {
                                    characteristicDataDto.setEnableWriteNoResp(true);
                                }
                                if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                                    characteristicDataDto.setEnableIndicate(true);
                                }
                                List<BluetoothGattDescriptor> descriptors = bluetoothGattCharacteristic.getDescriptors();
                                if (descriptors != null && descriptors.size() > 0) {
                                    for (int f = 0; f < descriptors.size(); f++) {
                                        BluetoothGattDescriptor bluetoothGattDescriptor = descriptors.get(f);
                                        Map<String, DescriptorDataDto> descriptorDataMap = characteristicDataDto.getDescriptorDataMap();
                                        if (descriptorDataMap == null) {
                                            descriptorDataMap = new ConcurrentHashMap<>();
                                            characteristicDataDto.setDescriptorDataMap(descriptorDataMap);
                                        }

                                        DescriptorDataDto descriptorDataDto = descriptorDataMap.get(bluetoothGattDescriptor.getUuid().toString());
                                        if (descriptorDataDto == null) {
                                            descriptorDataDto = new DescriptorDataDto(bluetoothGattDescriptor.getUuid().toString(), bluetoothGattCharacteristic.getUuid().toString(), bluetoothGattService.getUuid().toString());
                                            descriptorDataMap.put(descriptorDataDto.getDescriptorUUID(), descriptorDataDto);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                bluetoothGatt = gatt;
            }
            if (countDownLatch != null) countDownLatch.countDown();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (characteristic.getValue() == null || characteristic.getValue().length <= 0) {
                Log.e("BluetoothGattCallback", "onCharacteristicRead：value is null" );
                return;
            }
            Log.e("BluetoothGattCallback", "onCharacteristicRead:" + ByteUtil.bytes2HexString(characteristic.getValue()));
            String characteristicUUID = characteristic.getUuid().toString();
            String serviceUUID = characteristic.getService().getUuid().toString();
            CharacteristicDataDto characteristicDataDto = serviceDataDtoMap.get(serviceUUID).getCharacteristicDataMap().get(characteristicUUID);
            characteristicDataDto.setData(characteristic.getValue());
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
        }

        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);
            Log.e("BluetoothGattCallback", "onCharacteristicRead2：" + ByteUtil.bytes2HexString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e("BluetoothGattCallback", "onCharacteristicWrite：" + ByteUtil.bytes2HexString(characteristic.getValue()));
            if (countDownLatch != null) countDownLatch.countDown();
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String s = new String(characteristic.getValue(), StandardCharsets.US_ASCII);
            Log.e("BluetoothGattCallback", "onCharacteristicChanged:" + s);
            notifyBuff.append(s);
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            if (descriptor.getValue() == null || descriptor.getValue().length <= 0) {
                Log.e("BluetoothGattCallback", "onDescriptorRead：value is null" );
                return;
            }
            Log.e("BluetoothGattCallback", "onDescriptorRead：" + ByteUtil.bytes2HexString(descriptor.getValue()));
            String characteristicUUID = descriptor.getCharacteristic().getUuid().toString();
            String serviceUUID = descriptor.getCharacteristic().getService().getUuid().toString();
            String descriptorUUID = descriptor.getUuid().toString();
            DescriptorDataDto descriptorDataDto = serviceDataDtoMap.get(serviceUUID).getCharacteristicDataMap().get(characteristicUUID).getDescriptorDataMap().get(descriptorUUID);
            descriptorDataDto.setData(descriptor.getValue());
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }

        }

        @Override
        public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
            super.onDescriptorRead(gatt, descriptor, status, value);
            Log.e("BluetoothGattCallback", "onDescriptorRead :" + ByteUtil.bytes2HexString(descriptor.getValue()));

        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e("BluetoothGattCallback", "onDescriptorWrite :" + ByteUtil.bytes2HexString(descriptor.getValue()));
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.e("BluetoothGattCallback", "onReliableWriteCompleted");

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.e("BluetoothGattCallback", "onReadRemoteRssi");

        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.e("BluetoothGattCallback", "onMtuChanged");
            if (countDownLatch != null) countDownLatch.countDown();
        }

        @Override
        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
            super.onServiceChanged(gatt);
            Log.e("BluetoothGattCallback", "onServiceChanged");
        }
    };


    @SuppressLint("MissingPermission")
    public void destroy() {
        try {
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                bluetoothGatt = null;
            }
            countDownLatch = null;
            serviceDataDtoMap = null;
            bluetoothGattCallback = null;
            executor.shutdownNow();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public Map<String, BleServiceDataDto> getServiceDataDtoMap() {
        return serviceDataDtoMap;
    }

    public StringBuffer getNotifyBuff() {
        return notifyBuff;
    }


}
