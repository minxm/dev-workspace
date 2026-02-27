# 快速启动指南

完整运行小红书克隆应用的快速指南。

## 📋 前置要求

### 后端
- Node.js 16+ 已安装
- MySQL 8.0+ 已安装并运行
- 端口 3000 可用

### Android
- Android Studio 已安装
- Android 设备或模拟器
- 与后端在同一网络（或配置正确的API地址）

---

## 🚀 第一步：启动后端服务

### 1. 安装依赖

```bash
cd backend
npm install
```

### 2. 配置数据库

确保 MySQL 正在运行，然后修改 `.env` 文件：

```env
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=你的MySQL密码
DB_NAME=redbook_db
```

如果 `.env` 文件不存在，从示例文件复制：

```bash
cp .env.example .env
```

### 3. 初始化数据库

```bash
npm run init-db
```

你应该看到类似输出：
```
✅ 数据库 redbook_db 已创建或已存在
✅ users 表已创建
✅ notes 表已创建
...
🎉 数据库初始化完成！
```

### 4. 启动服务器

```bash
# 开发模式（推荐）
npm run dev

# 或生产模式
npm start
```

服务器启动成功后，你会看到：
```
╔═══════════════════════════════════════╗
║   🚀 小红书后端服务启动成功！         ║
╠═══════════════════════════════════════╣
║   端口: 3000                        
║   环境: development       
║   地址: http://localhost:3000      
╚═══════════════════════════════════════╝
```

### 5. 测试 API

访问健康检查端点：
```bash
curl http://localhost:3000/health
```

应该返回：
```json
{
  "success": true,
  "message": "服务器运行正常",
  "timestamp": "..."
}
```

---

## 📱 第二步：运行 Android 应用

### 1. 打开项目

在 Android Studio 中打开项目根目录。

### 2. 配置 API 地址

打开文件：`app/src/main/java/com/redbookclone/app/data/remote/ApiConfig.kt`

#### 使用模拟器

```kotlin
object ApiConfig {
    // 模拟器访问宿主机的localhost
    const val BASE_URL = "http://10.0.2.2:3000/api/"
}
```

#### 使用真机

先获取电脑的IP地址：

**Windows:**
```cmd
ipconfig
```

**Mac/Linux:**
```bash
ifconfig
```

然后修改配置：
```kotlin
object ApiConfig {
    // 替换为你的电脑IP
    const val BASE_URL = "http://192.168.1.100:3000/api/"
}
```

⚠️ **注意**：确保手机和电脑在同一WiFi网络下。

### 3. 同步 Gradle

点击 Android Studio 顶部的 "Sync Project with Gradle Files"。

### 4. 运行应用

1. 连接 Android 设备或启动模拟器
2. 点击 Run 按钮（绿色三角形）或按 `Shift + F10`
3. 等待应用安装并启动

---

## 🎯 使用应用

### 注册新用户

1. 打开应用，点击"注册"
2. 输入用户名、邮箱和密码
3. 点击"注册"按钮
4. 注册成功后自动登录

### 浏览笔记

首次使用时首页可能是空的，因为还没有笔记。

### 发布笔记

1. 点击底部导航栏的"+"按钮
2. 选择图片（1-9张）
3. 输入标题和内容
4. 可选：添加地点
5. 点击"发布"

### 互动功能

- ❤️ 点击爱心图标点赞
- ⭐ 点击星星图标收藏
- 💬 点击评论图标查看和发表评论
- 🔍 使用搜索功能查找笔记

---

## 🔧 常见问题

### Q: 应用无法连接到服务器

**解决方案：**

1. 确认后端服务正在运行
   ```bash
   curl http://localhost:3000/health
   ```

2. 检查防火墙是否阻止了端口 3000
   ```bash
   # Windows
   netsh advfirewall firewall add rule name="Node.js" dir=in action=allow protocol=TCP localport=3000
   
   # Mac
   # 系统偏好设置 -> 安全性与隐私 -> 防火墙 -> 防火墙选项 -> 添加应用
   ```

3. 真机测试时，检查电脑和手机是否在同一网络

4. 查看 Android Studio 的 Logcat 输出错误信息

### Q: 注册/登录失败

**解决方案：**

1. 检查数据库是否正确初始化
   ```bash
   cd backend
   npm run init-db
   ```

2. 检查后端日志输出
   ```bash
   # 如果使用 npm run dev，直接查看终端输出
   ```

3. 使用 Postman 测试 API：
   ```bash
   POST http://localhost:3000/api/auth/register
   Content-Type: application/json
   
   {
     "username": "testuser",
     "email": "test@example.com",
     "password": "123456"
   }
   ```

### Q: 图片上传失败

**解决方案：**

1. 确保 `backend/uploads` 目录存在且有写入权限
   ```bash
   mkdir -p backend/uploads
   chmod 755 backend/uploads
   ```

2. 检查 `.env` 中的上传配置：
   ```env
   UPLOAD_DIR=uploads
   MAX_FILE_SIZE=5242880  # 5MB
   ```

3. 在 Android 应用中授予存储权限

### Q: 数据库连接失败

**解决方案：**

1. 确认 MySQL 正在运行
   ```bash
   # Mac
   mysql.server status
   
   # Linux
   sudo systemctl status mysql
   
   # Windows
   # 在服务管理器中检查 MySQL 服务状态
   ```

2. 测试数据库连接
   ```bash
   mysql -u root -p
   # 输入密码后应该能登录
   ```

3. 检查 `.env` 配置是否正确

### Q: 端口已被占用

**解决方案：**

找出占用端口的进程并关闭：

```bash
# Mac/Linux
lsof -i :3000
kill -9 <PID>

# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

或修改后端端口：
```env
# .env
PORT=3001
```

并更新 Android 应用的 API 地址。

---

## 📊 测试数据

为了更好的演示效果，你可以：

1. 注册多个测试账号
2. 发布不同类型的笔记
3. 相互点赞、评论、收藏

### 快速创建测试数据

使用 Postman 或 curl 批量创建：

```bash
# 注册用户1
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"用户1","email":"user1@test.com","password":"123456"}'

# 注册用户2
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"用户2","email":"user2@test.com","password":"123456"}'
```

---

## 🎓 下一步

- 阅读 [后端 API 文档](backend/README.md)
- 查看 [完整部署指南](DEPLOYMENT.md)
- 探索 Android 应用源码
- 自定义功能和样式

---

## 💡 提示

### 开发建议

1. **使用开发者工具**
   - Android Studio Logcat 查看应用日志
   - Chrome DevTools 或 Postman 测试 API
   - MySQL Workbench 查看数据库

2. **代码热重载**
   - 后端使用 `npm run dev` 启用自动重启
   - Android 使用 Compose Preview 实时预览 UI

3. **调试技巧**
   - 在 Android Studio 中设置断点
   - 使用 `console.log` 在后端输出日志
   - 启用 OkHttp 日志查看网络请求

### 性能优化

- 使用 Redis 缓存热门数据
- 启用 Nginx 反向代理
- 优化数据库索引
- 压缩图片大小

---

## 📞 获取帮助

遇到问题？

1. 查看 [常见问题](#常见问题)
2. 检查后端和应用日志
3. 提交 GitHub Issue
4. 查阅项目文档

**祝你使用愉快！🎉**
