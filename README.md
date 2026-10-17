# Healio Wellbeing App

![Scala](https://img.shields.io/badge/Scala-3.3.4-DC322F?logo=scala&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-25-007396)
![Apache Derby](https://img.shields.io/badge/Apache%20Derby-10.17.1.0-8A2BE2)

<p align="center">
  <img src="src/main/resources/icon/app-icon.png" alt="Healio app icon" width="160">
</p>

## Overview

Healio is a desktop wellbeing tracker built with Scala, JavaFX/ScalaFX, FXML, and Apache Derby. It combines habit tracking, reminders, daily mood check-ins, progress insights, and achievement badges in a local multi-user application.

All application data is stored in a local Derby database created at runtime. The database is intentionally excluded from version control because it can contain usernames, password hashes, and personal wellbeing records.

## Features

- Register local accounts, sign in, and maintain a per-user session.
- Create, edit, delete, and categorize habits.
- Mark habits complete for the current day and view daily progress.
- Create and edit dated reminders with High, Medium, or Low priority.
- Mark reminders as completed and display upcoming items.
- Record a daily Happy, Neutral, Sad, or Angry mood.
- View a dashboard with pending work, streaks, recent badges, mood, and motivation.
- Review seven-day habit trends and weekly reminder status in charts.
- Earn badges for habit completions, complete days, streaks, and reminders.
- Navigate between Dashboard, Insights, Reminders, Habits, and Achievements.

## Tech Stack

- Scala 3.3.4
- sbt
- JavaFX 25 with platform-specific native modules
- ScalaFX 24.0.2-R36
- ScalaFXML Core 0.5
- Apache Derby 10.17.1.0 embedded database
- FXML and CSS for the desktop interface

## Project Structure

```text
src/main/
|-- scala/com/healthtracker/
|   |-- controller/            # JavaFX screen and dialog controllers
|   |-- database/              # Derby setup and DAO classes
|   |-- model/                 # Habit, reminder, badge, and user models
|   |-- service/               # Dashboard and achievement logic
|   |-- traits/                # Shared behavior
|   |-- MainApp.scala          # JavaFX application entry point
|   `-- Session.scala          # Current local user session
`-- resources/
    |-- com/healthtracker/controller/  # FXML views
    |-- icon/                           # Application and mood artwork
    |-- font/                           # Bundled Poppins fonts
    `-- styles/healio-theme.css         # Application theme
```

## Setup and Run

### Requirements

- A JDK compatible with JavaFX 25
- sbt installed and available on `PATH`
- Internet access on the first build so sbt can resolve dependencies

From the repository root:

```bash
sbt run
```

On first launch, Healio creates `healthtrackerDB/` in the working directory and initializes the required tables. Create an account from the registration tab, then sign in to use the application.

To compile without launching the interface:

```bash
sbt compile
```

## Assets

The repository includes an application icon, four mood illustrations, a supporting smile graphic, a custom CSS theme, and bundled Poppins font files. No full application screenshots are currently included.

## Data and Security Notes

- `healthtrackerDB/` and `derby.log` are local runtime artifacts and must not be committed.
- Data remains on the local machine; no remote API or cloud synchronization is implemented.
- Passwords are hashed with plain SHA-256 without a per-user salt. This is suitable only for a learning project, not production authentication.
- The app stores wellbeing records locally without database encryption.

## My Contribution

I contributed to the implementation represented in this repository, including the Scala/JavaFX interface flow, FXML integration, local data-access layer, habit and reminder workflows, dashboard insights, and achievement logic. This statement is limited to the project files shown here and does not claim ownership of Scala, JavaFX, ScalaFX, Apache Derby, bundled fonts, or other third-party components.

---

# Healio Wellbeing App 中文说明

## 项目简介

Healio 是一款使用 Scala、JavaFX/ScalaFX、FXML 与 Apache Derby 构建的桌面健康管理应用。它在本地多用户环境中整合习惯追踪、提醒事项、每日情绪记录、趋势分析与成就徽章。

所有应用数据均保存在运行时创建的本地 Derby 数据库中。该数据库可能包含用户名、密码哈希与个人健康记录，因此已明确排除在版本控制之外。

## 实际功能

- 注册本地账户、登录并维护独立用户会话。
- 新增、编辑、删除及分类习惯。
- 标记当天已完成的习惯并查看每日进度。
- 新增与编辑带日期和高、中、低优先级的提醒。
- 将提醒标记为完成并显示即将到来的项目。
- 记录当天 Happy、Neutral、Sad 或 Angry 情绪。
- 在仪表板查看待办事项、连续天数、最新徽章、情绪与激励语。
- 使用图表查看最近七天的习惯趋势与本周提醒状态。
- 通过习惯完成次数、完整完成日、连续记录与提醒完成获得徽章。
- 在 Dashboard、Insights、Reminders、Habits 与 Achievements 页面之间导航。

## 技术栈

- Scala 3.3.4
- sbt
- JavaFX 25 平台原生模块
- ScalaFX 24.0.2-R36
- ScalaFXML Core 0.5
- Apache Derby 10.17.1.0 嵌入式数据库
- FXML 与 CSS 桌面界面

## 项目结构

```text
src/main/
|-- scala/com/healthtracker/
|   |-- controller/            # 页面与对话框控制器
|   |-- database/              # Derby 初始化与 DAO
|   |-- model/                 # 数据模型
|   |-- service/               # 仪表板与成就逻辑
|   |-- traits/                # 共用行为
|   |-- MainApp.scala          # 应用入口
|   `-- Session.scala          # 当前用户会话
`-- resources/                 # FXML、图标、字体与 CSS
```

## 配置与运行

### 环境要求

- 与 JavaFX 25 兼容的 JDK
- 已安装并可通过 `PATH` 使用的 sbt
- 首次构建时可访问网络以下载依赖

在项目根目录运行：

```bash
sbt run
```

首次启动时，Healio 会在工作目录中创建 `healthtrackerDB/` 并初始化数据表。请先在注册页面创建账户，再登录使用。

仅编译项目：

```bash
sbt compile
```

## 素材

仓库包含应用图标、四个情绪插图、辅助笑脸图、定制 CSS 主题与 Poppins 字体文件，但目前没有完整应用截图。

## 数据与安全说明

- `healthtrackerDB/` 与 `derby.log` 属于本地运行时文件，不应提交。
- 数据仅保存在本机，没有远程 API 或云端同步。
- 密码使用未加盐的 SHA-256 哈希，仅适用于学习项目，不适合生产环境身份验证。
- 本地健康记录数据库未加密。

## 我的贡献

我参与了本仓库所展示的实现，包括 Scala/JavaFX 界面流程、FXML 整合、本地数据访问层、习惯与提醒工作流、仪表板分析及成就逻辑。此说明仅针对仓库内的项目文件，不主张拥有 Scala、JavaFX、ScalaFX、Apache Derby、内置字体或其他第三方组件的权利。
