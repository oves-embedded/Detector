package com.ov.producer.application;

import android.app.Application;
import android.content.Context;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.hjq.bar.TitleBar;
import com.hjq.bar.style.LightBarStyle;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.hjq.toast.style.WhiteToastStyle;
import com.ov.producer.R;
import com.ov.producer.dao.DaoMaster;
import com.ov.producer.dao.DaoSession;
import com.ov.producer.utils.permission.PermissionInterceptor;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshFooterCreator;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator;
import com.tencent.bugly.crashreport.CrashReport;

public class MyApplication extends Application {

    //实例化DaoMaster对象
    private DaoMaster mDaoMaster;
    //实例化DaoSession对象
    public static DaoSession sDaoSession;

    public static DaoMaster.DevOpenHelper mDevOpenHelper = null;

    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.purple_200, android.R.color.white);//全局设置主题颜色
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(25);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 1、获取需要连接的数据库
        mDevOpenHelper = new DaoMaster.DevOpenHelper(getApplicationContext(), "ov_producerv2.db");
        // 2、创建数据库连接
        mDaoMaster = new DaoMaster(mDevOpenHelper.getWritableDb());
        // 3、创建数据库会话
        sDaoSession = mDaoMaster.newSession();
        CrashReport.initCrashReport(getApplicationContext(), "ec0e4e18b9", false);
//        BleManager.getInstance().init(this);
//
//        BleManager.getInstance()
//                .enableLog(true)
//                .setReConnectCount(1, 5000)
//                .setSplitWriteNum(500)
//                .setConnectOverTime(20000)
//                .setOperateTimeout(5000);

        // 初始化吐司工具类
        Toaster.init(this, new WhiteToastStyle());

        // 设置不触发错误检测机制（全局设置）
        XXPermissions.setCheckMode(false);
        // 设置权限申请拦截器（全局设置）
        XXPermissions.setInterceptor(new PermissionInterceptor());

        // 初始化 TitleBar 默认样式
        TitleBar.setDefaultStyle(new LightBarStyle() {

            @Override
            public TextView newTitleView(Context context) {
                return new AppCompatTextView(context);
            }

            @Override
            public TextView newLeftView(Context context) {
                return new AppCompatTextView(context);
            }

            @Override
            public TextView newRightView(Context context) {
                return new AppCompatTextView(context);
            }
        });

    }
}
