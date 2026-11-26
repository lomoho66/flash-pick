package com.lomoho.quickpick;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 取件入口表：只收录拼多多与菜鸟（两家有可直达的待取列表与身份码页）。
 * 深链为各 App 对外暴露的标准 scheme，属于公开的互操作接口。
 * 每个入口的跳转顺序：主链接（带包名）→ 主链接（不带包名）→ 备用链接 → 拉起 App → 网页兜底 → 提示未安装。
 */
public final class Platforms {

    public static final class Entry {
        public final String id;          // 唯一 ID，如 pdd_packages
        public final String platformId;  // 平台 ID
        public final String platformName;
        public final String label;       // 入口名：取快递 / 身份码 / 待收货 …
        public final String cardDesc;    // 首页卡片副标题
        public final String shortLabel;  // 小组件 / 菜单里的简称，如「拼多多取件」
        public final String glyph;       // 文字版小组件大字：取 / ID / 收 / 单
        public final boolean primary;    // 首页卡片是否用红色主样式
        public final String[] pkgs;      // 目标 App 包名（按优先级，逐个尝试）
        public final String[] nativeUris; // 主 App 原生深链（仅主 App 已装时用，限定主包名），可为 null
        public final String[] uris;      // 主链接 + 备用链接，依次尝试
        public final String web;         // 网页兜底，可为 null

        Entry(String id, String platformId, String platformName, String label, String cardDesc,
              String shortLabel, String glyph, boolean primary, String web, String[] pkgs,
              String[] nativeUris, String... uris) {
            this.id = id;
            this.platformId = platformId;
            this.platformName = platformName;
            this.label = label;
            this.cardDesc = cardDesc;
            this.shortLabel = shortLabel;
            this.glyph = glyph;
            this.primary = primary;
            this.pkgs = pkgs;
            this.nativeUris = nativeUris;
            this.web = web;
            this.uris = uris;
        }
    }

    public static final class Platform {
        public final String id;
        public final String name;
        public final List<Entry> entries;

        Platform(String id, String name, Entry... entries) {
            this.id = id;
            this.name = name;
            this.entries = Arrays.asList(entries);
        }
    }

    private static final String PKG_PDD = "com.xunmeng.pinduoduo";
    private static final String PKG_CAINIAO = "com.cainiao.wireless";
    private static final String PKG_TAOBAO = "com.taobao.taobao";

    public static final List<Platform> ALL = new ArrayList<>();

    static {
        ALL.add(new Platform("pdd", "拼多多",
                new Entry("pdd_packages", "pdd", "拼多多", "取快递", "驿站待取列表",
                        "拼多多取件", "取", true,
                        "https://m.pinduoduo.net/mdkd/package?entry_source=18",
                        new String[]{PKG_PDD}, null,
                        // 必须显式带 tab=PACKAGE，否则从身份码页切不回来
                        "pinduoduo://com.xunmeng.pinduoduo/mdkd/package?tab=PACKAGE&entry_source=11"),
                new Entry("pdd_code", "pdd", "拼多多", "身份码", "驿站身份码",
                        "拼多多身份码", "ID", false,
                        "https://m.pinduoduo.net/mdkd/package?entry_source=18",
                        new String[]{PKG_PDD}, null,
                        "pinduoduo://com.xunmeng.pinduoduo/mdkd/package?tab=ID_CODE&entry_source=11")));

        // 菜鸟入口分两段：装了菜鸟只走原生段（nativeUris 限定菜鸟包名，
        // 深链全灭也只拉起菜鸟本体，绝不串到淘宝）；没装菜鸟才走 uris 段的淘宝内嵌页兜底。
        // 注：cainiao://desktop/package 在现行安卓菜鸟上疑似未注册，深链抛异常会顺链滑到淘宝，
        // 故原生段与兜底段分开处理。
        ALL.add(new Platform("cainiao", "菜鸟",
                new Entry("cainiao_packages", "cainiao", "菜鸟", "取快递", "驿站待取列表",
                        "菜鸟取件", "取", true,
                        // 网页兜底：淘宝内嵌菜鸟驿站页的裸 H5 地址（m-end-school-tab/home）
                        "https://pages-fast.m.taobao.com/wow/z/uniapp/1100333/last-mile-fe/m-end-school-tab/home",
                        new String[]{PKG_CAINIAO, PKG_TAOBAO},
                        new String[]{
                                "cainiao://desktop/package",
                                "cainiao://",
                                "guoguo://"},
                        // 兜底落点 = 淘宝内嵌的菜鸟驿站页（uniapp 1100333 的 m-end-school-tab/home），三条候选依次尝试；
                        // 与淘宝「待取」（olist 订单页）是两个落点，别混
                        "taobao://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fpages-fast.m.taobao.com%2Fwow%2Fz%2Funiapp%2F1100333%2Flast-mile-fe%2Fm-end-school-tab%2Fhome",
                        "tbopen://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fpages-fast.m.taobao.com%2Fwow%2Fz%2Funiapp%2F1100333%2Flast-mile-fe%2Fm-end-school-tab%2Fhome",
                        "taobao://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fpages-fast.m.taobao.com%2Fwow%2Fz%2Funiapp%2F1100333%2Flast-mile-fe%2Fm-end-school-tab%2Fhome%3Ffrom%3Dtb"),
                new Entry("cainiao_code", "cainiao", "菜鸟", "身份码", "驿站身份码",
                        "菜鸟身份码", "ID", false, null,
                        new String[]{PKG_CAINIAO, PKG_TAOBAO},
                        new String[]{
                                "cainiao://desktop/station_code",
                                "cainiao://router/station_code",
                                "cainiao://"},
                        "tbopen://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fmarket.m.taobao.com%2Fapp%2Fcn-yz%2Fmulti-activity%2FauthCode.html",
                        "taobao://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fmarket.m.taobao.com%2Fapp%2Fcn-yz%2Fmulti-activity%2FauthCode.html",
                        "tbopen://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fpages-fast.m.taobao.com%2Fwow%2Fz%2Funiapp%2F1011717%2Flast-mile-fe%2Fend-collect-platform%2Fidentity-code%3Fx-ssr%3Dtrue")));

    }

