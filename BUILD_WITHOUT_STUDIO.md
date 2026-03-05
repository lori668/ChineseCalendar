# 无 Android Studio 编译 APK 指南

## 方法一：使用 GitHub Actions（最简单，推荐）

### 步骤 1：创建 GitHub 账号
访问 https://github.com 注册免费账号

### 步骤 2：创建新仓库
1. 登录后点击右上角 `+` -> `New repository`
2. 仓库名：`ChineseCalendar`
3. 设为 Public 或 Private
4. 点击 `Create repository`

### 步骤 3：推送代码
```bash
cd /home/admin/test/ChineseCalendar

# 初始化 git
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit"

# 关联远程仓库（替换为你的用户名）
git remote add origin https://github.com/YOUR_USERNAME/ChineseCalendar.git

# 推送
git branch -M main
git push -u origin main
```

### 步骤 4：下载编译好的 APK
1. 打开你的 GitHub 仓库页面
2. 点击 `Actions` 标签
3. 等待构建完成（约 5-10 分钟）
4. 点击最近的构建记录
5. 在底部 `Artifacts` 部分下载 `app-debug.apk`

---

## 方法二：使用 GitPod 在线 IDE

### 步骤 1：打开 GitPod
访问 https://gitpod.io 并用 GitHub 账号登录

### 步骤 2：创建工作区
1. 点击 `New Workspace`
2. 输入你的 GitHub 仓库地址
3. 点击 `Open Workspace`

### 步骤 3：编译 APK
在终端执行：
```bash
cd /workspace/ChineseCalendar
chmod +x gradlew
./gradlew assembleDebug
```

编译完成后，APK 位于：
`/workspace/ChineseCalendar/app/build/outputs/apk/debug/app-debug.apk`

可以右键下载或使用命令：
```bash
gp sync-done build
```

---

## 方法三：本地命令行编译（需要下载 SDK）

### 下载必要文件

1. **下载 JDK 17**
   - 下载地址：https://adoptium.net/temurin/releases/
   - 选择 Linux x64 JDK 17

2. **下载 Android 命令行工具**
   - 下载地址：https://developer.android.com/studio#command-tools
   - 选择 `Command line tools only`

3. **安装 SDK**
```bash
# 解压并设置环境变量
mkdir -p ~/android-sdk
mv cmdline-tools ~/android-sdk/
mkdir -p ~/android-sdk/cmdline-tools/latest
mv ~/android-sdk/cmdline-tools/* ~/android-sdk/cmdline-tools/latest/

export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID-sdk/platform-tools

# 接受许可证
yes | sdkmanager --licenses

# 安装必要的 SDK 组件
sdkmanager "platform-tools"
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
```

4. **配置项目 local.properties**
```bash
echo "sdk.dir=$HOME/android-sdk" > /home/admin/test/ChineseCalendar/local.properties
```

5. **编译 APK**
```bash
cd /home/admin/test/ChineseCalendar
chmod +x gradlew
./gradlew assembleDebug
```

---

## 方法四：使用在线 Android 构建器

### Codemagic (https://codemagic.io)
1. 用 GitHub 账号登录
2. 添加你的项目
3. 配置 Android 构建
4. 自动编译并下载 APK

免费额度：每月 500 分钟构建时间

---

## 安装到手机

编译好 APK 后，有多种方式安装：

### 方式 1：USB 传输
1. 将 APK 文件复制到手机
2. 在手机文件管理器中打开
3. 点击安装（需允许未知来源）

### 方式 2：ADB 安装
```bash
adb install app-debug.apk
```

### 方式 3：通过网络传输
1. 使用文件分享应用（如 Send Anywhere）
2. 或通过微信/QQ 发送到手机

---

## 常见问题

### Q: GitHub Actions 构建失败？
A: 检查 `.github/workflows/build.yml` 文件是否正确，查看构建日志

### Q: APK 太大？
A: Release 版本使用了代码压缩，体积更小

### Q: 安装时提示"未知来源"？
A: 在手机设置中允许安装未知来源应用

### Q: 应用闪退？
A: Debug 版本需要签名，建议使用 Release 版本或正确配置签名

---

## 快速开始（最快方式）

**推荐使用 GitHub Actions：**

```bash
# 执行以下命令推送代码到 GitHub
cd /home/admin/test/ChineseCalendar
git init
git add .
git commit -m "Initial commit"
git branch -M main
# 替换为你的 GitHub 用户名
git remote add origin https://github.com/YOUR_USERNAME/ChineseCalendar.git
git push -u origin main
```

然后在 GitHub 页面查看 Actions 标签，等待构建完成即可下载 APK！
