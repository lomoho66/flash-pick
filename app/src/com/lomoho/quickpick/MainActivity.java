package com.lomoho.quickpick;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 主页（苹果风）：大标题、左上作者徽标、右上明暗切换，
 * 按平台分组的入口卡（只显示在「更多」里开启的平台）；右上齿轮进「更多」页，旁边是外观三档按钮。
 * 交互：点按震动反馈、弹簧式按压缩放、开场一次淡入上浮。
 */
public class MainActivity extends Activity {

    // 桌面图标别名只能在 App 真正退到后台时切换（前台切换会被 ColorOS 杀进程）。
    // 注意 recreate() 是旧实例先 stop、新实例后 start，计数会瞬时归零，在切换明暗的半途
    // 触发别名切换照样被杀。故：配置变更引起的 stop 直接跳过；其余归零先挂 250ms 延迟
    // （只防瞬时 stop-start 抖动），期间有新 Activity start 就取消。
    private static boolean iconHookInstalled = false;
    private static int startedActivities = 0;
    private static final android.os.Handler ICON_HANDLER =
            new android.os.Handler(android.os.Looper.getMainLooper());
    private static Runnable pendingIconApply;

    private void installIconHook() {
        if (iconHookInstalled) {
            return;
        }
        iconHookInstalled = true;
        final android.content.Context appCtx = getApplicationContext();
        getApplication().registerActivityLifecycleCallbacks(new android.app.Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStarted(Activity a) {
                startedActivities++;
                if (pendingIconApply != null) {
                    ICON_HANDLER.removeCallbacks(pendingIconApply);
                    pendingIconApply = null;
                }
            }

            @Override
            public void onActivityStopped(Activity a) {
                startedActivities--;
                if (startedActivities < 0) {
                    startedActivities = 0;
                }
                if (startedActivities > 0 || a.isChangingConfigurations()) {
                    return;
                }
                if (pendingIconApply != null) {
                    ICON_HANDLER.removeCallbacks(pendingIconApply);
                }
                pendingIconApply = () -> {
                    pendingIconApply = null;
                    if (startedActivities <= 0) {
                        Prefs.applyIconForTheme(appCtx);
                    }
                };
                ICON_HANDLER.postDelayed(pendingIconApply, 250);
            }

            @Override
            public void onActivityCreated(Activity a, Bundle b) {
            }

            @Override
            public void onActivityResumed(Activity a) {
            }

            @Override
            public void onActivityPaused(Activity a) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity a, Bundle b) {
            }

            @Override
            public void onActivityDestroyed(Activity a) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        installIconHook();
        Ui.edgeToEdge(this);
        boolean night = Ui.night(this);
        List<View> anim = new ArrayList<>();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 22);
        root.setPadding(pad, Ui.dp(this, 30) + Ui.statusBarHeight(this), pad, pad);

