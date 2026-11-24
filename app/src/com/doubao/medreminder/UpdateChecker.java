package com.doubao.medreminder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 检查更新：从 GitHub 仓库里的 updates/version.json 读最新版本号，
 * 有新版就下载 APK 并交给系统安装。
 */
public class UpdateChecker {

    /** GitHub raw 地址，仓库或分支换了只改这里 */
    public static final String VERSION_URL =
            "https://raw.githubusercontent.com/Himzer240/medreminder/main/updates/version.json";

    private static final Handler main = new Handler(Looper.getMainLooper());

    public interface OnCheck {
        void onResult(boolean checked, String message);
    }

    /** 有没有网（连上 WiFi 或移动数据，且系统认为可用） */
    public static boolean isOnline(Context c) {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                    c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return true;
            android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        } catch (Throwable e) {
            return true;
        }
    }

    public static void check(final Context c, final OnCheck cb) {
        // 先看下载目录里有没有已经下好、但没装成的包；有更高的版本就直接装
        final Pkg local = bestLocal(c);
        if (local != null) {
            promptInstallLocal(c, local, cb);
            return;
        }
        checkRemote(c, cb);
    }

    /** 已经下载好、版本号高于当前安装版本的那个包 */
    private static final class Pkg {
        File file;
        int code;
        String name;
    }

    private static Pkg bestLocal(Context c) {
        try {
            int installed = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode;
            File dir = ApkProvider.dir(c);
            File[] fs = dir.listFiles();
            if (fs == null) return null;
            Pkg best = null;
            for (File f : fs) {
                String n = f.getName();
                if (!n.endsWith(".apk")) continue;
                android.content.pm.PackageInfo pi =
                        c.getPackageManager().getPackageArchiveInfo(f.getAbsolutePath(), 0);
                if (pi == null) continue;
                if (pi.versionCode <= installed) continue;
                if (best == null || pi.versionCode > best.code) {
                    best = new Pkg();
                    best.file = f;
                    best.code = pi.versionCode;
                    best.name = pi.versionName;
                }
            }
            return best;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void promptInstallLocal(final Context c, final Pkg pkg, final OnCheck cb) {
        new Loc(c)
                .setTitle("发现已下载的安装包")
                .setMessage("下载目录里有 v" + pkg.name + "（比现在装的版本新），直接安装吗？\n"
                        + "取消的话会再去 GitHub 检查一次")
                .setNegativeButton("去检查", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        checkRemote(c, cb);
                    }
                })
                .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        install(c, pkg.file);
                        if (cb != null) cb.onResult(true, "已打开安装器：" + pkg.name);
                    }
                })
                .show();
    }

    private static void checkRemote(final Context c, final OnCheck cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg;
                try {
                    JSONObject o = fetch(VERSION_URL);
                    final int remoteCode = o.optInt("versionCode", 0);
                    final String remoteName = o.optString("versionName", "");
                    final String url = o.optString("url", "");
                    final String note = o.optString("note", "");
                    int localCode = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode;
                    if (remoteCode <= localCode) {
                        final String m = "已经是最新版（" + remoteName + "）";
                        main.post(new Runnable() {
                            @Override
                            public void run() {
                                if (cb != null) cb.onResult(true, m);
                            }
                        });
                        return;
                    }
                    final String nm = remoteName;
                    final String nt = note;
                    final String u = url;
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cb != null) cb.onResult(true, "发现新版本 " + nm);
                            promptDownload(c, nm, nt, u);
                        }
                    });
                    return;
                } catch (Throwable e) {
                    msg = "检查更新失败：" + shortMsg(e);
                }
                final String m = msg;
                main.post(new Runnable() {
                    @Override
                    public void run() {
                        if (cb != null) cb.onResult(false, m);
                    }
                });
            }
        }).start();
    }

    private static JSONObject fetch(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("User-Agent", "MedReminder");
        InputStream in = conn.getInputStream();
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[2048];
        int n;
        while ((n = in.read(buf)) > 0) sb.append(new String(buf, 0, n, "UTF-8"));
        in.close();
        conn.disconnect();
        return new JSONObject(sb.toString());
    }

    private static void promptDownload(final Context c, final String name, final String note, final String url) {
        String msg = "最新版本：" + name + (note.isEmpty() ? "" : ("\n" + note)) + "\n\n现在下载并安装吗？";
        new Loc(c)
                .setTitle("发现新版本")
                .setMessage(msg)
                .setNegativeButton("以后再说", null)
                .setPositiveButton("下载安装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        download(c, name, url);
                    }
                })
                .show();
    }

    private static void download(final Context c, final String name, final String url) {
        final TextView tv = new TextView(c);
        tv.setPadding(46, 46, 46, 30);
        tv.setTextSize(14);
        tv.setText("正在下载 " + name + "…");
        final AlertDialog dlg = new Loc(c)
                .setTitle("下载更新")
                .setView(tv)
                .setCancelable(false)
                .create();
        dlg.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                File out = new File(ApkProvider.dir(c), "update-" + name + ".apk");
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", "MedReminder");
                    InputStream in = conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(out);
                    byte[] buf = new byte[4096];
                    long total = 0;
                    int n;
                    while ((n = in.read(buf)) > 0) {
                        fos.write(buf, 0, n);
                        total += n;
                        final long t = total;
                        main.post(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText("正在下载 " + name + "…\n已接收 " + (t / 1024) + " KB");
                            }
                        });
                    }
                    fos.close();
                    in.close();
                    conn.disconnect();
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            dlg.dismiss();
                            install(c, out);
                        }
                    });
                } catch (Throwable e) {
                    final String m = "下载失败：" + shortMsg(e);
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            dlg.dismiss();
                            Loc.toast(c, m, android.widget.Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private static void install(Context c, File apk) {
        try {
            Uri uri = ApkProvider.uriFor(apk);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            c.startActivity(i);
        } catch (Throwable e) {
            Loc.toast(c, "打不开安装器，请到应用里手动安装：" + apk.getAbsolutePath(),
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private static String shortMsg(Throwable e) {
        String m = e.getMessage();
        if (m == null) m = e.getClass().getSimpleName();
        return m.length() > 70 ? m.substring(0, 70) : m;
    }

    // ============ 前台自动检查：有更新才弹，弹窗不给点空白关 ============

    private static final String PREF = "app_prefs";
    private static long lastSilentAt = 0;

    private static int ignoredCode(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt("ignored_update_code", 0);
    }

    private static void setIgnoredCode(Context c, int code) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
                .putInt("ignored_update_code", code).apply();
    }

    /** 静默检查：没更新什么都不弹；有更新弹一个关不掉的弹窗，只能选「更新」或「不更新」 */
    public static void checkSilent(final Context c) {
        long now = System.currentTimeMillis();
        if (now - lastSilentAt < 60 * 1000L) return;
        lastSilentAt = now;

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Pkg local = bestLocal(c);
                if (local != null && local.code > ignoredCode(c)) {
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            askLocal(c, local);
                        }
                    });
                    return;
                }
                try {
                    JSONObject o = fetch(VERSION_URL);
                    final int remoteCode = o.optInt("versionCode", 0);
                    final String remoteName = o.optString("versionName", "");
                    final String url = o.optString("url", "");
                    final String note = o.optString("note", "");
                    final int installed;
                    try {
                        installed = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode;
                    } catch (Throwable e) {
                        return;
                    }
                    if (remoteCode <= installed) return;
                    if (remoteCode <= ignoredCode(c)) return;
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            askRemote(c, remoteCode, remoteName, note, url);
                        }
                    });
                } catch (Throwable ignored) {
                    // 网络失败就安静退出
                }
            }
        }).start();
    }

    private static void askRemote(final Context c, final int code, final String name,
                                  final String note, final String url) {
        String msg = "新版本：" + name + (note.isEmpty() ? "" : ("\n" + note));
        AlertDialog d = new Loc(c)
                .setTitle("发现新版本")
                .setMessage(msg)
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int w) {
                        download(c, name, url);
                    }
                })
                .setNegativeButton("不更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int w) {
                        setIgnoredCode(c, code);
                    }
                })
                .create();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        d.show();
    }

    private static void askLocal(final Context c, final Pkg pkg) {
        AlertDialog d = new Loc(c)
                .setTitle("有下载好的新版本")
                .setMessage("已经下好 v" + pkg.name + "，还没装。现在装吗？")
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int w) {
                        install(c, pkg.file);
                    }
                })
                .setNegativeButton("不更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int w) {
                        setIgnoredCode(c, pkg.code);
                    }
                })
                .create();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        d.show();
    }
}
