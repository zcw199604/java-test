# 系统安装部署说明文档

## 1. 文档说明

本文档用于答辩光盘交付时说明“烟草采销存协同管理平台”的安装、部署和启动方法。部署环境采用 Ubuntu 服务器，MySQL 使用 Docker 容器运行，前端使用 Node.js 编译后由 Spring Boot 后端统一托管，后端通过 Java 启动 jar 包提供页面和接口服务。

项目默认访问地址：

```text
http://服务器IP:8080
```

后端健康检查地址：

```text
http://服务器IP:8080/api/health
```

## 2. 项目技术栈

| 类型 | 技术 |
| --- | --- |
| 操作系统 | Ubuntu Server |
| 数据库 | MySQL 8，Docker 部署 |
| 后端 | Java 8，Spring Boot 2.7.18，MyBatis，Apache Shiro |
| 前端 | Vue 3，Vite，Vue Router，Pinia，Element Plus，ECharts |
| 构建工具 | Maven，Node.js，npm |
| 默认端口 | 后端 8080，MySQL 宿主机映射端口 3307 |

## 3. 目录结构

将光盘中的项目复制到服务器，例如放到 `/opt/tobacco-platform`：

```text
/opt/tobacco-platform
├── backend/        Spring Boot 后端项目
├── frontend/       Vue 前端项目
├── docs/           项目说明文档
├── helloagents/    项目知识库
└── README.md       项目基础说明
```

后续命令均以 `/opt/tobacco-platform` 为项目根目录示例。如果实际目录不同，请将命令中的路径替换为实际路径。

## 4. 服务器环境准备

### 4.1 更新系统软件源

```bash
sudo apt update
sudo apt upgrade -y
```

### 4.2 安装 Docker

```bash
sudo apt install -y docker.io curl lsof
sudo systemctl enable docker
sudo systemctl start docker
docker --version
```

如当前用户需要直接执行 `docker` 命令，可加入 `docker` 用户组：

```bash
sudo usermod -aG docker $USER
```

执行后重新登录服务器使用户组生效。也可以继续使用 `sudo docker` 执行后续命令。

### 4.3 安装 Java、Maven、Node.js 和 npm

项目后端按 Java 8 编译，建议安装 JDK 8；如果 Ubuntu 软件源没有 `openjdk-8-jdk`，可安装 JDK 11 运行本项目。

```bash
sudo apt install -y openjdk-8-jdk maven
java -version
mvn -version
```

如果当前 Ubuntu 软件源提示找不到 `openjdk-8-jdk`，可改用：

```bash
sudo apt install -y openjdk-11-jdk maven
java -version
mvn -version
```

前端建议使用 Node.js 20 LTS 或更高的长期支持版本。可使用 NodeSource 安装：

```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
node -v
npm -v
```

## 5. 使用 Docker 部署 MySQL

### 5.1 创建 MySQL 数据目录

```bash
sudo mkdir -p /opt/tobacco-platform-data/mysql
```

通常 MySQL 官方镜像会在首次启动时自动处理数据目录权限。如启动后日志提示目录权限不足，可再执行：

```bash
sudo chown -R 999:999 /opt/tobacco-platform-data/mysql
```

### 5.2 启动 MySQL 8 容器

项目默认后端配置连接 `127.0.0.1:3307/tobacco_platform`，数据库用户名为 `root`，密码为 `root123`。因此 Docker 运行命令如下：

```bash
docker run -d \
  --name tobacco-mysql \
  --restart=always \
  -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=tobacco_platform \
  -e TZ=Asia/Shanghai \
  -v /opt/tobacco-platform-data/mysql:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

如果服务器当前用户没有 Docker 权限，请在命令前加 `sudo`。

### 5.3 检查 MySQL 容器状态

```bash
docker ps
docker logs --tail 100 tobacco-mysql
```

确认数据库可连接：

```bash
docker exec -it tobacco-mysql mysql -uroot -proot123 -e "SHOW DATABASES;"
```

输出中应包含：

```text
tobacco_platform
```

## 6. 数据库初始化说明

后端启动时会自动执行以下 SQL 文件：

```text
backend/src/main/resources/sql/schema.sql
backend/src/main/resources/sql/data.sql
```

其中：

- `schema.sql`：创建系统所需数据表。
- `data.sql`：写入演示账号、角色、权限、商品、供应商、客户、仓库、采购单、销售单和库存数据。

当前配置文件为：

```text
backend/src/main/resources/application.yml
```

默认数据库连接配置如下：

```text
DB_HOST=127.0.0.1
DB_PORT=3307
DB_NAME=tobacco_platform
DB_USERNAME=root
DB_PASSWORD=root123
```

注意：当前项目配置了 `spring.sql.init.mode=always`，每次后端启动都会重新执行初始化 SQL，并重置演示数据。答辩演示时可以保留此设置；如需长期保存正式业务数据，首次初始化成功后，后续启动建议增加参数：

```bash
--spring.sql.init.mode=never
```

## 7. 前端编译

进入前端目录并安装依赖：

```bash
cd /opt/tobacco-platform/frontend
npm ci
```

编译前端：

```bash
npm run build
```

本项目的 Vite 构建配置会将前端编译产物输出到：

```text
backend/src/main/resources/static
```

因此后端 jar 启动后，会同时提供前端页面和后端 API。

## 8. 后端打包

进入后端目录：

```bash
cd /opt/tobacco-platform/backend
```

执行 Maven 打包：

```bash
mvn clean package -DskipTests
```

打包完成后，jar 文件位于：

```text
backend/target/tobacco-platform-backend-0.1.0.jar
```

如需执行后端测试，可运行：

```bash
mvn test
```

## 9. 启动后端 jar

### 9.1 前台启动

适合首次验证：

```bash
cd /opt/tobacco-platform/backend
java -jar target/tobacco-platform-backend-0.1.0.jar
```

如需显式指定数据库连接和 JWT 密钥，可使用：

```bash
java \
  -DDB_HOST=127.0.0.1 \
  -DDB_PORT=3307 \
  -DDB_NAME=tobacco_platform \
  -DDB_USERNAME=root \
  -DDB_PASSWORD=root123 \
  -DJWT_SECRET=TobaccoPlatformSecretKeyForDemo2026TobaccoPlatform \
  -jar target/tobacco-platform-backend-0.1.0.jar