        // 顶行：作者徽标（点进关于） + 明暗切换
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView pill = new TextView(this);
        pill.setText("@lomoho 🦉");
        pill.setTextSize(12);
        pill.setTextColor(Ui.grey(this));
        int ph = Ui.dp(this, 12), pv = Ui.dp(this, 5);
        pill.setPadding(ph, pv, ph, pv);
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(Ui.neutralFill(this));
        pillBg.setCornerRadius(Ui.dp(this, 20));
        pill.setBackground(pillBg);
        pill.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
        head.addView(pill, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        View spacer = new View(this);
        head.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));

        // 设置按钮：齿轮圆钮，进「更多」页
        android.widget.FrameLayout gearBtn = new android.widget.FrameLayout(this);
        GradientDrawable gearBg = new GradientDrawable();
        gearBg.setColor(Ui.card(this));
        gearBg.setShape(GradientDrawable.OVAL);
        gearBtn.setBackground(gearBg);
        ImageView gearIcon = new ImageView(this);
        gearIcon.setImageResource(R.drawable.ic_gear);
        int gp = Ui.dp(this, 10);
        gearIcon.setPadding(gp, gp, gp, gp);
        gearBtn.addView(gearIcon, new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        gearBtn.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            startActivity(new Intent(this, SettingsActivity.class));
        });
        head.addView(gearBtn, new LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 40)));

        // 外观按钮：图标随档位（跟随系统 = 半实半空圆、浅色太阳、深色月亮）；
        // 点击直接循环切换三档（不弹菜单），带震动 + Toast 报当前档位
        android.widget.FrameLayout modeBtn = new android.widget.FrameLayout(this);
        GradientDrawable modeBg = new GradientDrawable();
        modeBg.setColor(Ui.card(this));
        modeBg.setShape(GradientDrawable.OVAL);
        modeBtn.setBackground(modeBg);
        String themeMode = Prefs.themeMode(this);
        ImageView modeIcon = new ImageView(this);
        modeIcon.setImageResource("dark".equals(themeMode) ? R.drawable.ic_moon
                : ("light".equals(themeMode) ? R.drawable.ic_sun : R.drawable.ic_theme_half));
        int mp = Ui.dp(this, 10);
        modeIcon.setPadding(mp, mp, mp, mp);
        modeBtn.addView(modeIcon, new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        modeBtn.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            String cur = Prefs.themeMode(this);
            String next = "auto".equals(cur) ? "light" : ("light".equals(cur) ? "dark" : "auto");
            Prefs.setThemeMode(this, next);
            // 桌面图标不在前台切：ColorOS 在 App 前台启停桌面别名会直接杀进程，
            // 改为退到后台时统一对齐，见 installIconHook()
            String label = "light".equals(next) ? "天亮了" : ("dark".equals(next) ? "天黑了" : "跟随系统");
            android.widget.Toast.makeText(this, label, android.widget.Toast.LENGTH_SHORT).show();
            recreate();
        });
        LinearLayout.LayoutParams modeLp = new LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 40));
        modeLp.leftMargin = Ui.dp(this, 10);
        head.addView(modeBtn, modeLp);
        root.addView(head);
        anim.add(head);

        // 大标题：品牌字标图（按品牌包 logo 复刻：「门」+ 闪电成「闪」，浅版墨字红电、深版暖白字金电）
        ImageView title = new ImageView(this);
        title.setImageResource(Ui.night(this) ? R.drawable.wordmark_dark : R.drawable.wordmark_light);
        title.setAdjustViewBounds(true);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 50));
        tLp.topMargin = Ui.dp(this, 34);
        root.addView(title, tLp);
        anim.add(title);

        TextView sub = new TextView(this);
        sub.setText("驿站取件，一触即达");
        sub.setTextSize(16);
        sub.setTextColor(Ui.grey(this));
        LinearLayout.LayoutParams sLp = Ui.matchWrap();
        sLp.topMargin = Ui.dp(this, 8);
        root.addView(sub, sLp);
        anim.add(sub);

        // 平台分组：恒显全部平台（平台开关模块已删）
        boolean anyShown = false;
        for (Platforms.Platform p : Platforms.ALL) {
            anyShown = true;
            TextView section = new TextView(this);
            section.setText(p.name);
            section.setTextSize(15);
            section.setTypeface(Typeface.DEFAULT_BOLD);
            section.setTextColor(Ui.dark(this));
            LinearLayout.LayoutParams secLp = Ui.matchWrap();
            secLp.topMargin = Ui.dp(this, 26);
            root.addView(section, secLp);
            anim.add(section);
            // 同平台两个入口横置一排，省纵向空间
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < p.entries.size(); i++) {
                LinearLayout tile = entryTile(p.entries.get(i));
                LinearLayout.LayoutParams tLp2 = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                if (i > 0) {
                    tLp2.leftMargin = Ui.dp(this, 10);
                }
                row.addView(tile, tLp2);
            }
            LinearLayout.LayoutParams rLp = Ui.matchWrap();
            rLp.topMargin = Ui.dp(this, 10);
            root.addView(row, rLp);
            anim.add(row);
        }
        if (!anyShown) {
            TextView empty = new TextView(this);
            empty.setText("还没有开启任何平台，去「更多」里打开");
            empty.setTextSize(14);
            empty.setTextColor(Ui.grey(this));
            LinearLayout.LayoutParams eLp = Ui.matchWrap();
            eLp.topMargin = Ui.dp(this, 30);
            root.addView(empty, eLp);
        }

        TextView tip = new TextView(this);
        tip.setText("添加桌面小组件/长按 APP 图标\n均可呼出快捷入口");
        tip.setLineSpacing(0, 1.3f);
        tip.setTextSize(12);
        tip.setTextColor(Ui.grey(this));
        tip.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tipLp = Ui.matchWrap();
        tipLp.topMargin = Ui.dp(this, 18);
        root.addView(tip, tipLp);
        anim.add(tip);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(root);
        scroll.setBackgroundColor(Ui.bg(this));
        setContentView(scroll);

        // 开场动画每个进程只播一次：从「更多」返回、从外部 App 切回都会 recreate，
        // 每次重播会拖慢「立刻能点」的体感，重建时直接呈现终态
        if (!sEntrancePlayed) {
            sEntrancePlayed = true;
            entrance(anim);
        }
    }

    private static boolean sEntrancePlayed = false;

    @Override
    protected void onResume() {
        super.onResume();
        // 桌面组件是静态快照、永不自刷：每次回到主页都重绘一遍，
        // 覆盖主页切外观（recreate 后回到这里）、系统深浅色变化、设置页改槽位三种情况
        WidgetViews.refreshAll(this);
        // 从「更多」改了设置回来，首页需要重建（首次 onResume 紧跟 onCreate，跳过）
        if (createdOnce) {
            recreate();
        }
        createdOnce = true;
    }

    private boolean createdOnce = false;

    /** 开场动画：依次淡入上浮，只播一次；延时封顶，避免长列表后段等太久。 */
    private void entrance(List<View> views) {
        for (int i = 0; i < views.size(); i++) {
            View v = views.get(i);
            v.setAlpha(0f);
            v.setTranslationY(Ui.dp(this, 16));
            v.animate().alpha(1f).translationY(0f)
                    .setStartDelay(Math.min(i, 8) * 60L).setDuration(550)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    /** 横置入口砖：白卡 24 圆角，图标块居中在上、名称与说明在下；主入口红渐变图标块，身份码类中性灰图标块。
     *  主 App 未装时整砖置灰、说明改「未安装」（点击仍走兜底链，行为不变）。 */
    private LinearLayout entryTile(Platforms.Entry e) {
        boolean night = Ui.night(this);
        boolean off = !Platforms.installed(this, e);
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(Ui.dp(this, 10), Ui.dp(this, 16), Ui.dp(this, 10), Ui.dp(this, 14));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Ui.card(this));
        bg.setCornerRadius(Ui.dp(this, 24));
        if (night) {
            bg.setStroke(Math.max(1, Ui.dp(this, 1) / 2), Color.argb(20, 255, 255, 255));
        }
        tile.setBackground(bg);

        // 品牌入口图（3:2 透明底）：按入口选图、深色用反白版；未安装态去饱和置灰（保留图形）
        ImageView icon = new ImageView(this);
        icon.setImageResource(menuIcon(e.id, night));
        if (off) {
            android.graphics.ColorMatrix cm = new android.graphics.ColorMatrix();
            cm.setSaturation(0f);
            icon.setColorFilter(new android.graphics.ColorMatrixColorFilter(cm));
            icon.setAlpha(0.55f);
        }
        tile.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 84), Ui.dp(this, 56)));

        TextView t = new TextView(this);
        t.setText(e.label);
        t.setTextSize(16);
        t.setTextColor(off ? Ui.grey(this) : Ui.dark(this));
        t.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tLp = Ui.matchWrap();
        tLp.topMargin = Ui.dp(this, 10);
        tile.addView(t, tLp);
        TextView d = new TextView(this);
        d.setText(off ? "未安装" : e.cardDesc);
        d.setTextSize(12);
        d.setTextColor(Ui.grey(this));
        d.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dLp = Ui.matchWrap();
        dLp.topMargin = Ui.dp(this, 2);
        tile.addView(d, dLp);

        tile.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            Platforms.open(this, e.id);
        });
        // 长按：询问是否把这个入口的单入口小组件添加到桌面
        tile.setOnLongClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            askPinWidget(e);
            return true;
        });
        attachPressScale(tile);
        return tile;
    }

    /** 桌面小组件列表里显示的名字（与清单 label 一致，手动添加时按这个找）。 */
    private static String widgetLabel(Platforms.Entry e) {
        switch (e.id) {
            case "pdd_packages":
                return "闪取 · 多多取件";
            case "pdd_code":
                return "闪取 · 多多身份码";
            case "cainiao_packages":
                return "闪取 · 菜鸟取件";
            default:
                return "闪取 · 菜鸟身份码";
        }
    }

    /**
     * 长按入口砖 → 添加小组件。
     * ColorOS 桌面不执行 requestPinAppWidget（请求被接受但不落桌面、回调也不回），
     * 手动添加正常。故弹窗先把手动路径说清，自动添加只作为尝试。
     */
    private void askPinWidget(Platforms.Entry e) {
        Ui.confirm(this, "添加小组件",
                "自动添加对部分桌面（如 ColorOS）不生效。若添加后桌面没出现，请长按桌面空白处 → 小组件 → 选「"
                        + widgetLabel(e) + "」拖到桌面。",
                "自动添加", () -> pinWidget(e));
    }

    private void pinWidget(Platforms.Entry e) {
        android.appwidget.AppWidgetManager awm = android.appwidget.AppWidgetManager.getInstance(this);
        if (!awm.isRequestPinAppWidgetSupported()) {
            android.widget.Toast.makeText(this, "当前桌面不支持自动添加，请长按桌面手动添加小组件",
                    android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        Class<?> provider;
        switch (e.id) {
            case "pdd_packages":
                provider = WidgetPddPick.class;
                break;
            case "pdd_code":
                provider = WidgetPddCode.class;
                break;
            case "cainiao_packages":
                provider = WidgetCnPick.class;
                break;
            default:
                provider = WidgetCnCode.class;
                break;
        }
        // 成功回调：桌面真正把组件放上后，PinCallbackReceiver 会弹「已添加到桌面」
        android.content.Intent cb = new android.content.Intent(this, PinCallbackReceiver.class)
                .setAction(PinCallbackReceiver.ACTION);
        android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(this, 0, cb,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        boolean ok = awm.requestPinAppWidget(new android.content.ComponentName(this, provider), null, pi);
        android.widget.Toast.makeText(this,
                ok ? "已发送添加请求，若桌面未出现请手动添加" : "添加失败，请长按桌面手动添加",
                android.widget.Toast.LENGTH_LONG).show();
    }

    /** 入口 → 品牌图标（深色模式用反白版）。 */
    private int menuIcon(String id, boolean night) {
        switch (id) {
            case "pdd_packages":
                return night ? R.drawable.menu_pdd_pickup_dark : R.drawable.menu_pdd_pickup;
            case "pdd_code":
                return night ? R.drawable.menu_pdd_idcode_dark : R.drawable.menu_pdd_idcode;
            case "cainiao_packages":
                return night ? R.drawable.menu_cainiao_pickup_dark : R.drawable.menu_cainiao_pickup;
            default:
                return night ? R.drawable.menu_cainiao_idcode_dark : R.drawable.menu_cainiao_idcode;
        }
    }

    /** 弹簧式按压反馈：按下缩到 0.965 带回弹，松开弹回。 */
    private void attachPressScale(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.965f).scaleY(0.965f).setDuration(160)
                            .setInterpolator(new OvershootInterpolator(1.5f)).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(280)
                            .setInterpolator(new OvershootInterpolator(1.5f)).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }
}
