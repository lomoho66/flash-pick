package com.lomoho.quickpick;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

/** 1×1 固定入口小组件基类：子类只给入口 ID，内容走 WidgetViews.single。 */
public abstract class BaseSingleWidget extends AppWidgetProvider {

    abstract String entryId();

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            manager.updateAppWidget(id, WidgetViews.single(context, id, entryId()));
        }
    }
}
