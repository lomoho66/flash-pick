package com.lomoho.quickpick;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

/** 2×2 双入口小组件：左右入口在「更多」里自选（large1 / large2）。 */
public class WidgetDuo extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            manager.updateAppWidget(id, WidgetViews.duo(context, id));
        }
    }
}
