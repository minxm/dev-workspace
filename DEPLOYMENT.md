# 部署指南

完整的小红书克隆应用部署指南，包括后端API和Android应用。

## 目录
- [环境要求](#环境要求)
- [后端部署](#后端部署)
- [Android应用部署](#android应用部署)
- [生产环境配置](#生产环境配置)
- [常见问题](#常见问题)

---

## 环境要求

### 后端环境
- Node.js 16.x 或更高版本
- MySQL 8.0 或更高版本
- 2GB RAM 最低
- 10GB 磁盘空间

### Android开发环境
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

---

## 后端部署

### 方式一：云服务器部署（推荐）

#### 1. 准备服务器

推荐使用以下云服务商：
- 阿里云 ECS
- 腾讯云 CVM
- AWS EC2
- 华为云 ECS

最低配置：
- 2核 CPU
- 2GB 内存
- 20GB 磁盘
- Ubuntu 20.04 或 CentOS 7+

#### 2. 安装依赖

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Node.js 18.x
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# 安装 MySQL
sudo apt install -y mysql-server

# 安装 Nginx (可选，用于反向代理)
sudo apt install -y nginx

# 安装 PM2 (进程管理器)
sudo npm install -g pm2
```

#### 3. 配置 MySQL

```bash
# 启动 MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation

# 登录 MySQL
sudo mysql -u root -p

# 创建数据库和用户
CREATE DATABASE redbook_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'redbook'@'localhost' IDENTIFIED BY 'your_strong_password';
GRANT ALL PRIVILEGES ON redbook_db.* TO 'redbook'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 4. 部署后端代码

```bash
# 创建应用目录
sudo mkdir -p /var/www/redbook-api
cd /var/www/redbook-api

# 克隆代码或上传代码
# 方式1: 使用 git
git clone <your-repo-url> .

# 方式2: 使用 rsync 从本地上传
# rsync -avz /local/path/backend/ user@server:/var/www/redbook-api/

# 安装依赖
cd backend
npm install --production

# 配置环境变量
cp .env.example .env
nano .env
```

编辑 `.env` 文件：
```env
PORT=3000
NODE_ENV=production

DB_HOST=localhost
DB_PORT=3306
DB_USER=redbook
DB_PASSWORD=your_strong_password
DB_NAME=redbook_db

JWT_SECRET=your_super_secret_jwt_key_change_this
JWT_EXPIRES_IN=7d

UPLOAD_DIR=uploads
MAX_FILE_SIZE=5242880

BASE_URL=https://your-domain.com
```

#### 5. 初始化数据库

```bash
npm run init-db
```

#### 6. 使用 PM2 启动服务

```bash
# 启动应用
pm2 start src/server.js --name redbook-api

# 设置开机自启
pm2 startup
pm2 save

# 查看状态
pm2 status

# 查看日志
pm2 logs redbook-api
```

#### 7. 配置 Nginx 反向代理

```bash
sudo nano /etc/nginx/sites-available/redbook-api
```

添加配置：
```nginx
server {
    listen 80;
    server_name your-domain.com;

    client_max_body_size 10M;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_cache_bypass $http_upgrade;
    }

    location /uploads {
        alias /var/www/redbook-api/backend/uploads;
        expires 1y;
        access_log off;
    }
}
```

启用配置：
```bash
sudo ln -s /etc/nginx/sites-available/redbook-api /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

#### 8. 配置 HTTPS (使用 Let's Encrypt)

```bash
# 安装 Certbot
sudo apt install -y certbot python3-certbot-nginx

# 获取 SSL 证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

### 方式二：Docker 部署

#### 1. 创建 Dockerfile

在 `backend` 目录创建 `Dockerfile`：

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm install --production

COPY . .

EXPOSE 3000

CMD ["npm", "start"]
```

#### 2. 创建 docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: redbook-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: redbook_db
      MYSQL_USER: redbook
      MYSQL_PASSWORD: your_password
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"
    networks:
      - redbook-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  api:
    build: ./backend
    container_name: redbook-api
    environment:
      NODE_ENV: production
      PORT: 3000
      DB_HOST: mysql
      DB_PORT: 3306
      DB_USER: redbook
      DB_PASSWORD: your_password
      DB_NAME: redbook_db
      JWT_SECRET: your_jwt_secret
      JWT_EXPIRES_IN: 7d
      BASE_URL: https://your-domain.com
    volumes:
      - ./backend/uploads:/app/uploads
    ports:
      - "3000:3000"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - redbook-network

  nginx:
    image: nginx:alpine
    container_name: redbook-nginx
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./backend/uploads:/usr/share/nginx/html/uploads:ro
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - api
    networks:
      - redbook-network

volumes:
  mysql_data:

networks:
  redbook-network:
    driver: bridge
```

#### 3. 启动服务

```bash
# 构建并启动
docker-compose up -d

# 初始化数据库
docker-compose exec api npm run init-db

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

---

## Android 应用部署

### 1. 配置 API 地址

修改 `/workspace/app/src/main/java/com/redbookclone/app/data/remote/ApiConfig.kt`：

```kotlin
object ApiConfig {
    // 生产环境
    const val BASE_URL = "https://your-api-domain.com/api/"
    
    // 或者使用IP地址
    // const val BASE_URL = "http://your-server-ip:3000/api/"
}
```

### 2. 生成签名密钥

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

### 3. 配置签名

创建 `app/keystore.properties`：

```properties
storePassword=your_keystore_password
keyPassword=your_key_password
keyAlias=my-key-alias
storeFile=../my-release-key.jks
```

修改 `app/build.gradle.kts`：

```kotlin
android {
    ...
    
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 4. 构建 Release 版本

```bash
# 清理项目
./gradlew clean

# 构建 APK
./gradlew assembleRelease

# 构建 AAB (用于 Google Play)
./gradlew bundleRelease

# APK 位置: app/build/outputs/apk/release/app-release.apk
# AAB 位置: app/build/outputs/bundle/release/app-release.aab
```

### 5. 测试 APK

```bash
# 安装到设备
adb install app/build/outputs/apk/release/app-release.apk
```

### 6. 发布到应用商店

#### Google Play
1. 登录 [Google Play Console](https://play.google.com/console)
2. 创建新应用
3. 上传 AAB 文件
4. 填写应用信息
5. 提交审核

#### 其他应用商店
- 华为应用市场
- 小米应用商店
- OPPO 软件商店
- vivo 应用商店
- 应用宝

---

## 生产环境配置

### 安全配置

#### 1. 设置防火墙

```bash
# UFW (Ubuntu)
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

#### 2. 配置 MySQL 安全

```sql
-- 禁止远程 root 登录
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');

-- 删除匿名用户
DELETE FROM mysql.user WHERE User='';

-- 删除测试数据库
DROP DATABASE IF EXISTS test;
DELETE FROM mysql.db WHERE Db='test' OR Db='test\\_%';

FLUSH PRIVILEGES;
```

#### 3. 配置备份

```bash
# 创建备份脚本
sudo nano /usr/local/bin/backup-redbook.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/backups/redbook"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份数据库
mysqldump -u redbook -p'your_password' redbook_db > $BACKUP_DIR/db_$DATE.sql

# 备份上传的文件
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz /var/www/redbook-api/backend/uploads

# 删除30天前的备份
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete
```

```bash
# 设置权限
sudo chmod +x /usr/local/bin/backup-redbook.sh

# 添加定时任务
sudo crontab -e
# 每天凌晨2点执行备份
0 2 * * * /usr/local/bin/backup-redbook.sh
```

### 性能优化

#### 1. MySQL 优化

```bash
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

```ini
[mysqld]
max_connections = 200
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
query_cache_size = 64M
```

#### 2. Node.js 优化

```bash
# PM2 cluster 模式
pm2 start src/server.js -i max --name redbook-api
```

#### 3. Nginx 缓存配置

```nginx
http {
    proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=api_cache:10m max_size=1g inactive=60m;
    
    server {
        location /api/ {
            proxy_cache api_cache;
            proxy_cache_valid 200 10m;
            proxy_cache_use_stale error timeout invalid_header updating;
            add_header X-Cache-Status $upstream_cache_status;
            
            proxy_pass http://localhost:3000;
        }
    }
}
```

---

## 监控和日志

### 1. PM2 监控

```bash
# 实时监控
pm2 monit

# Web界面
pm2 plus
```

### 2. 日志管理

```bash
# 查看日志
pm2 logs redbook-api

# 日志轮转
pm2 install pm2-logrotate
pm2 set pm2-logrotate:max_size 10M
pm2 set pm2-logrotate:retain 7
```

### 3. 系统监控

```bash
# 安装 Netdata
bash <(curl -Ss https://my-netdata.io/kickstart.sh)
```

---

## 常见问题

### Q1: 无法连接到数据库
**A:** 检查 MySQL 是否启动，防火墙是否开放端口，`.env` 配置是否正确。

### Q2: 图片上传失败
**A:** 检查 `uploads` 目录权限，确保应用有写入权限：
```bash
sudo chmod 755 /var/www/redbook-api/backend/uploads
sudo chown -R www-data:www-data /var/www/redbook-api/backend/uploads
```

### Q3: Android 应用无法连接到 API
**A:** 
- 检查 API 地址配置是否正确
- 检查服务器防火墙是否开放端口
- 使用 `http://10.0.2.2:3000` 连接本地模拟器
- 生产环境必须使用 HTTPS

### Q4: JWT token 过期
**A:** 调整 `.env` 中的 `JWT_EXPIRES_IN` 配置，或实现 refresh token 机制。

### Q5: PM2 进程崩溃
**A:** 查看日志定位问题：
```bash
pm2 logs redbook-api --err
pm2 restart redbook-api
```

---

## 联系支持

如遇到部署问题，请提交 Issue 或联系技术支持。

**祝部署顺利！🎉**
