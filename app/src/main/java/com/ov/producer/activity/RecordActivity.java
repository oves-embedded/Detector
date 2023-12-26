package com.ov.producer.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.ov.producer.MainActivity;
import com.ov.producer.R;
import com.ov.producer.application.MyApplication;
import com.ov.producer.entity.BleCheckRecord;
import com.ov.producer.entity.OvAttrDto;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecordActivity extends AppCompatActivity {

    @BindView(R.id.titleBar)
    TitleBar titleBar;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private RecordAdapter recordAdapter;

    private List<BleCheckRecord> list=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshAdapter();
            }
        });

        refreshLayout.setEnableLoadMore(false);
        titleBar.setOnTitleBarListener(new OnTitleBarListener() {
            @Override
            public void onLeftClick(TitleBar titleBar) {
                OnTitleBarListener.super.onLeftClick(titleBar);
                RecordActivity.this.finish();
            }

            @Override
            public void onTitleClick(TitleBar titleBar) {
                OnTitleBarListener.super.onTitleClick(titleBar);
            }

            @Override
            public void onRightClick(TitleBar titleBar) {
                OnTitleBarListener.super.onRightClick(titleBar);
                MyApplication.sDaoSession.getBleCheckRecordDao().deleteAll();
                refreshAdapter();
            }
        });
        recordAdapter = new RecordAdapter(R.layout.item_ble_record);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recordAdapter);
        refreshAdapter();
    }


    private void refreshAdapter(){
        if(refreshLayout.isRefreshing())refreshLayout.finishRefresh();
        List<BleCheckRecord> bleCheckRecords = MyApplication.sDaoSession.getBleCheckRecordDao().loadAll();
        list=bleCheckRecords;
        recordAdapter.setNewInstance(list);
    }


    class RecordAdapter extends BaseQuickAdapter<BleCheckRecord, BaseViewHolder> {

        public RecordAdapter(int layoutResId) {
            super(layoutResId);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, BleCheckRecord dto) {
            String productName = dto.getProductName() == null ? "" : dto.getProductName().toLowerCase();
            int drawableId = RecordActivity.this.getResources().getIdentifier("icon_" + productName, "mipmap", RecordActivity.this.getPackageName());
            baseViewHolder.setImageResource(R.id.iv_devIcon, drawableId);
            baseViewHolder.setText(R.id.tv_mac,dto.getMac());
            baseViewHolder.setText(R.id.tv_opid,dto.getOpid()==null?"<空>":dto.getOpid());
            if (dto.getFlag()!=null&&dto.getFlag()) {
                baseViewHolder.setBackgroundResource(R.id.ll_item, R.drawable.shape_circle_check_ok);
            } else {
                baseViewHolder.setBackgroundResource(R.id.ll_item, R.drawable.shape_circle_check_err);
            }

        }

    }



}
