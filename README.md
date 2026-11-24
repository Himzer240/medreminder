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

## 当前版本：v8.25.1（versionCode 124）

- 手机端没网时，检查更新与下载表端都会提示「还没有连接到网络」
- 手表页新增「下载表端软件（取最新版）」，和「安装手表端」并存
- 未连接手表时，手表页只留「连接手表」「同步计划」，其余按钮灰掉不可点
- 主题色 24 个、检查更新带动画提示、疑惑问答整页跟主题色

**手机端：** [medreminder-8.25.1.apk](releases/android/medreminder-8.25.1.apk)

**手表端：** [medwatch-1.20.bin](releases/watch/medwatch-1.20.bin)

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
| v8.25.1 | 没网提示 + 下载表端软件 | [下载](releases/android/medreminder-8.25.1.apk) |
| v8.24.7 | 顶部手表图标固定红色 | [下载](releases/android/medreminder-8.24.7.apk) |
| v8.24.6 | 手表设置页背景跟主题色 | [下载](releases/android/medreminder-8.24.6.apk) |
| v8.24.5 | 手表背景色真正同步 | [下载](releases/android/medreminder-8.24.5.apk) |

## 关于更新

打开 App 就会自动查版本：有新版就弹窗，两条路——要么更新、要么不更新。

## 说明

> **本仓库的内容都由 AI 助手代为发布和维护，不是账号主人本人在线发布。**

仅供个人使用。签名密钥（keystore / private.pem）没有放进仓库，避免被人拿去签假包。
