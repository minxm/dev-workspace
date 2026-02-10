# 小红书克隆应用

一个使用 Kotlin 和 Jetpack Compose 构建的现代化 Android 应用，模仿小红书的核心功能。

## 功能特性

### ✨ 核心功能

- **用户认证**
  - 用户注册和登录
  - 用户会话管理
  - 安全的密码处理

- **内容浏览**
  - 瀑布流式首页展示
  - 精美的笔记卡片设计
  - 平滑的滚动体验
  - 图片懒加载

- **笔记详情**
  - 多图片轮播展示
  - 完整的笔记内容展示
  - 用户信息展示
  - 评论列表

- **发布笔记**
  - 多图片选择（最多9张）
  - 添加标题和内容
  - 地点标记
  - 话题标签

- **互动功能**
  - 点赞/取消点赞
  - 收藏/取消收藏
  - 发表评论
  - 评论点赞

- **个人主页**
  - 用户资料展示
  - 粉丝/关注/笔记统计
  - 我的笔记
  - 收藏的笔记
  - 点赞的笔记

- **搜索功能**
  - 全局搜索笔记
  - 热门搜索推荐
  - 搜索结果展示

- **底部导航**
  - 首页
  - 发现
  - 发布
  - 消息
  - 我的

## 技术栈

### 架构
- **MVVM 架构模式**
- **单一 Activity 架构**
- **Repository 模式**

### UI
- **Jetpack Compose** - 现代化的声明式 UI 框架
- **Material Design 3** - 最新的 Material 设计规范
- **Compose Navigation** - 页面导航

### 依赖注入
- **Hilt** - Google 推荐的依赖注入框架

### 数据持久化
- **Room Database** - 本地数据库
- **DataStore** - 用户偏好设置存储

### 网络和图片
- **Retrofit** - 网络请求（预留接口）
- **Coil** - 图片加载库
- **OkHttp** - HTTP 客户端

### 其他库
- **Kotlin Coroutines** - 异步编程
- **Flow** - 响应式数据流
- **Accompanist** - Compose 辅助库（轮播、权限等）

## 项目结构

```
app/
├── data/
│   ├── local/          # 本地数据存储
│   │   ├── AppDatabase.kt
│   │   ├── UserDao.kt
│   │   ├── NoteDao.kt
│   │   ├── CommentDao.kt
│   │   └── UserPreferences.kt
│   ├── model/          # 数据模型
│   │   ├── User.kt
│   │   ├── Note.kt
│   │   └── Comment.kt
│   └── repository/     # 数据仓库
│       ├── AuthRepository.kt
│       ├── NoteRepository.kt
│       └── CommentRepository.kt
├── di/                 # 依赖注入
│   └── AppModule.kt
├── ui/
│   ├── navigation/     # 导航
│   │   └── Screen.kt
│   ├── screens/        # 界面
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── NoteDetailScreen.kt
│   │   ├── PublishScreen.kt
│   │   ├── ProfileScreen.kt
│   │   ├── SearchScreen.kt
│   │   └── MainScreen.kt
│   ├── theme/          # 主题
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/      # ViewModel
│       ├── AuthViewModel.kt
│       ├── NoteViewModel.kt
│       └── CommentViewModel.kt
├── MainActivity.kt
└── RedBookApp.kt
```

## 开始使用

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17 或更高版本
- Android SDK 34
- Gradle 8.2

### 构建和运行

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd RedBookClone
   ```

2. **打开项目**
   - 使用 Android Studio 打开项目
   - 等待 Gradle 同步完成

3. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击 Run 按钮或使用快捷键 `Shift + F10`

### 最低系统要求

- Android 7.0 (API 24) 及以上
- 推荐 Android 11.0 (API 30) 及以上以获得最佳体验

## 功能演示

### 登录注册
- 输入邮箱和密码即可登录
- 新用户可以注册账号
- 自动保存登录状态

### 浏览笔记
- 首页展示瀑布流式笔记列表
- 点击笔记卡片查看详情
- 支持点赞、收藏、评论

### 发布笔记
- 点击底部导航栏的"+"按钮
- 选择图片（支持多张）
- 添加标题和内容
- 可选添加地点信息
- 发布后自动刷新首页

### 个人中心
- 查看个人资料
- 浏览我的笔记
- 查看收藏和点赞的内容
- 退出登录

## 数据说明

应用包含模拟数据用于演示：
- 预置了5条示例笔记
- 图片使用 Picsum Photos API
- 所有数据存储在本地数据库

## 后续优化方向

- [ ] 接入真实的后端 API
- [ ] 添加视频笔记功能
- [ ] 实现关注/粉丝功能
- [ ] 添加私信功能
- [ ] 支持话题和标签
- [ ] 添加消息通知
- [ ] 实现内容推荐算法
- [ ] 支持草稿箱
- [ ] 添加数据同步
- [ ] 性能优化和代码重构

## 许可证

MIT License

## 联系方式

如有问题或建议，欢迎提出 Issue 或 Pull Request。

---

**注意**: 这是一个学习和演示项目，仅用于技术交流。