    private Platforms() {
    }

    public static Entry find(String id) {
        if (id != null) {
            for (Platform p : ALL) {
                for (Entry e : p.entries) {
                    if (e.id.equals(id)) {
                        return e;
                    }
                }
            }
        }
        return ALL.get(0).entries.get(0);
    }

    public static List<Entry> allEntries() {
        List<Entry> out = new ArrayList<>();
        for (Platform p : ALL) {
            out.addAll(p.entries);
        }
        return out;
    }

    /** 依次尝试该入口的全部链接与兜底，全失败才提示未安装。 */
    public static void open(Context ctx, String entryId) {
        Entry e = find(entryId);
        if (e.nativeUris != null) {
            // 原生段：主 App 已装就只在它里面解决（深链 → 拉起本体），绝不滑到兜底 App
            String mainPkg = e.pkgs[0];
            if (isInstalled(ctx, mainPkg)) {
                for (String uri : e.nativeUris) {
                    if (tryView(ctx, uri, mainPkg)) {
                        return;
                    }
                }
                if (launchApp(ctx, mainPkg)) {
                    return;
                }
            }
            // 主 App 未装：走 uris 兜底链（如淘宝内嵌页），再不行拉起兜底 App
            for (String uri : e.uris) {
                for (String pkg : e.pkgs) {
                    if (tryView(ctx, uri, pkg)) {
                        return;
                    }
                }
                if (tryView(ctx, uri, null)) {
                    return;
                }
            }
            for (String pkg : e.pkgs) {
                if (launchApp(ctx, pkg)) {
                    return;
                }
            }
            if (e.web != null && tryView(ctx, e.web, null)) {
                return;
            }
            Toast.makeText(ctx, "未检测到" + e.platformName + " App", Toast.LENGTH_LONG).show();
            return;
        }
        for (String uri : e.uris) {
            for (String pkg : e.pkgs) {
                if (tryView(ctx, uri, pkg)) {
                    return;
                }
            }
            if (tryView(ctx, uri, null)) {
                return;
            }
        }
        // 至少把目标 App 打开（按包名优先级逐个试）
        for (String pkg : e.pkgs) {
            if (launchApp(ctx, pkg)) {
                return;
            }
        }
        if (e.web != null && tryView(ctx, e.web, null)) {
            return;
        }
        Toast.makeText(ctx, "未检测到" + e.platformName + " App", Toast.LENGTH_LONG).show();
    }

    /** 入口的主 App 是否已装（首页「未安装」置灰态用，判定与跳转的原生段一致）。 */
    public static boolean installed(Context ctx, Entry e) {
        return isInstalled(ctx, e.pkgs[0]);
    }

    private static boolean isInstalled(Context ctx, String pkg) {
        try {
            return ctx.getPackageManager().getLaunchIntentForPackage(pkg) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean launchApp(Context ctx, String pkg) {
        try {
            Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(pkg);
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(launch);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean tryView(Context ctx, String uri, String pkg) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (pkg != null) {
                i.setPackage(pkg);
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
            return true;
        } catch (ActivityNotFoundException | SecurityException ignored) {
            return false;
        }
    }
}
