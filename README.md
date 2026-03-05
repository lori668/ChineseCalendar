# 安卓万年历应用 - 编译安装说明

## 项目简介

这是一款完全本地运行的安卓万年历应用，包含以下功能：
- **公历 + 农历 + 黄历**：完整日历功能，支持 24 节气、法定节假日
- **生日管理**：记录家人朋友生日，支持提醒
- **任务管理**：个人任务提醒，支持重复任务
- **纯本地运行**：无需联网、无需登录、无广告

## 系统要求

- **Android Studio**: Arctic Fox (2020.3.1) 或更高版本，推荐使用 Giraffe (2022.3.1) 或更高
- **JDK**: 17 或更高
- **Android SDK**:
  - Compile SDK: 34
  - Min SDK: 24 (Android 7.0)
  - Target SDK: 34
- **Gradle**: 8.2.0 或更高

## 编译步骤

### 方法一：使用 Android Studio（推荐）

1. **打开项目**
   - 启动 Android Studio
   - 选择 `File` -> `Open`
   - 选择 `ChineseCalendar` 文件夹

2. **等待 Gradle 同步**
   - 首次打开时，Android Studio 会自动下载依赖并同步项目
   - 如果同步失败，请检查网络连接或手动点击 `File` -> `Sync Project with Gradle Files`

3. **连接设备或启动模拟器**
   - 连接安卓手机（需开启 USB 调试模式）
   - 或启动 Android Studio 自带的模拟器

4. **运行应用**
   - 点击工具栏的绿色运行按钮（▶）
   - 或使用快捷键 `Shift + F10`

5. **生成 APK 文件**
   - 选择 `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`
   - 生成的 APK 文件位于：`app/build/outputs/apk/debug/app-debug.apk`

### 方法二：使用命令行

1. **进入项目目录**
   ```bash
   cd ChineseCalendar
   ```

2. **设置 gradlew 权限**
   ```bash
   chmod +x gradlew
   ```

3. **编译 Debug 版本**
   ```bash
   ./gradlew assembleDebug
   ```

4. **编译 Release 版本（需签名）**
   ```bash
   ./gradlew assembleRelease
   ```

5. **安装到设备**
   ```bash
   ./gradlew installDebug
   ```

## 编译输出

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`

## 安装说明

### 在模拟器上安装
- 直接点击 Android Studio 的运行按钮即可

### 在真机上安装

1. **开启 USB 调试**
   - 进入手机设置 -> 关于手机
   - 连续点击"版本号"7 次，开启开发者选项
   - 返回设置 -> 系统 -> 开发者选项
   - 开启"USB 调试"

2. **连接电脑**
   - 使用 USB 线连接手机和电脑
   - 手机上允许 USB 调试授权

3. **安装 APK**
   - 方法一：在 Android Studio 中直接运行
   - 方法二：使用 adb 命令
     ```bash
     adb install app/build/outputs/apk/debug/app-debug.apk
     ```

4. **手动安装**
   - 将 APK 文件复制到手机
   - 在手机上使用文件管理器打开 APK
   - 允许"未知来源"安装（如果需要）
   - 点击安装

## 权限说明

应用请求以下权限：
- **POST_NOTIFICATIONS**: 发送生日和任务提醒通知
- **SCHEDULE_EXACT_ALARM**: 设置精确的提醒时间
- **RECEIVE_BOOT_COMPLETED**: 开机后自动恢复提醒
- **READ/WRITE_EXTERNAL_STORAGE** (Android 10 以下): 备份/恢复数据

## 常见问题

### Q1: Gradle 同步失败
**解决方案**：
- 检查网络连接
- 在 `File` -> `Settings` -> `Build, Execution, Deployment` -> `Gradle` 中，勾选 `Offline work` 后取消勾选
- 删除 `.gradle` 文件夹后重新同步

### Q2: 构建失败，提示 SDK 版本不匹配
**解决方案**：
- 打开 SDK Manager，安装 Android 14 (API 34)
- 确保 `compileSdk = 34` 和 `targetSdk = 34`

### Q3: 应用无法安装
**解决方案**：
- 确保手机开启 USB 调试
- 检查 APK 文件是否完整
- 尝试卸载旧版本后重新安装

### Q4: 提醒不工作
**解决方案**：
- 在系统设置中授予通知权限
- 允许应用后台运行
- 部分手机需要在电池优化中设置为"不优化"

## 项目结构

```
app/src/main/java/com/calendar/
├── CalendarApplication.kt      # 应用入口
├── data/                       # 数据层
│   ├── AppDatabase.kt          # 数据库
│   ├── dao/                    # 数据访问对象
│   ├── entities/               # 数据实体
│   └── repository/             # 数据仓库
├── domain/                     # 领域层
│   └── calendar/               # 日历引擎
│       ├── LunarCalendar.kt    # 农历计算
│       ├── SolarTerm.kt        # 节气计算
│       ├── AlmanacData.kt      # 黄历数据
│       └── HolidayManager.kt   # 节假日管理
├── ui/                         # UI 层
│   ├── MainActivity.kt         # 主 Activity
│   ├── screens/                # 屏幕
│   ├── theme/                  # 主题
│   └── viewmodels/             # ViewModel
├── service/                    # 服务
│   ├── AlarmScheduler.kt       # 闹钟调度
│   ├── NotificationHelper.kt   # 通知帮助
│   └── ReminderService.kt      # 提醒服务
└── receiver/                   # 广播接收器
    ├── AlarmReceiver.kt        # 闹钟接收器
    └── BootReceiver.kt         # 开机接收器
```

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose + Material 3
- **架构**: MVVM
- **数据库**: Room (SQLite)
- **依赖注入**: 手动注入（简单项目无需使用 Dagger/Hilt）
- **异步**: Kotlin Coroutines + Flow
- **数据序列化**: Gson

## 开发建议

1. **代码规范**: 遵循 Kotlin 代码风格指南
2. **版本控制**: 使用 Git 进行版本管理
3. **测试**: 编写单元测试和 UI 测试
4. **性能**: 避免主线程阻塞，使用协程处理耗时操作

## 许可证

本项目仅供学习和个人使用。

## 联系方式

如有问题，请通过以下方式联系：
- GitHub Issues
- 邮件反馈
