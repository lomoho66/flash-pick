package com.lomoho.quickpick;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/** 小组件共用：生成跳板 PendingIntent，按组件实例 + 入口区分，避免互相覆盖。 */
public final class WidgetJump {

    private WidgetJump() {
    }

    public static PendingIntent pending(Context context, int widgetId, String entryId) {
        Intent intent = new Intent(context, JumpActivity.class);
        intent.putExtra(JumpActivity.EXTRA_TARGET, entryId);
        intent.setAction("com.lomoho.quickpick.JUMP." + entryId + "." + widgetId);
        int requestCode = widgetId * 100 + Math.abs(entryId.hashCode() % 90);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }
}
