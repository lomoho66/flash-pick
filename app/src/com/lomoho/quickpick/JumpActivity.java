package com.lomoho.quickpick;

import android.app.Activity;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.widget.FrameLayout;

/**
 * 透明跳板 Activity：接住小组件 / 长按菜单的点击，震一下（系统触感反馈，不需要震动权限）再跳转，随即关闭。
 * extra "target" 的值是 Platforms 里的入口 ID，如 pdd_packages。
 */
public class JumpActivity extends Activity {

    public static final String EXTRA_TARGET = "target";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        setContentView(frame);
        String target = getIntent() != null ? getIntent().getStringExtra(EXTRA_TARGET) : null;
        String dest = target == null ? "pdd_packages" : target;
        frame.post(() -> {
            frame.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            Platforms.open(this, dest);
            finish();
        });
    }
}
