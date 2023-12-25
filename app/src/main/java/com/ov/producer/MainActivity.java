package com.ov.producer;

import static com.ov.producer.enums.EventBusTagEnum.NOT_ENABLE_LE;
import static com.ov.producer.enums.EventBusTagEnum.NOT_SUPPORT_LE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.ov.producer.activity.CheckActivity;
import com.ov.producer.constants.ReturnResult;
import com.ov.producer.entity.BleCheckRecord;
import com.ov.producer.entity.BleServiceDataDto;
import com.ov.producer.entity.CharacteristicDataDto;
import com.ov.producer.entity.DataConvert;
import com.ov.producer.entity.DescriptorDataDto;
import com.ov.producer.entity.EventBusMsg;
import com.ov.producer.entity.OvAttrDto;
import com.ov.producer.enums.EventBusTagEnum;
import com.ov.producer.enums.ServiceNameEnum;
import com.ov.producer.service.BleService;
import com.ov.producer.utils.BleDeviceUtil;
import com.ov.producer.utils.LogUtil;
import com.ov.producer.utils.permission.PermissionInterceptor;
import com.ov.producer.utils.permission.PermissionNameConvert;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private BleService bleService;

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.titleBar)
    TitleBar titleBar;

    private BleAdapter bleAdapter;

    private int REQUEST_CODE_SCAN_ONE=999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initService();
        initView();
        bleAdapter = new BleAdapter(R.layout.item_ble_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(bleAdapter);
    }

    public void initView() {
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                Log.e("setOnRefreshListener", "onRefresh");
                if (bleService != null) {
                    bleService.stopScan();
                    bleService.startBleScan();
                }

            }
        });
//        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
//            @Override
//            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
//                refreshLayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
//                Log.e("setOnLoadMoreListener", "finishLoadMore");
//            }
//        });

        titleBar.setOnTitleBarListener(new OnTitleBarListener() {
            @Override
            public void onLeftClick(TitleBar titleBar) {
                OnTitleBarListener.super.onLeftClick(titleBar);
                XXPermissions.with(MainActivity.this)
                        .permission(Permission.CAMERA)
                        .permission(Permission.READ_EXTERNAL_STORAGE)
                        .interceptor(new PermissionInterceptor()).request(new OnPermissionCallback() {
                            @Override
                            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                if(!allGranted){
                                    return;
                                }
                                ScanUtil.startScan(MainActivity.this, REQUEST_CODE_SCAN_ONE, new HmsScanAnalyzerOptions.Creator().create());
                            }
                        });
            }

            @Override
            public void onTitleClick(TitleBar titleBar) {
                OnTitleBarListener.super.onTitleClick(titleBar);
            }

            @Override
            public void onRightClick(TitleBar titleBar) {
                OnTitleBarListener.super.onRightClick(titleBar);
            }
        });
    }

    private void startScan() {
        XXPermissions.with(MainActivity.this)
                .permission(Permission.BLUETOOTH_SCAN)
                .permission(Permission.BLUETOOTH_CONNECT)
                .permission(Permission.BLUETOOTH_ADVERTISE)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .permission(Permission.ACCESS_BACKGROUND_LOCATION)
                .interceptor(new PermissionInterceptor()).request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            return;
                        }
                        if (bleService != null) {
                            bleService.startBleScan();
                        }
                        Toaster.show(String.format(getString(R.string.demo_obtain_permission_success_hint), PermissionNameConvert.getPermissionString(MainActivity.this, permissions)));
                    }
                });
    }

    private void initService() {
        Intent intent = new Intent(MainActivity.this, BleService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bleService = ((BleService.BleServiceBinder) service).getService();
                startScan();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void subscriber(EventBusMsg msg) {
        LogUtil.error(JSON.toJSONString(msg));
        if (msg.getTagEnum() == EventBusTagEnum.BLE_FIND) {
            if (refreshLayout.isRefreshing()) {
                refreshLayout.finishRefresh();
            }
            List<BleCheckRecord> list = (List<BleCheckRecord>) msg.getT();
            bleAdapter.setNewInstance(list);
        }

        if(msg.getTagEnum() == EventBusTagEnum.NOT_SUPPORT_LE){
            Toaster.show("该设备不支持低功耗蓝牙");
        }
        if(msg.getTagEnum() == EventBusTagEnum.NOT_ENABLE_LE){
            Toaster.show("请打开蓝牙开关");
        }
        if(msg.getTagEnum() == EventBusTagEnum.BLE_INIT_ERROR){
            Toaster.show("蓝牙程序初始化失败");
        }

    }

    class BleAdapter extends BaseQuickAdapter<BleCheckRecord, BaseViewHolder> {

        public BleAdapter(int layoutResId) {
            super(layoutResId);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, BleCheckRecord info) {
            String fullName = info.getBleName();
            if (!TextUtils.isEmpty(fullName)) {
                baseViewHolder.setText(R.id.tv_bleName, fullName);
            }
            String productName = info.getProductName() == null ? "" : info.getProductName().toLowerCase();
            int drawableId = MainActivity.this.getResources().getIdentifier("icon_" + productName, "mipmap", MainActivity.this.getPackageName());
            baseViewHolder.setImageResource(R.id.iv_devIcon, drawableId);
            String address = info.getMac();
            if (!TextUtils.isEmpty(address)) {
                baseViewHolder.setText(R.id.tv_mac, address);
            }

            baseViewHolder.getView(R.id.ll_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bleService != null) {
                        Intent intent = new Intent(MainActivity.this, CheckActivity.class);
                        intent.putExtra("data", JSON.toJSONString(info));
                        startActivity(intent);
                    }
                }
            });
        }
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
                        }
                    }
                }
            }
        }
        return dtoMap;
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    //Activity回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE_SCAN_ONE) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                Toast.makeText(this,obj.originalValue,Toast.LENGTH_SHORT).show();
                if(obj.originalValue!=null&&obj.originalValue.length()>6){
                    String substring = obj.originalValue.substring(obj.originalValue.length() - 6);
                    Toast.makeText(MainActivity.this, substring, Toast.LENGTH_SHORT).show();
                    if(bleService!=null){
                        BleCheckRecord suffKeywordBle = bleService.findSuffKeywordBle(substring);
                        if(suffKeywordBle==null){
                            Toaster.show("未扫描到该设备");
                            return;
                        }
                        Intent intent = new Intent(MainActivity.this, CheckActivity.class);
                        intent.putExtra("data", JSON.toJSONString(suffKeywordBle));
                        startActivity(intent);
                    }
                }else{
                    Toaster.show("对不起，条码错误！");
                }
            }
        }
    }

}