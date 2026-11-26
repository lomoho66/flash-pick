package com.lomoho.quickpick;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 「更多」页：
 * 顶部简介，下面依次是 2x2 小组件定制、关于。
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = Ui.page(this, "更多");

        // 信息卡：版本 + 介绍
        LinearLayout info = Ui.whiteCard(this);
        TextView ver = new TextView(this);
        String v = "2.0";
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        ver.setText("闪取  " + v);
        ver.setTextSize(15);
        ver.setTextColor(Ui.dark(this));
        info.addView(ver);
        // 简介压成一行，完整介绍在「作者信息」页
        TextView intro = new TextView(this);
        intro.setText("不联网 · 零权限 · 帮你在驿站取件时节约时间");
        intro.setTextSize(14);
        intro.setTextColor(Ui.secondary(this));
        LinearLayout.LayoutParams inLp = Ui.matchWrap();
        inLp.topMargin = Ui.dp(this, 6);
        info.addView(intro, inLp);
        LinearLayout.LayoutParams infoLp = Ui.matchWrap();
        infoLp.topMargin = Ui.dp(this, 20);
        root.addView(info, infoLp);


        // 小组件：四个 1×1 固定入口无需设置；这里只管 2×2 双入口的上下自选与预览
        root.addView(groupLabel("2x2 小组件定制"));
        LinearLayout g2 = group();
        g2.addView(widgetPreview());
        g2.addView(hairline());
        g2.addView(slotRow("large1", "上"));
        g2.addView(hairline());
        g2.addView(slotRow("large2", "下"));
        root.addView(g2);


        // 关于类目：只有作者信息（打赏入口已移进作者信息页下方）
        root.addView(groupLabel("关于"));
        LinearLayout g3 = group();
        g3.addView(menuRow(Ui.night(this) ? R.drawable.ic_info_gold : R.drawable.ic_info_red, "作者信息", "联系与支持",
                () -> startActivity(new Intent(this, AboutActivity.class))));
        root.addView(g3);

        setContentView(Ui.scrollWrap(this, root));
    }

    // ---------- 小部件 ----------

    private TextView groupLabel(String text) {
        TextView l = new TextView(this);
        l.setText(text);
        l.setTextSize(13);
        l.setTextColor(Ui.grey(this));
        LinearLayout.LayoutParams lp = Ui.matchWrap();
        lp.topMargin = Ui.dp(this, 20);
        lp.leftMargin = Ui.dp(this, 16);
        lp.bottomMargin = Ui.dp(this, 6);
        l.setLayoutParams(lp);
        return l;
    }

    private LinearLayout group() {
        // 与全 App 卡片统一：同色（Ui.card）、同圆角 20（原 14 圆角 + 深色 #2C2C2E 与其他页不一致，已修）
        LinearLayout g = new LinearLayout(this);
        g.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Ui.card(this));
        bg.setCornerRadius(Ui.dp(this, 20));
        g.setBackground(bg);
        g.setLayoutParams(Ui.matchWrap());
        return g;
    }

    private LinearLayout settingRow(View right, String label, String desc) {
        return settingRow(right, label, desc, null);
    }

    private LinearLayout settingRow(View right, String label, String desc, Runnable click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 11), Ui.dp(this, 14), Ui.dp(this, 11));
        row.setMinimumHeight(Ui.dp(this, 52));
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(16);
        t.setTextColor(Ui.dark(this));
        texts.addView(t);
        if (desc != null) {
            TextView d = new TextView(this);
            d.setText(desc);
            d.setTextSize(12);
            d.setTextColor(Ui.grey(this));
            texts.addView(d);
        }
        row.addView(texts);
        if (right != null) {
            row.addView(right);
        }
        if (click != null) {
            row.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                click.run();
            });
        }
        return row;
    }

    private View hairline() {
        View v = new View(this);
        v.setBackgroundColor(Ui.separator(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Ui.dp(this, 1) / 2));
        lp.leftMargin = Ui.dp(this, 14);
        v.setLayoutParams(lp);
        return v;
    }

    /** 槽位入口选择行：右侧当前绑定值 + 箭头，点开列表换绑。 */
    private LinearLayout slotRow(String slot, String label) {
        Platforms.Entry cur = Platforms.find(Prefs.slotEntry(this, slot));
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        TextView right = new TextView(this);
        right.setText(cur.shortLabel);
        right.setTextSize(14);
        right.setTextColor(Ui.grey(this));
        box.addView(right);
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(Ui.night(this) ? R.drawable.ic_chev_dark : R.drawable.ic_chev);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(Ui.dp(this, 9), Ui.dp(this, 15));
        aLp.leftMargin = Ui.dp(this, 8);
        box.addView(arrow, aLp);
        return settingRow(box, label, null, () -> pickEntry(slot));
    }

    /** 双入口组件实时预览：按当前上下绑定画一张 2×2 缩略图（与真实组件同构：两行品牌图 + 简称）。 */
    private LinearLayout widgetPreview() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12));
        GradientDrawable bg = new GradientDrawable();
        // 预览区底色走主题中性色：浅 = 页面底色内嵌感，深 = 比卡亮一阶
        bg.setColor(Ui.night(this) ? android.graphics.Color.rgb(0x2C, 0x2C, 0x2E) : Ui.BG);
        bg.setCornerRadius(Ui.dp(this, 16));
        box.setBackground(bg);
        box.addView(previewRow(Platforms.find(Prefs.slotEntry(this, "large1"))));
        LinearLayout second = previewRow(Platforms.find(Prefs.slotEntry(this, "large2")));
        LinearLayout.LayoutParams sLp = Ui.matchWrap();
        sLp.topMargin = Ui.dp(this, 10);
        box.addView(second, sLp);
        LinearLayout.LayoutParams lp = Ui.matchWrap();
        lp.setMargins(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 4));
        box.setLayoutParams(lp);
        return box;
    }

    private LinearLayout previewRow(Platforms.Entry e) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        ImageView icon = new ImageView(this);
        icon.setImageResource(WidgetViews.menuIcon(e.id, Ui.night(this)));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        row.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 54), Ui.dp(this, 36)));
        TextView lab = new TextView(this);
        lab.setText(WidgetViews.duoLabel(e));
        lab.setTextSize(14);
        lab.setTypeface(Typeface.DEFAULT_BOLD);
        lab.setTextColor(Ui.dark(this));
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lLp.leftMargin = Ui.dp(this, 10);
        row.addView(lab, lLp);
        return row;
    }

    private void pickEntry(String slot) {
        java.util.List<Platforms.Entry> entries = Platforms.allEntries();
        String[] names = new String[entries.size()];
        int checked = 0;
        String curId = Prefs.slotEntry(this, slot);
        for (int i = 0; i < entries.size(); i++) {
            names[i] = entries.get(i).shortLabel;
            if (entries.get(i).id.equals(curId)) {
                checked = i;
            }
        }
        Ui.singleChoice(this, "选择入口", names, checked, which -> {
            Prefs.setSlotEntry(this, slot, entries.get(which).id);
            WidgetViews.refreshAll(this);
            recreate();
        });
    }

        /** 类目内的子菜单行：红色浅底图标块 + 标题 + 说明 + 箭头，整行可点。 */
    private LinearLayout menuRow(int iconRes, String titleText, String descText, Runnable click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 11), Ui.dp(this, 14), Ui.dp(this, 11));
        row.setMinimumHeight(Ui.dp(this, 52));

        ImageView icon = new ImageView(this);
        int ip = Ui.dp(this, 7);
        icon.setPadding(ip, ip, ip, ip);
        GradientDrawable chipBg = new GradientDrawable();
        chipBg.setColor(Ui.chipTint(this));
        chipBg.setCornerRadius(Ui.dp(this, 8));
        icon.setBackground(chipBg);
        icon.setImageResource(iconRes);
        row.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 30), Ui.dp(this, 30)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams txLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        txLp.leftMargin = Ui.dp(this, 12);
        texts.setLayoutParams(txLp);
        TextView t = new TextView(this);
        t.setText(titleText);
        t.setTextSize(16);
        t.setTextColor(Ui.dark(this));
        texts.addView(t);
        TextView d = new TextView(this);
        d.setText(descText);
        d.setTextSize(12);
        d.setTextColor(Ui.grey(this));
        texts.addView(d);
        row.addView(texts);

        ImageView arrow = new ImageView(this);
        arrow.setImageResource(Ui.night(this) ? R.drawable.ic_chev_dark : R.drawable.ic_chev);
        row.addView(arrow, new LinearLayout.LayoutParams(Ui.dp(this, 9), Ui.dp(this, 15)));

        row.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            click.run();
        });
        return row;
    }
}
