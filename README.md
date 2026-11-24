# 吃药提醒（MedReminder）

> **说明：本仓库的内容（版本包、更新日志、更新配置）都由 AI 助手代为发布和维护，不是账号主人本人在线发布或回复。**
> 账号主人不常上 GitHub，这里只作为 App「检查更新」的取数地址和发版存档，Issue / PR 不保证会有人处理。

小米手表配套的吃药提醒 App，手机端（com.doubao.medreminder）+ 手表端小程序。

## 版本与更新

- 最新版：v8.22.0（versionCode 110）
- 检查更新的元数据在 `updates/version.json`，App 里的「设置 → 检查更新」会读这个文件，比对 versionCode，有新包就下载并跳到系统安装器。
- 字段说明：`versionCode` 整数、`versionName` 字符串、`url` 直接指向 APK 地址、`note` 更新说明。

## 目录

- `updates/version.json` —— 最新版本信息（每次发版都要改这里）
- `CHANGELOG.md` —— 更新日志

发版流程：改完代码 → 打包签名 → 上传 APK → 把 `updates/version.json` 里的版本号和 url 换成新的（由 AI 助手完成）。
