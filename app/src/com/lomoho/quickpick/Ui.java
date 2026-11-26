package com.lomoho.quickpick;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 苹果风设计系统：iOS 配色（分组浅灰底 / 白卡 / 系统红点缀 / 系统灰文字），
 * 颜色随系统深色模式切换。圆角 20、发丝线分隔、不用重描边。
 */
public final class Ui {

    /** iOS systemRed */
    public static final int RED = Color.rgb(0xFF, 0x3B, 0x30);
    /** 闪电金（深色模式主题色：深色下以闪电金替代主题红） */
    public static final int GOLD = Color.rgb(0xEF, 0xC6, 0x5C);
    /** 兼容旧引用：等于浅色模式的分组底色 */
    public static final int BG = Color.rgb(0xF2, 0xF2, 0xF7);
    public static final int DARK = Color.rgb(0x1C, 0x1C, 0x1E);
    public static final int GREY = Color.rgb(0x8E, 0x8E, 0x93);

    private Ui() {
    }

    public static boolean night(Context ctx) {
        String mode = Prefs.themeMode(ctx);
        if ("dark".equals(mode)) {
            return true;
        }
        if ("light".equals(mode)) {
            return false;
        }
        return (ctx.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    /** 主题强调色：浅色品牌红 #E02E24，深色闪电金 GOLD */
    public static int pdd(Context ctx) {
        return night(ctx) ? GOLD : Color.rgb(0xE0, 0x2E, 0x24);
    }





    public static int bg(Context ctx) {
        return night(ctx) ? Color.rgb(0x00, 0x00, 0x00) : Color.rgb(0xF2, 0xF2, 0xF7);
    }

    public static int card(Context ctx) {
        return night(ctx) ? Color.rgb(0x1C, 0x1C, 0x1E) : Color.WHITE;
    }

    public static int dark(Context ctx) {
        return night(ctx) ? Color.WHITE : Color.rgb(0x1C, 0x1C, 0x1E);
    }

    public static int grey(Context ctx) {
        return Color.rgb(0x8E, 0x8E, 0x93);
    }

    /** 正文次级文字：浅色模式深灰，深色模式浅灰，两种底上都够对比 */
    public static int secondary(Context ctx) {
        return night(ctx) ? Color.rgb(0xAE, 0xAE, 0xB2) : Color.rgb(0x48, 0x48, 0x4A);
    }

    public static int separator(Context ctx) {
        return night(ctx) ? Color.rgb(0x38, 0x38, 0x3A) : Color.rgb(0xE5, 0xE5, 0xEA);
    }

    /** 图标 chip 的浅色底：浅色模式浅红，深色模式暗金 */
    public static int chipTint(Context ctx) {
        return night(ctx) ? Color.rgb(0x3D, 0x31, 0x14) : Color.rgb(0xFF, 0xEC, 0xEB);
    }

    /** 中性胶囊/按钮底（更多按钮、作者徽标） */
    public static int neutralFill(Context ctx) {
        return night(ctx) ? Color.rgb(0x1C, 0x1C, 0x1E) : Color.rgb(0xE9, 0xE9, 0xEE);
    }

    public static int dp(Context ctx, int v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }


    /** 把二级页内容包进可滚动容器：内容超过一屏时可以下滑（page 本身只是纵向布局，不带滚动）。 */
    public static ScrollView scrollWrap(Context ctx, View content) {
        ScrollView sv = new ScrollView(ctx);
        sv.setBackgroundColor(bg(ctx));
        content.setLayoutParams(new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        sv.addView(content);
        return sv;
    }


    /** 状态栏高度（px），沉浸式布局时给内容顶部让位用。 */
    public static int statusBarHeight(Context ctx) {
        int id = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? ctx.getResources().getDimensionPixelSize(id) : 0;
    }

    /**
     * 沉浸式状态栏：状态栏透明、App 背景色延伸到状态栏下面，内容顶部自动让出状态栏高度。
     * 浅色模式用深色状态栏图标，深色模式用浅色图标。
     */
    public static void edgeToEdge(Activity activity) {
        Window w = activity.getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(Color.TRANSPARENT);
        // 底部导航栏跟界面同色（原来没设、系统默认黑条，很突兀）
        w.setNavigationBarColor(bg(activity));
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (!night(activity)) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        w.getDecorView().setSystemUiVisibility(flags);
    }

    /** 二级页骨架：顶部「← + 标题」，下面是内容区 root。 */
    public static LinearLayout page(Activity activity, String title) {
        edgeToEdge(activity);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(activity, 20);
        root.setPadding(pad, dp(activity, 28) + statusBarHeight(activity), pad, pad);
        root.setBackgroundColor(bg(activity));

        LinearLayout head = new LinearLayout(activity);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = new TextView(activity);
        back.setText("‹");
        back.setTextSize(32);
        back.setTextColor(night(activity) ? GOLD : RED);
        back.setPadding(0, 0, dp(activity, 8), dp(activity, 4));
        back.setOnClickListener(v -> activity.finish());
        head.addView(back);

        TextView t = new TextView(activity);
        t.setText(title);
        t.setTextSize(22);
        t.setTextColor(dark(activity));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        head.addView(t);
        root.addView(head);
        return root;
    }

    public static LinearLayout whiteCard(Context ctx) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        int cp = dp(ctx, 18);
        card.setPadding(cp, cp, cp, cp);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(card(ctx));
        bg.setCornerRadius(dp(ctx, 20));
        card.setBackground(bg);
        return card;
    }

    public static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static View divider(Context ctx) {
        View v = new View(ctx);
        v.setBackgroundColor(separator(ctx));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(ctx, 1) / 2));
        lp.topMargin = dp(ctx, 12);
        lp.bottomMargin = dp(ctx, 12);
        v.setLayoutParams(lp);
        return v;
    }

