# 小红书克隆应用 - 后端API

基于 Node.js + Express + MySQL 构建的 RESTful API 服务。

## 功能特性

- ✅ 用户认证（注册/登录/JWT）
- ✅ 笔记CRUD操作
- ✅ 多图片上传
- ✅ 点赞/收藏功能
- ✅ 评论系统
- ✅ 搜索功能
- ✅ 分页查询
- ✅ 图片托管

## 技术栈

- **Node.js** - 运行环境
- **Express** - Web框架
- **MySQL** - 数据库
- **JWT** - 身份认证
- **Multer** - 文件上传
- **bcryptjs** - 密码加密

## 快速开始

### 1. 安装依赖

```bash
cd backend
npm install
```

### 2. 配置环境变量

复制 `.env.example` 为 `.env` 并修改配置：

```bash
cp .env.example .env
```

修改数据库配置：
```env
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=redbook_db
```

### 3. 初始化数据库

确保 MySQL 服务已启动，然后运行：

```bash
npm run init-db
```

这将创建所有必需的数据库表。

### 4. 启动服务

开发模式（自动重启）：
```bash
npm run dev
```

生产模式：
```bash
npm start
```

服务将在 http://localhost:3000 启动

## API文档

### 认证接口

#### 注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

#### 登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

响应：
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "uuid",
      "username": "testuser",
      "email": "test@example.com",
      "avatar": "url",
      "bio": "",
      ...
    }
  }
}
```

#### 获取当前用户信息
```http
GET /api/auth/me
Authorization: Bearer <token>
```

#### 更新个人资料
```http
PUT /api/auth/profile
Authorization: Bearer <token>
Content-Type: multipart/form-data

{
  "username": "newname",
  "bio": "新的个人简介",
  "avatar": <file>
}
```

### 笔记接口

#### 获取笔记列表
```http
GET /api/notes?page=1&limit=20
Authorization: Bearer <token>
```

#### 获取笔记详情
```http
GET /api/notes/:id
Authorization: Bearer <token>
```

#### 发布笔记
```http
POST /api/notes
Authorization: Bearer <token>
Content-Type: multipart/form-data

{
  "title": "标题",
  "content": "内容",
  "location": "地点",
  "topics": ["话题1", "话题2"],
  "images": [<file1>, <file2>, ...]
}
```

#### 删除笔记
```http
DELETE /api/notes/:id
Authorization: Bearer <token>
```

#### 点赞/取消点赞
```http
POST /api/notes/:id/like
Authorization: Bearer <token>
```

#### 收藏/取消收藏
```http
POST /api/notes/:id/collect
Authorization: Bearer <token>
```

#### 获取用户的笔记
```http
GET /api/notes/user/:userId
Authorization: Bearer <token>
```

#### 搜索笔记
```http
GET /api/notes/search?q=关键词
Authorization: Bearer <token>
```

### 评论接口

#### 获取笔记的评论列表
```http
GET /api/comments/note/:noteId
Authorization: Bearer <token>
```

#### 发表评论
```http
POST /api/comments/note/:noteId
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "评论内容"
}
```

#### 删除评论
```http
DELETE /api/comments/:id
Authorization: Bearer <token>
```

#### 点赞/取消点赞评论
```http
POST /api/comments/:id/like
Authorization: Bearer <token>
```

## 数据库设计

### 用户表 (users)
- id, username, email, password, avatar, bio
- followers_count, following_count, notes_count
- created_at, updated_at

### 笔记表 (notes)
- id, user_id, title, content, cover_image, location
- likes_count, comments_count, collects_count, views_count
- created_at, updated_at

### 笔记图片表 (note_images)
- id, note_id, image_url, sort_order

### 笔记话题表 (note_topics)
- id, note_id, topic

### 评论表 (comments)
- id, note_id, user_id, content, likes_count
- created_at, updated_at

### 用户点赞表 (user_likes)
- id, user_id, note_id

### 用户收藏表 (user_collects)
- id, user_id, note_id

### 评论点赞表 (comment_likes)
- id, user_id, comment_id

### 关注表 (follows)
- id, follower_id, following_id

## 部署

### Docker部署

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: redbook_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  api:
    build: .
    ports:
      - "3000:3000"
    environment:
      DB_HOST: mysql
      DB_USER: root
      DB_PASSWORD: root
      DB_NAME: redbook_db
    depends_on:
      - mysql

volumes:
  mysql_data:
```

运行：
```bash
docker-compose up -d
```

### 云服务器部署

1. 安装 Node.js 和 MySQL
2. 克隆代码到服务器
3. 安装依赖：`npm install --production`
4. 配置环境变量
5. 初始化数据库：`npm run init-db`
6. 使用 PM2 启动：`pm2 start src/server.js --name redbook-api`

## 测试

使用 Postman 或 curl 测试API：

```bash
# 健康检查
curl http://localhost:3000/health

# 注册
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"123456"}'
```

## 许可证

MIT License
