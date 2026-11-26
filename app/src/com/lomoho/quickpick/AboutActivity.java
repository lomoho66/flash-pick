package com.lomoho.quickpick;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 作者信息页：作者链接（值用灰色 + 右箭头、整行可点，不再用红下划线）、
 * 关于闪取（完整介绍从设置页搬来）、致谢、版本号与检查更新 / 反馈问题。
 */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = Ui.page(this, "作者信息");

        // 英雄区：头像靠左 + 右侧作者介绍（头像不裁圆角）
        LinearLayout heroWrap = new LinearLayout(this);
        heroWrap.setOrientation(LinearLayout.HORIZONTAL);
        // 右侧文字块底部与头像下边缘齐平
        heroWrap.setGravity(Gravity.BOTTOM);
        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.drawable.author_avatar);
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(Ui.dp(this, 104), Ui.dp(this, 104));
        avLp.rightMargin = Ui.dp(this, 14);
        heroWrap.addView(avatar, avLp);
        LinearLayout heroTexts = new LinearLayout(this);
        heroTexts.setOrientation(LinearLayout.VERTICAL);
        heroTexts.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView heroName = new TextView(this);
        heroName.setText("@lomoho");
        heroName.setTextSize(16);
        heroName.setTypeface(Typeface.DEFAULT_BOLD);
        heroName.setTextColor(Ui.dark(this));
        heroTexts.addView(heroName);
        TextView heroIntro = new TextView(this);
        heroIntro.setText("本项目由作者独立开发维护，已开源至 GitHub。加入频道，第一时间获取最新资讯。");
        heroIntro.setTextSize(14);
        heroIntro.setTextColor(Ui.secondary(this));
        heroIntro.setLineSpacing(0, 1.35f);
        LinearLayout.LayoutParams hiLp = Ui.matchWrap();
        hiLp.topMargin = Ui.dp(this, 6);
        heroTexts.addView(heroIntro, hiLp);
        heroWrap.addView(heroTexts);
        LinearLayout.LayoutParams heroLp = Ui.matchWrap();
        heroLp.topMargin = Ui.dp(this, 26);
        root.addView(heroWrap, heroLp);

        LinearLayout card = Ui.whiteCard(this);
        LinearLayout.LayoutParams cardLp = Ui.matchWrap();
        cardLp.topMargin = Ui.dp(this, 16);
        card.setLayoutParams(cardLp);

        card.addView(heading("作者"));
        // 三行都不露用户名/链接文本，只留名称 + 箭头，点开直达；图标浅色红、深色金
        boolean night = Ui.night(this);
        card.addView(linkRow(night ? R.drawable.logo_coolapk_gold : R.drawable.logo_coolapk_red, "酷安", null,
                () -> openUrl("https://www.coolapk.com/u/514048")), rowLp(12));
        card.addView(linkRow(night ? R.drawable.logo_github_gold : R.drawable.logo_github_red, "GitHub", null,
                () -> openUrl("https://github.com/lomoho66")), rowLp(12));
        card.addView(linkRow(night ? R.drawable.ic_tg_gold : R.drawable.ic_tg_red, "TG频道", null,
                () -> openUrl("https://t.me/lom0ho")), rowLp(12));

        card.addView(Ui.divider(this));
        card.addView(heading("致谢"));
        card.addView(iconRow("技术支持", R.drawable.muse_logo, "Muse by Meta"), rowLp(12));
        card.addView(iconRow("设计支持", R.drawable.claude_logo, "Claude by Anthropic"), rowLp(12));

        card.addView(Ui.divider(this));
        String v = "";
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        card.addView(Ui.infoRow(this, "版本号", v));
        // 检查更新：首发版点击弹提示文案
        card.addView(navRow("检查更新", () -> android.widget.Toast.makeText(this,
                "首发版本暂无更新，敬请关注作者后续动态", android.widget.Toast.LENGTH_LONG).show()
        ), rowLp(12));
        card.addView(navRow("反馈问题", this::sendMail), rowLp(12));

        root.addView(card);

        // 打赏入口：页面下方按钮，整宽居中；点击直达爱发电
        // 图标+文字用横排容器整体居中（TextView 复合图标只按文字居中，图标会看着漂移）
        LinearLayout donate = new LinearLayout(this);
        donate.setOrientation(LinearLayout.HORIZONTAL);
        donate.setGravity(Gravity.CENTER);
        android.graphics.drawable.GradientDrawable dBg = new android.graphics.drawable.GradientDrawable();
        int donateInk;
        if (Ui.night(this)) {
            // 深色：金渐变 + 深棕字/图标（白字在金底上对比不够）
            dBg.setColors(new int[]{android.graphics.Color.rgb(0xF2, 0xD3, 0x8A), android.graphics.Color.rgb(0xDF, 0xA8, 0x3F)});
            donateInk = android.graphics.Color.rgb(0x3A, 0x2A, 0x08);
        } else {
            dBg.setColors(new int[]{android.graphics.Color.rgb(0xFF, 0x5A, 0x4F), android.graphics.Color.rgb(0xE0, 0x2E, 0x24)});
            donateInk = android.graphics.Color.WHITE;
        }
        dBg.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TL_BR);
        // 圆角与上方卡片统一为 20dp
        dBg.setCornerRadius(Ui.dp(this, 20));
        donate.setBackground(dBg);
        ImageView coffee = new ImageView(this);
        coffee.setImageResource(Ui.night(this) ? R.drawable.ic_coffee_dark : R.drawable.ic_coffee_white);
        LinearLayout.LayoutParams coffeeLp = new LinearLayout.LayoutParams(Ui.dp(this, 26), Ui.dp(this, 26));
        coffeeLp.rightMargin = Ui.dp(this, 9);
        donate.addView(coffee, coffeeLp);
        TextView donateText = new TextView(this);
        donateText.setText("请作者喝杯咖啡");
        donateText.setTextSize(21);
        donateText.setTypeface(Typeface.DEFAULT_BOLD);
        donateText.setTextColor(donateInk);
        donate.addView(donateText);
        donate.setOnClickListener(vw -> {
            vw.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            openUrl("https://afdian.com/a/lomoho");
        });
        LinearLayout.LayoutParams dwLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 68));
        // 与卡片同宽（同在 root 20dp 内边距里）、间距对齐卡片上边距 16dp，视觉上成一个整体
        dwLp.topMargin = Ui.dp(this, 16);
        root.addView(donate, dwLp);

        setContentView(Ui.scrollWrap(this, root));
    }

    private TextView heading(String text) {
        TextView h = new TextView(this);
        h.setText(text);
        h.setTextSize(13);
        h.setTextColor(Ui.grey(this));
        return h;
    }

    private LinearLayout.LayoutParams rowLp(int topDp) {
        LinearLayout.LayoutParams lp = Ui.matchWrap();
        lp.topMargin = Ui.dp(this, topDp);
        return lp;
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception ignored) {
        }
    }

    private void sendMail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:lomoho@duck.com"));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "没找到邮箱 App", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private ImageView chevron() {
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(Ui.night(this) ? R.drawable.ic_chev_dark : R.drawable.ic_chev);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 9), Ui.dp(this, 15));
        lp.leftMargin = Ui.dp(this, 8);
        arrow.setLayoutParams(lp);
        return arrow;
    }

    /** 链接行：图标 + 标签 + 灰色值 + 箭头，整行可点。 */
    private LinearLayout linkRow(int iconRes, String label, String display, Runnable click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            click.run();
        });
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(Ui.dp(this, 24), Ui.dp(this, 24));
        iconLp.rightMargin = Ui.dp(this, 10);
        row.addView(icon, iconLp);
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(16);
        l.setTextColor(Ui.dark(this));
        row.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (display != null) {
            TextView r = new TextView(this);
            r.setText(display);
            r.setTextSize(16);
            r.setTextColor(Ui.grey(this));
            r.setGravity(Gravity.END);
            row.addView(r);
        }
        row.addView(chevron());
        return row;
    }

    /** 致谢行：标签 + 品牌图标与署名（图标 20dp，与文字对齐）。 */
    private LinearLayout iconRow(String label, int iconRes, String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(15);
        l.setTextColor(Ui.dark(this));
        row.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(Ui.dp(this, 20), Ui.dp(this, 20));
        iconLp.rightMargin = Ui.dp(this, 5);
        row.addView(icon, iconLp);
        TextView r = new TextView(this);
        r.setText(text);
        r.setTextSize(15);
        r.setTextColor(Ui.grey(this));
        row.addView(r);
        return row;
    }

    /** 导航行：只有标签 + 箭头，整行可点。 */
    private LinearLayout navRow(String label, Runnable click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            click.run();
        });
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(15);
        l.setTextColor(Ui.dark(this));
        row.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(chevron());
        return row;
    }
}
