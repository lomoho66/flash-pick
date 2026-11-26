package com.lomoho.quickpick;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

/**
 * 小组件内容构建：
 * 四个 1×1 各固定一个入口（多多取件 / 多多身份码 / 菜鸟取件 / 菜鸟身份码），
 * 一个 2×2 双入口（左右绑 large1 / large2，在「更多」里自选）。
 * 图标与主页按钮同款品牌图、一一对应，卡底与图标深浅随 App 外观设置。
 */
public final class WidgetViews {

    private static final int INK = 0xFF1C1C1E;
    private static final int PAPER = 0xFFFFFFFF;

    private WidgetViews() {
    }

    /** 入口 → 主页同款品牌图（浅/深各一版，与 MainActivity 菜单图同一套）。 */
    static int menuIcon(String entryId, boolean night) {
        switch (entryId) {
            case "pdd_code":
                return night ? R.drawable.menu_pdd_idcode_dark : R.drawable.menu_pdd_idcode;
            case "cainiao_packages":
                return night ? R.drawable.menu_cainiao_pickup_dark : R.drawable.menu_cainiao_pickup;
            case "cainiao_code":
                return night ? R.drawable.menu_cainiao_idcode_dark : R.drawable.menu_cainiao_idcode;
            default:
                return night ? R.drawable.menu_pdd_pickup_dark : R.drawable.menu_pdd_pickup;
        }
    }

    private static int cardBg(boolean night) {
        return night ? R.drawable.widget_card_dark : R.drawable.btn_id_plain;
    }

    /** 每个 1×1 用自己的布局（默认值即该入口的图与名，桌面选择器预览才准）。 */
    private static int layoutFor(String entryId) {
        switch (entryId) {
            case "pdd_code":
                return R.layout.widget_single_pdd_code;
            case "cainiao_packages":
                return R.layout.widget_single_cn_pick;
            case "cainiao_code":
                return R.layout.widget_single_cn_code;
            default:
                return R.layout.widget_single_pdd_pick;
        }
    }

    /** 1×1 固定入口组件。 */
    public static RemoteViews single(Context ctx, int widgetId, String entryId) {
        Platforms.Entry entry = Platforms.find(entryId);
        boolean night = Ui.night(ctx);
        RemoteViews views = new RemoteViews(ctx.getPackageName(), layoutFor(entry.id));
        // 伪装成 app 图标：圆角方块底随外观换色；名称由组件内文字充当（仿桌面标签：白字带阴影）
        views.setInt(R.id.widget_single_card, "setBackgroundResource",
                night ? R.drawable.widget_tile_dark : R.drawable.widget_tile_light);
        views.setImageViewResource(R.id.widget_icon, menuIcon(entry.id, night));
        views.setTextViewText(R.id.widget_label, entry.shortLabel);
        views.setOnClickPendingIntent(R.id.widget_small_root,
                WidgetJump.pending(ctx, widgetId, entry.id));
        return views;
    }

    /** 2×1 双入口组件：左右各一个入口，槽位 large1 / large2。 */
    public static RemoteViews duo(Context ctx, int widgetId) {
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_duo);
        fillHalf(ctx, views, widgetId,
                Platforms.find(Prefs.slotEntry(ctx, "large1")),
                R.id.widget_duo_left, R.id.widget_duo_icon_l, R.id.widget_duo_label_l);
        fillHalf(ctx, views, widgetId,
                Platforms.find(Prefs.slotEntry(ctx, "large2")),
                R.id.widget_duo_right, R.id.widget_duo_icon_r, R.id.widget_duo_label_r);
        return views;
    }

    /** 2×2 组件里的文字只留功能（图标已能区分平台）：不带多多/菜鸟字样。 */
    static String duoLabel(Platforms.Entry entry) {
        // 与主页按钮同名：两个取件入口都叫「取快递」，身份码都叫「身份码」
        return entry.primary ? "取快递" : "身份码";
    }

    private static void fillHalf(Context ctx, RemoteViews views, int widgetId, Platforms.Entry entry,
                                 int rootId, int iconId, int labelId) {
        boolean night = Ui.night(ctx);
        views.setInt(rootId, "setBackgroundResource", cardBg(night));
        views.setImageViewResource(iconId, menuIcon(entry.id, night));
        views.setTextViewText(labelId, duoLabel(entry));
        views.setTextColor(labelId, night ? PAPER : INK);
        views.setOnClickPendingIntent(rootId, WidgetJump.pending(ctx, widgetId, entry.id));
    }

    /** 设置变更后立即刷新全部已添加的组件实例。 */
    public static void refreshAll(Context ctx) {
        AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
        refreshSingles(ctx, manager, WidgetPddPick.class, "pdd_packages");
        refreshSingles(ctx, manager, WidgetPddCode.class, "pdd_code");
        refreshSingles(ctx, manager, WidgetCnPick.class, "cainiao_packages");
        refreshSingles(ctx, manager, WidgetCnCode.class, "cainiao_code");
        int[] duo = manager.getAppWidgetIds(new ComponentName(ctx, WidgetDuo.class));
        for (int id : duo) {
            manager.updateAppWidget(id, duo(ctx, id));
        }
    }

    private static void refreshSingles(Context ctx, AppWidgetManager manager,
                                       Class<?> provider, String entryId) {
        int[] ids = manager.getAppWidgetIds(new ComponentName(ctx, provider));
        for (int id : ids) {
            manager.updateAppWidget(id, single(ctx, id, entryId));
        }
    }
}
