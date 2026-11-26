package com.lomoho.quickpick;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

/** 轻量设置存储：外观模式、双入口组件槽位；桌面图标随外观自动切换。零权限，存应用私有目录。 */
public final class Prefs {

    private static final String FILE = "ui_prefs";

    private Prefs() {
    }

    private static SharedPreferences sp(Context ctx) {
        return ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** 外观：auto / light / dark */
    public static String themeMode(Context ctx) {
        return sp(ctx).getString("theme_mode", "auto");
    }

    public static void setThemeMode(Context ctx, String v) {
        sp(ctx).edit().putString("theme_mode", v).apply();
    }

    /** 2×1 双入口组件左右槽位绑定的入口 ID */
    public static String slotEntry(Context ctx, String slot) {
        String def = "large2".equals(slot) ? "cainiao_code" : "pdd_packages";
        return sp(ctx).getString("slot_" + slot, def);
    }

    public static void setSlotEntry(Context ctx, String slot, String entryId) {
        sp(ctx).edit().putString("slot_" + slot, entryId).apply();
    }

    /**
     * 桌面图标随外观自动切换：深色 → 夜幕、浅色 → 奶油。
     * 用 icon_applied 记上次已应用值，没变化直接返回，避免每次回到主页都惊动桌面。
     */
    public static void applyIconForTheme(Context ctx) {
        String want = Ui.night(ctx) ? "midnight" : "classic";
        if (want.equals(sp(ctx).getString("icon_applied", ""))) {
            return;
        }
        sp(ctx).edit().putString("icon_applied", want).apply();
        PackageManager pm = ctx.getPackageManager();
        String pkg = ctx.getPackageName();
        ComponentName classic = new ComponentName(pkg, pkg + ".IconClassic");
        ComponentName midnight = new ComponentName(pkg, pkg + ".IconMidnight");
        boolean useMidnight = "midnight".equals(want);
        // 先启用新别名再停用旧的，避免瞬间无桌面图标；个别 ROM 抛异常也不能连累 App 本身
        try {
            pm.setComponentEnabledSetting(useMidnight ? midnight : classic,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(useMidnight ? classic : midnight,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception ignored) {
            // 图标没跟上而已，下次退后台再对齐
        }
    }
}
