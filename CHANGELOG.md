# 更新日志

## v8.22.0（versionCode 110）
- 设置页新增「检查更新（从 GitHub 下载新版）」按钮
- 读取本仓库 `updates/version.json`，比对 versionCode
- 发现新版：下载 APK 到缓存，再交给系统安装器（新增 ApkProvider + REQUEST_INSTALL_PACKAGES 权限）
- v8.21.1：疑惑问答加「为什么只有小米手表」并配「打开小米运动健康」按钮