    public static LinearLayout infoRow(Context ctx, String label, String value) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView l = new TextView(ctx);
        l.setText(label);
        l.setTextSize(15);
        l.setTextColor(dark(ctx));
        row.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView r = new TextView(ctx);
        r.setText(value);
        r.setTextSize(15);
        r.setTextColor(grey(ctx));
        r.setGravity(Gravity.END);
        row.addView(r, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
        return row;
    }

    // ---------- 自绘弹窗（替代系统 AlertDialog：默认配色与全 App 风格不统一） ----------

    private static Dialog baseDialog(Activity act, LinearLayout cardBox) {
        Dialog dialog = new Dialog(act);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(card(act));
        cardBg.setCornerRadius(dp(act, 24));
        cardBox.setBackground(cardBg);
        cardBox.setOrientation(LinearLayout.VERTICAL);
        cardBox.setPadding(dp(act, 22), dp(act, 20), dp(act, 22), dp(act, 14));
        dialog.setContentView(cardBox);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            int width = (int) (act.getResources().getDisplayMetrics().widthPixels * 0.86);
            w.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    private static TextView dialogTitle(Activity act, String title) {
        TextView t = new TextView(act);
        t.setText(title);
        t.setTextSize(17);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(dark(act));
        return t;
    }

    private static TextView dialogButton(Activity act, String text, boolean primary) {
        TextView b = new TextView(act);
        b.setText(text);
        b.setTextSize(15);
        if (primary) {
            b.setTypeface(Typeface.DEFAULT_BOLD);
            b.setTextColor(pdd(act));
        } else {
            b.setTextColor(secondary(act));
        }
        b.setPadding(dp(act, 12), dp(act, 10), dp(act, 4), dp(act, 6));
        return b;
    }

    /** 确认弹窗：标题 + 正文 + 「取消 / 主按钮」。 */
    public static void confirm(Activity act, String title, String message,
                               String positiveText, Runnable onPositive) {
        LinearLayout box = new LinearLayout(act);
        Dialog dialog = baseDialog(act, box);
        box.addView(dialogTitle(act, title));
        TextView msg = new TextView(act);
        msg.setText(message);
        msg.setTextSize(15);
        msg.setTextColor(secondary(act));
        msg.setLineSpacing(0, 1.3f);
        LinearLayout.LayoutParams mLp = matchWrap();
        mLp.topMargin = dp(act, 10);
        box.addView(msg, mLp);
        LinearLayout btns = new LinearLayout(act);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        btns.setGravity(Gravity.END);
        TextView cancel = dialogButton(act, "取消", false);
        cancel.setOnClickListener(v -> dialog.dismiss());
        btns.addView(cancel);
        TextView ok = dialogButton(act, positiveText, true);
        ok.setOnClickListener(v -> {
            dialog.dismiss();
            onPositive.run();
        });
        btns.addView(ok);
        LinearLayout.LayoutParams bLp = matchWrap();
        bLp.topMargin = dp(act, 8);
        box.addView(btns, bLp);
        dialog.show();
    }

    /** 单选弹窗：自绘单选圈（品牌色），点中即生效关闭。 */
    public static void singleChoice(Activity act, String title, String[] names, int checked,
                                    java.util.function.IntConsumer onPick) {
        LinearLayout box = new LinearLayout(act);
        Dialog dialog = baseDialog(act, box);
        box.addView(dialogTitle(act, title));
        LinearLayout list = new LinearLayout(act);
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lLp = matchWrap();
        lLp.topMargin = dp(act, 8);
        box.addView(list, lLp);
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            LinearLayout row = new LinearLayout(act);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(act, 2), dp(act, 11), dp(act, 2), dp(act, 11));
            FrameLayout radio = new FrameLayout(act);
            View ring = new View(act);
            GradientDrawable ringBg = new GradientDrawable();
            ringBg.setShape(GradientDrawable.OVAL);
            ringBg.setColor(Color.TRANSPARENT);
            ringBg.setStroke(dp(act, 2), pdd(act));
            ring.setBackground(ringBg);
            radio.addView(ring, new FrameLayout.LayoutParams(dp(act, 22), dp(act, 22)));
            if (i == checked) {
                View dot = new View(act);
                GradientDrawable dotBg = new GradientDrawable();
                dotBg.setShape(GradientDrawable.OVAL);
                dotBg.setColor(pdd(act));
                dot.setBackground(dotBg);
                FrameLayout.LayoutParams dLp = new FrameLayout.LayoutParams(dp(act, 12), dp(act, 12));
                dLp.gravity = Gravity.CENTER;
                radio.addView(dot, dLp);
            }
            row.addView(radio, new LinearLayout.LayoutParams(dp(act, 22), dp(act, 22)));
            TextView lab = new TextView(act);
            lab.setText(names[i]);
            lab.setTextSize(16);
            lab.setTextColor(dark(act));
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tLp.leftMargin = dp(act, 12);
            row.addView(lab, tLp);
            row.setOnClickListener(v -> {
                dialog.dismiss();
                onPick.accept(idx);
            });
            list.addView(row);
        }
        LinearLayout btns = new LinearLayout(act);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        btns.setGravity(Gravity.END);
        TextView cancel = dialogButton(act, "取消", false);
        cancel.setOnClickListener(v -> dialog.dismiss());
        btns.addView(cancel);
        box.addView(btns);
        dialog.show();
    }
}
