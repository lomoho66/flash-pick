package com.lomoho.quickpick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/** 桌面确认钉住小组件后的回调：系统真把组件放上桌面才走这里，给用户确切反馈。 */
public class PinCallbackReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.lomoho.quickpick.PIN_OK";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "小组件已添加到桌面", Toast.LENGTH_SHORT).show();
    }
}
