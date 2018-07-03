package com.destination.desapp.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.destination.desapp.App;
import com.destination.desapp.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by shanqixuan on 2017/11/14.
 * Toast工具类
 */

public class MyToast {
    public static final boolean LENGTH_LONG = true;
    public static final boolean LENGTH_SHORT = false;
    private WindowManager mWdm;
    private View mToastView;
    private WindowManager.LayoutParams mParams;
    //记录Toast的显示长短类型
    private boolean mShowTime;
    //记录当前Toast的内容是否已经在显示
    private boolean mIsShow;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private MyToast(Context context, View view, boolean showTime) {
        //记录Toast的显示长短类型
        mShowTime = showTime;
        //记录当前Toast的内容是否已经在显示
        mIsShow = false;
        mWdm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mToastView = view;
        //设置布局参数
        setParams();
    }

    private void setParams() {
        mParams = new WindowManager.LayoutParams();
        mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.format = PixelFormat.TRANSLUCENT;
        //设置进入退出动画效果
        mParams.windowAnimations = R.style.anim_view;
        //该Type描述的是形成的窗口的层级关系，下面会详细列出它的属性
        if(Build.VERSION.SDK_INT>=25){
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }else{
            mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        mParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //当此窗口为用户可见时，保持设备常开，并保持亮度不变。
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置后可透过toast进行点击操作
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        mParams.gravity = Gravity.CENTER;

        //设置Toast全屏显示（包括顶部状态栏）
        if (mToastView != null)
            mToastView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Deprecated
    public static MyToast MakeText(Activity activity, boolean showTime, String message) {
        return MakeText(showTime, message);
    }

    public static MyToast MakeText(boolean showTime, String message) {
        Context context = App.Companion.instance();
        //加载View视图，这个就是我们要显示的内容
        View toastView = LayoutInflater.from(context).inflate(R.layout.mytoast_layout, null);
        TextView msgView = (TextView) toastView.findViewById(R.id.toast_message);
        msgView.setText(message);
        MyToast result = new MyToast(context, toastView, showTime);

        return result;
    }

    public static MyToast sendVoiceTooShort(boolean showTime) {
        Context context = App.Companion.instance();
        View toastView = LayoutInflater.from(context).inflate(R.layout.voice_too_short_toast_layout, null);
        MyToast result = new MyToast(context, toastView, showTime);

        return result;
    }

    public void show() {
        //如果Toast没有显示，则开始加载显示
        if (!mIsShow) {
            mIsShow = true;
            //FIXME debug 将其加载到windowManager上
            mWdm.addView(mToastView, mParams);
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    if (mWdm != null && mToastView != null) {
                        mWdm.removeView(mToastView);
                    }
                    mIsShow = false;
                }
            }, (long) (mShowTime ? 3000 : 2000), TimeUnit.MILLISECONDS);//毫秒
        }
    }

    public void cancel() {
        if (executorService == null) {
            mWdm.removeView(mToastView);
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        mIsShow = false;
    }
}
