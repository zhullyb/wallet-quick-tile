# Wallet Quick Tile

一个很小的 Android 原生应用：在系统下拉快捷设置中提供一个 **Wallet**
磁贴，点击后直接进入 Android 原生的“默认 Wallet 应用”选择页面。

> A tiny Quick Settings tile for opening Android's default Wallet app picker.

## 为什么会有这个项目

这个项目来自海外留学和生活中的一个真实痛点。

国内外的 NFC 支付生态并不统一，同一台手机上可能装着好几个需要轮流成为
“默认 NFC / Wallet 应用”的服务。例如：

- 使用手机默认 NFC 钱包刷银联卡；
- 使用中国银行、工商银行等银行 App 提供的 HCE 云闪付卡；
- 在海外使用 Google Wallet 刷当地银行发行的借记卡；
- 遇到合适汇率或活动时，切换到微信 NFC 支付以享受相应优惠。

在不同国家、商户、银行卡和活动之间切换时，默认 Wallet 可能需要频繁更改。
Android 系统及部分厂商 ROM 的设置入口层级较深，而且某些 OEM 页面显示的候选应用
并不完整。
每次都在设置里寻找入口很麻烦，于是就 vibe 了这个小工具：下拉快捷设置，点一下
磁贴，直接进入 Android 原生 Wallet role 选择器。

以上只是个人使用场景举例。银行卡、HCE、Google Wallet、微信 NFC 和汇率优惠的
实际可用性取决于设备、地区、银行、商户和服务提供方，本项目不参与或处理任何支付。

## 功能

- 提供一个可添加到 Quick Settings 的 **Wallet** 磁贴；
- 点击磁贴后收起快捷设置并打开原生默认 Wallet 选择页面；
- Android 13 及以上可在 App 内请求系统添加磁贴；
- 磁贴只是快捷入口，不维护 ON/OFF 状态；
- 不读取卡片信息，不发起支付，也不修改默认 Wallet；
- 不申请 NFC、支付、Wallet、网络或存储权限；
- 不使用额外的第三方运行时框架。

## 安装与使用

### 从 GitHub Releases 安装

打开本仓库的 **Releases** 页面，在最新版本中下载：

```text
wallet-quick-tile-<tag>-debug.apk
```

不要下载 GitHub 自动生成的 `Source code` 压缩包。Release 同时提供对应的
`.apk.sha256` 文件，可在电脑上校验：

```bash
sha256sum -c wallet-quick-tile-<tag>-debug.apk.sha256
```

下载 APK 后直接在手机上打开安装即可。

### 添加磁贴

安装 APK 后：

1. 打开 **Wallet Quick Tile**；
2. Android 13 及以上点击“添加 Wallet 磁贴”，然后确认系统提示；
3. 也可以完全展开快捷设置，点击“编辑”，手动把 **Wallet** 拖入启用区域；
4. 点击磁贴即可打开默认 Wallet 应用选择页面。

## 编译

环境要求：

- JDK 17；
- Android SDK Platform 35；
- Android Studio Ladybug 或更新版本，或直接使用 Gradle Wrapper。

```bash
./gradlew assembleDebug
```

生成文件：

```text
app/build/outputs/apk/debug/app-debug.apk
```

运行静态检查：

```bash
./gradlew lintDebug
```

## 实现原理

目标页面是 Android Permission Controller 中的：

```text
Package:
com.android.permissioncontroller

Activity:
com.android.permissioncontroller.role.ui.DefaultAppActivity

String Extra:
android.intent.extra.ROLE_NAME = android.app.role.WALLET
```

下面的 ADB 命令可以直接启动该页面：

```bash
adb shell am start \
  -n com.android.permissioncontroller/.role.ui.DefaultAppActivity \
  --es android.intent.extra.ROLE_NAME android.app.role.WALLET
```

但普通第三方 App 与 ADB shell 的权限不同。Android 15 上的
`DefaultAppActivity` 受普通第三方应用无法获得的 `MANAGE_ROLE_HOLDERS` 系统权限
保护，不能简单照搬 ADB 命令直接启动。

因此，本项目优先启动 Permission Controller 导出的系统中转 Activity：

```text
com.android.permissioncontroller.role.ui.v35.ChangeDefaultCardEmulationActivity
```

它会在 Permission Controller 自己的进程内创建带有 `ROLE_WALLET` 的 Intent，
再进入 `DefaultAppActivity`。最终显示的仍然是原生 Wallet role 选择器，而不是
厂商自己的 NFC 支付设置页面。

为了避免再次进入候选列表为空的 OEM 页面，本项目不会 fallback 到：

```text
android.settings.NFC_PAYMENT_SETTINGS
```

## Android 版本兼容性

- `TileService` 最低需要 Android 7.0 / API 24，因此项目 `minSdk` 为 24；
- Android 14 / API 34 及以上使用不可变 Activity `PendingIntent` 调用
  `startActivityAndCollapse(PendingIntent)`；
- API 24–33 使用旧版 `startActivityAndCollapse(Intent)`；
- App 内请求添加磁贴使用 Android 13 / API 33 引入的
  `StatusBarManager.requestAddTileService()`；
- 原生 Wallet 页面和中转 Activity 属于系统内部实现，OEM 或 Android 系统更新
  可能改变组件名、权限或行为；
- 当前方案仅在小米手机上经过实际测试；其他品牌设备尚未验证，不同 Android 版本、
  厂商 ROM，以及其他小米 / HyperOS / MIUI 版本也可能存在差异。

## 权限与隐私

Manifest 中没有声明任何 `uses-permission`。

`android.permission.BIND_QUICK_SETTINGS_TILE` 只用于保护 `TileService` 的系统绑定，
不是 App 主动申请的运行时权限。应用不会读取 NFC 数据、银行卡、交易信息、位置、
账号或网络内容。

## 项目结构

```text
app/src/main/java/com/example/walletquicktile/
├── MainActivity.kt
├── WalletSettingsIntents.kt
└── WalletTileService.kt
```

项目使用 Kotlin 和 Android 平台 API。除 Kotlin 标准库外，没有引入额外的第三方库。

## 自动发布（维护者）

[`.github/workflows/release.yml`](.github/workflows/release.yml) 会在任意新 tag push 到
GitHub 时执行以下操作：

1. 安装 JDK 17、Android SDK Platform 35 和 Build Tools 35.0.0；
2. 运行 `lintDebug`；
3. 构建 debug APK；
4. 创建对应 tag 的 GitHub Release；
5. 上传重命名后的 APK 和 SHA-256 校验文件。

### 发布新版本

确保待发布的代码已经提交，然后创建并推送 SemVer tag。首个版本为 `0.0.1`：

```bash
git tag v0.0.1
git push origin v0.0.1
```

Workflow 接受 `0.0.1` 或 `v0.0.1` 形式的 tag。APK 的 `versionName` 直接来自 tag
（开头的 `v` 会被移除），`versionCode` 也根据 tag 的 major、minor、patch 生成。
同一 workflow run 重新执行时会覆盖该 Release 中同名的 APK 和校验文件。

## 维护说明

这是一个为解决个人痛点而 **vibe coding** 出来的小工具，不是商业产品，也不是任何
手机厂商、银行、卡组织、Google、微信或支付服务的官方项目。

后续维护没有保障：可能不会及时适配新的 Android 或厂商系统版本，也不保证及时回复
Issue、合并 PR 或发布新版。欢迎提交 Issue 和改进建议，但请不要依赖本项目处理紧急的
支付或出行需求。

使用前请自行确认设备和支付环境，并保留进入系统设置手动切换 Wallet 的备用方式。
