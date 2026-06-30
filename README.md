# giffgaff 助手 (ggkeep)

一个用于管理 giffgaff 等 SIM 卡保号任务的 Android 应用，帮助你追踪 180 天消费规则，避免号码因长期无活动被回收。

## 预览:
<img width="406" height="798" alt="ScreenShot_2026-06-30_085541_245" src="https://github.com/user-attachments/assets/91732039-5151-48d4-9bc4-a9b0f8fc4110" />
<img width="512" height="798" alt="微信图片_20260630084656_18_353" src="https://github.com/user-attachments/assets/29b364a2-b3af-45ac-9334-acd1466dde04" />


## ✨ 功能特性

- **任务管理**：添加/编辑/删除保号任务，记录手机号、最后消费时间、提醒时间等
- **180 天规则追踪**：自动计算剩余天数，状态色直观显示
  - 🟢 正常（>15 天）
  - 🟠 即将到期（8-15 天）
  - 🔴 紧急（1-7 天）
  - ⚪ 已过期（≤0 天）
- **定时提醒**：每任务独立闹钟，到点通知，支持「确认消费」和「发短信」action
- **桌面组件**：Jetpack Glance 实现，半透明卡片，分页显示所有任务
- **常用指令**：内置 giffgaff 常用指令快捷入口
  - 打开官网 (https://www.giffgaff.com/)
  - 查询 SIM 卡号码（短信 NUMBER 到 2020）
  - 查询余额或消费记录（USSD *100#）
  - 关闭语音信箱（USSD ##002#）
- **保号短信**：一键调起系统短信，预填保号内容
- **开机自启**：重启后自动恢复所有闹钟

## 🛠 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose + Material 3
- **架构**：MVVM (ViewModel + StateFlow + Repository)
- **DI**：Hilt
- **数据库**：Room
- **提醒**：AlarmManager + BroadcastReceiver
- **桌面组件**：Jetpack Glance 1.1.0
- **日期**：java.time + Core Library Desugaring
- **最低 SDK**：API 24 (Android 7.0)

## 📦 安装

### 下载apk安装
从 https://github.com/gclinux/giffgaff-keeper/releases/ 中下载apk安装

### 从源码编译

1. 克隆仓库
   ```bash
   git clone https://github.com/<your-username>/ggkeep.git
   cd ggkeep
   ```

2. 使用 Android Studio 打开项目，等待 Gradle 同步完成

3. 连接设备或模拟器，点击 Run

### 命令行编译

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`

## 📱 使用说明

1. **添加任务**：点击右下角 + 号，填写 SIM 卡信息
2. **设置提醒**：选择提醒时间和提前天数（7 或 15 天）
3. **登记消费**：每次消费后点击「登记最后消费日期」重置计时
4. **添加桌面组件**：长按桌面 → 添加插件 → giffgaff助手
5. **常用指令**：在任务详情页点击「常用指令」快速访问 giffgaff 服务

## ⚠️ 注意事项

- 国产 ROM（小米/华为/OPPO 等）可能限制后台闹钟，建议加入「自启动白名单」并关闭电池优化
- Android 13+ 需授予通知权限
- USSD 代码中的 `#` 会被自动编码，无需手动处理
- 所有指令（拨号/短信/网页）均会先弹确认对话框，并调起系统应用由你最终确认
- 小米手机桌面小部件添加过程: 双指在桌面收缩触发桌面菜单->点击小部件->点击全部应用->点击最下方 "安卓小部件"->找到gifgaff助手 进行添加

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE)

## 🤝 贡献

欢迎提 Issue 和 PR！