```

### 9.2 后台启动

适合答辩演示或服务器长期运行：

```bash
cd /opt/tobacco-platform/backend
nohup java -jar target/tobacco-platform-backend-0.1.0.jar > /opt/tobacco-platform/backend/app.log 2>&1 &
```

查看日志：

```bash
tail -f /opt/tobacco-platform/backend/app.log
```

停止服务：

```bash
ps -ef | grep tobacco-platform-backend
kill 进程号
```

### 9.3 systemd 服务方式启动

创建服务文件：

```bash
sudo nano /etc/systemd/system/tobacco-platform.service
```

写入以下内容：

```ini
[Unit]
Description=Tobacco Platform Backend
After=network.target docker.service

[Service]
Type=simple
WorkingDirectory=/opt/tobacco-platform/backend
Environment=DB_HOST=127.0.0.1
Environment=DB_PORT=3307
Environment=DB_NAME=tobacco_platform
Environment=DB_USERNAME=root
Environment=DB_PASSWORD=root123
Environment=JWT_SECRET=TobaccoPlatformSecretKeyForDemo2026TobaccoPlatform
ExecStart=/usr/bin/java -jar /opt/tobacco-platform/backend/target/tobacco-platform-backend-0.1.0.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

加载并启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable tobacco-platform
sudo systemctl start tobacco-platform
sudo systemctl status tobacco-platform
```

查看服务日志：

```bash
journalctl -u tobacco-platform -f
```

## 10. 访问系统

浏览器访问：

```text
http://服务器IP:8080
```

如果在服务器本机访问：

```text
http://localhost:8080
```

默认演示账号：

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 超级管理员 | admin | 123456 |
| 普通管理员 | manager | 123456 |
| 采购专员 | buyer | 123456 |
| 销售专员 | seller | 123456 |
| 库管人员 | keeper | 123456 |

## 11. 部署验证

### 11.1 检查后端健康接口

```bash
curl http://127.0.0.1:8080/api/health
```

正常情况下返回内容中应包含：

```text
"status":"UP"
```

### 11.2 检查页面访问

打开浏览器访问：

```text
http://服务器IP:8080/login
```

页面应显示“烟草采销存协同管理平台”登录页，并显示验证码。

### 11.3 检查数据库连接

登录系统后如能正常进入驾驶舱，并看到采购、销售、库存等演示数据，说明后端已成功连接 MySQL。

## 12. 常见问题

### 12.1 8080 端口被占用

查看占用进程：

```bash
sudo lsof -i:8080
```

可停止占用进程，或启动后端时修改端口：

```bash
java -jar target/tobacco-platform-backend-0.1.0.jar --server.port=8081
```

### 12.2 3307 端口被占用

查看端口：

```bash
sudo lsof -i:3307
```

如需修改 MySQL 映射端口，需要同时修改后端启动参数 `DB_PORT`。

### 12.3 npm install 或 npm ci 失败

可尝试清理缓存后重新安装：

```bash
cd /opt/tobacco-platform/frontend
npm cache clean --force
npm ci
```

如果网络较慢，可配置国内镜像源：

```bash
npm config set registry https://registry.npmmirror.com
npm ci
```

### 12.4 后端启动提示数据库连接失败

按顺序检查：

```bash
docker ps
docker logs --tail 100 tobacco-mysql
docker exec -it tobacco-mysql mysql -uroot -proot123 -e "SELECT 1;"
```

确认以下配置一致：

```text
MySQL 容器端口映射：3307:3306
后端 DB_PORT：3307
后端 DB_NAME：tobacco_platform
后端 DB_USERNAME：root
后端 DB_PASSWORD：root123
```

### 12.5 登录后菜单显示不完整

系统菜单会根据当前账号权限动态显示。请确认当前登录账号角色是否具备对应权限；如需查看全部菜单，可使用超级管理员账号 `admin / 123456`。

## 13. 光盘交付建议

刻录光盘时建议包含：

```text
tobacco-platform/
├── backend/
├── frontend/
├── docs/
│   ├── system-install-deployment.md
│   └── user-manual.md
├── helloagents/
└── README.md
```

答辩演示时推荐流程：

1. 启动 MySQL Docker 容器。
2. 执行前端 `npm run build`。
3. 执行后端 `mvn clean package -DskipTests`。
4. 启动 `tobacco-platform-backend-0.1.0.jar`。
5. 浏览器访问 `http://服务器IP:8080`。
6. 使用 `admin / 123456` 登录并演示各业务模块。
