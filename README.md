# 吃药提醒 · 完整仓库

<p align="center">
  <b>小米手表配套的吃药提醒 App</b><br>
  手机端管理药品与提醒，手表端到点震动提醒
</p>

<p align="center">
  <a href="https://himzer240.github.io/medreminder/">网页版下载页</a>
  &nbsp;|&nbsp;
  <a href="releases/android/medreminder-8.25.1.apk">下载最新版 v8.25.1</a>
</p>

---

## 当前版本：v8.25.5（versionCode 128）· 手表端 1.21

- 闪退已修：更新弹窗原来用 Application 的上下文，安卓直接 BadToken 崩；现在用当前 Activity，弹不出来也不崩
- 新增日志：闪退自动写 `crash-*.log`，运行时写 `app.log`，设置页「日志」可查看位置并一键分享
- 手表端 1.21：计划可暂停（主页不显示，手表设置里可选「恢复」或「禁用」），手表设置页颜色跟随手机主题
- 更新检查与下载走 jsDelivr 镜像，三个地址自动重试，安装包直接从本仓库取

**手机端：** [medreminder-8.25.5.apk](releases/android/medreminder-8.25.5.apk)

**手表端：** [medwatch-1.21.bin](releases/watch/medwatch-1.21.bin)

## 仓库里有什么

```
app/                手机端源码（Java + 原生 XML，自带 build.sh 一键打包）
  AndroidManifest.xml
  build.sh
  src/ res/ assets/ libs/
watch/              手表端源码（小米 Vela / aiot 工程）
  src/ manifest.json package.json sign/
releases/android/   历史所有手机端 APK（medreminder-<版本>.apk）
releases/watch/     历史所有手表端安装包（medwatch-<版本>.bin）
updates/version.json  唯一数据源：最新版本号、说明、APK/表端地址、历史版本
docs/index.html      网页版下载页，运行时读 version.json
CHANGELOG.md         完整更新日志
```

APK 和表端 bin 都是直接提交进仓库的二进制文件，不再依赖外部网盘链接；`updates/version.json` 里的下载地址指向本仓库 Pages 路径。

## 构建方法

手机端（需要 JDK 17 + Android SDK 34）：

```bash
cd app && bash build.sh
```

手表端：

```bash
cd watch && npm i && npx aiot release --enable-jsc
# 产物 dist/*.rpk，改名 .bin 即可装到手表
```

## 历史版本

完整清单见 [releases/android](releases/android) 与 [releases/watch](releases/watch)，各版本说明见 [CHANGELOG.md](CHANGELOG.md)。最近几个：

| 版本 | 说明 | 下载 |
| --- | --- | --- |
| v8.25.5 | 日志（闪退可导出）+ 修闪退 | [下载](releases/android/medreminder-8.25.5.apk) |
| v8.25.4 | 修闪退（换 Activity 弹窗） | [下载](releases/android/medreminder-8.25.4.apk) |
| v8.25.3 | 手表端 1.21：计划暂停 | [下载](releases/android/medreminder-8.25.3.apk) |
| v8.25.2 | 更新走 jsDelivr 镜像 | [下载](releases/android/medreminder-8.25.2.apk) |
| v8.25.1 | 没网提示 + 下载表端软件 | [下载](releases/android/medreminder-8.25.1.apk) |

## 关于更新

打开 App 就会自动查版本：有新版就弹窗，两条路——要么更新、要么不更新。

版本信息读 `updates/version.json`，依次尝试 jsDelivr / fastly / raw 三个地址；安装包地址也在 json 里，指向本仓库 `releases/`。

## 关于日志

出问题时看 `Android/data/com.doubao.medreminder/files/logs/`：`crash-*.log` 是闪退堆栈，`app.log` 是运行日志。App 里「设置 → 日志」能看到路径、能直接分享出来。

## 说明

> **本仓库的内容都由 AI 助手代为发布和维护，不是账号主人本人在线发布。**

仅供个人使用。签名密钥（keystore / private.pem）没有放进仓库，避免被人拿去签假包。
