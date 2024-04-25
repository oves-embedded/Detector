package com.ov.producer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.ov.producer.MainActivity;
import com.ov.producer.R;
import com.ov.producer.application.MyApplication;
import com.ov.producer.constants.ReturnResult;
import com.ov.producer.entity.BleCheckRecord;
import com.ov.producer.entity.BleServiceDataDto;
import com.ov.producer.entity.CharacteristicDataDto;
import com.ov.producer.entity.DataConvert;
import com.ov.producer.entity.DescriptorDataDto;
import com.ov.producer.entity.OvAttrDto;
import com.ov.producer.enums.ServiceNameEnum;
import com.ov.producer.service.BleService;
import com.ov.producer.utils.BleDeviceUtil;
import com.ov.producer.utils.LogUtil;
import com.ov.producer.utils.permission.PermissionInterceptor;
import com.ov.producer.utils.permission.PermissionNameConvert;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckActivity extends AppCompatActivity {


    @BindView(R.id.titleBar)
    TitleBar titleBar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.confirm_button)
    Button confirm_button;
    @BindView(R.id.fail_button)
    Button fail_button;
    private AttrAdapter attrAdapter;
    private BleService bleService;
    private BleDeviceUtil bleDeviceUtil;
    private String opid;
    private List<OvAttrDto> list;

    BleCheckRecord checkRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        ButterKnife.bind(this);

        String data = getIntent().getStringExtra("data");
        checkRecord = new Gson().fromJson(data, BleCheckRecord.class);
//        MyApplication.sDaoSession.getBleCheckRecordDao().deleteAll();

        titleBar.setOnTitleBarListener(new OnTitleBarListener() {
            @Override
            public void onLeftClick(TitleBar titleBar) {
                OnTitleBarListener.super.onLeftClick(titleBar);
                CheckActivity.this.finish();
            }
        });

        attrAdapter = new AttrAdapter(R.layout.item_ble_attr);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(attrAdapter);
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bleService = ((BleService.BleServiceBinder) service).getService();
                Toaster.show("正在连接蓝牙，请稍等。。。");

                startCheck(checkRecord.getMac());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, BIND_AUTO_CREATE);

        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XXPermissions.with(CheckActivity.this).permission(
                                Permission.READ_EXTERNAL_STORAGE)
                        .permission(Permission.WRITE_EXTERNAL_STORAGE)
                        .permission(Permission.READ_PHONE_STATE)
                        .interceptor(new PermissionInterceptor()).request(new OnPermissionCallback() {
                            @Override
                            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                if (!allGranted) {
                                    return;
                                }
                                checkRecord.setTestTime(new Date());
                                checkRecord.setScanStr(new Gson().toJson(list));
                                checkRecord.setOpid(opid);
                                checkRecord.setUploaded(false);
                                checkRecord.setFlag(true);
                                long insert = MyApplication.sDaoSession.getBleCheckRecordDao().insertOrReplace(checkRecord);
                                Toaster.show(String.format(getString(R.string.demo_obtain_permission_success_hint), PermissionNameConvert.getPermissionString(CheckActivity.this, permissions)));
                                CheckActivity.this.finish();
                            }
                        });
            }
        });
        fail_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XXPermissions.with(CheckActivity.this).permission(
                                Permission.READ_EXTERNAL_STORAGE)
                        .permission(Permission.WRITE_EXTERNAL_STORAGE)
                        .interceptor(new PermissionInterceptor()).request(new OnPermissionCallback() {
                            @Override
                            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                if (!allGranted) {
                                    Toaster.show("权限获取失败");
                                    return;
                                }
                                checkRecord.setTestTime(new Date());
                                checkRecord.setScanStr(new Gson().toJson(list));
                                checkRecord.setOpid(opid);
                                checkRecord.setFlag(false);
                                checkRecord.setUploaded(false);
                                long insert = MyApplication.sDaoSession.getBleCheckRecordDao().insertOrReplace(checkRecord);
                                Toaster.show(String.format(getString(R.string.demo_obtain_permission_success_hint), PermissionNameConvert.getPermissionString(CheckActivity.this, permissions)));
                                CheckActivity.this.finish();
                            }
                        });
            }
        });

    }


    private void startCheck(String mac) {
        new Thread() {
            @Override
            public void run() {
                try {
                    bleDeviceUtil = bleService.connectBle(mac);
                    if (bleDeviceUtil != null) {
                        Toaster.show("蓝牙连接成功，正在读取设备信息！");
                        checkData();
                        Toaster.show("数据读取完成，请检查是否有误！");
                    } else {
                        Toaster.show("蓝牙连接失败，请检查设备是否正常或已被其他设备连接！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void checkData() {
        if (bleDeviceUtil != null) {
            Map<String, BleServiceDataDto> serviceDataDtoMap = bleDeviceUtil.getServiceDataDtoMap();
            Set<String> keySet = serviceDataDtoMap.keySet();
            for (String serviceUUID : keySet) {
                BleServiceDataDto bleServiceDataDto = serviceDataDtoMap.get(serviceUUID);
                if (serviceUUID.startsWith(ServiceNameEnum.ATT_SERVICE_NAME.getPrefixCode())) {
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
                                                    OvAttrDto ovAttrDto = new OvAttrDto();
                                                    ovAttrDto.setName(characteristicDataDto.getName());
                                                    ovAttrDto.setValue(characteristicDataDto.getRealVal());
                                                    ovAttrDto.setDesc(characteristicDataDto.getDesc());
                                                    ovAttrDto.setValType(characteristicDataDto.getValType());
                                                    ovAttrDto.setServiceUUID(characteristicDataDto.getParentServiceUUID());
                                                    ovAttrDto.setCharacteristicUUID(characteristicDataDto.getCharacteristicUUID());
                                                    if (list == null)
                                                        list = new CopyOnWriteArrayList<>();
                                                    list.add(ovAttrDto);
                                                    LogUtil.info(serviceUUID + ">" + characteristicDataDto.getCharacteristicUUID() + ":" + new Gson().toJson(ovAttrDto));

                                                    CheckActivity.this.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (ovAttrDto.getName().equals("opid")) {
                                                                titleBar.setTitle(ovAttrDto.getValue().toString());
                                                                opid = ovAttrDto.getValue().toString();
                                                                if (bleService != null) {
                                                                    bleService.publishMsg2ProductMqtt(opid, checkRecord.getProductName());
                                                                }
                                                            }
                                                            attrAdapter.setNewInstance(list);
                                                            recyclerView.scrollToPosition(list.size() - 1);
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    class AttrAdapter extends BaseQuickAdapter<OvAttrDto, BaseViewHolder> {

        public AttrAdapter(int layoutResId) {
            super(layoutResId);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, OvAttrDto dto) {
            baseViewHolder.setText(R.id.tv_name, dto.getName() + " :" + (dto.getValue() == null ? "<空>" : dto.getValue().toString()));
            baseViewHolder.setText(R.id.tv_desc, dto.getDesc());
            if (dto.getValue() == null) {
                baseViewHolder.setBackgroundResource(R.id.ll_item, R.drawable.shape_circle_check_err);
            } else {
                baseViewHolder.setBackgroundResource(R.id.ll_item, R.drawable.shape_circle_check_ok);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleDeviceUtil != null) {
            bleDeviceUtil.destroy();
        }
    }
}
