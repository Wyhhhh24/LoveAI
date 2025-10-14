# AI恋爱助手 - AI Love Assistant

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=timeGradient&height=300&&section=header&text=AI恋爱助手&fontSize=90&fontAlign=50&fontAlignY=30&desc=AI Love Assistant&descAlign=50&descSize=30&descAlignY=60&animation=twinkling" />
</p>

## 项目简介

**AI恋爱助手** 是一款基于Spring AI和阿里云百炼大模型的智能恋爱咨询平台，专为大学生群体设计，提供专业的恋爱心理指导和情感支持。无论你是单身、恋爱中还是已婚，AI恋爱助手都能为你提供贴心的情感建议。

## 主要功能

- **智能对话系统**  
  - 多轮对话记忆：基于Spring AI的对话记忆功能，提供连贯的咨询体验。
  - RAG知识库问答：集成恋爱知识库，提供专业的恋爱建议和情感指导。
  - 情绪识别：智能分析用户情绪状态，提供针对性的情感支持。

- **互动游戏功能**  
  - 心跳挽回战：模拟恋爱中的矛盾场景，帮助用户学习沟通技巧。
  - 情绪检测：识别生气、失望、伤心等情绪状态。
  - 原谅值系统：通过游戏化的方式提升恋爱沟通能力。

- **智能文档生成**  
  - PDF报告生成：基于对话内容自动生成个性化恋爱成长报告。
  - 结构化输出：使用Spring AI的结构化输出功能，生成标准化的建议文档。
  - 中文字体支持：完美支持中文PDF生成。

- **丰富工具集成**  
  - 文件操作工具：支持文件上传、下载和管理。
  - 网页搜索工具：实时获取最新信息。
  - PDF生成工具：自动生成恋爱指导文档。
  - MCP服务集成：支持高德地图等第三方服务。

## 技术栈
<img align="center" src="https://skillicons.dev/icons?i=java,spring,mysql,postgresql,redis&theme=light" />
本项目主要使用 Java 开发，结合现代技术栈，确保性能与扩展性。

**后端技术栈：**
- Spring Boot 3.4.4 - 主框架
- Spring AI - AI对话框架
- 阿里云百炼大模型 - 核心AI能力
- MyBatis Plus - 数据持久层
- PostgreSQL + PgVector - 向量数据库
- Redis - 缓存和会话管理
- MinIO - 文件存储

## 安装与使用

1. **环境要求**
   - Java 21+
   - MySQL 8.0+
   - PostgreSQL 12+ (支持pgvector扩展)
   - Redis 6.0+
   - Maven 3.6+

2. **克隆项目**：`git clone https://github.com/your-username/ai-agent.git`
3. **进入项目目录**：`cd ai-agent`
4. **配置数据库**：
   ```sql
   -- 创建MySQL数据库
   CREATE DATABASE loveai;
   
   -- 创建PostgreSQL数据库并安装pgvector扩展
   CREATE DATABASE mydatabase;
   \c mydatabase;
   CREATE EXTENSION vector;
   ```
5. **配置应用**：修改 `application.yml` 中的数据库连接信息
6. **启动应用**：`mvn spring-boot:run`

## 核心功能

### 智能恋爱咨询
- 基于专业恋爱心理学知识库
- 支持单身、恋爱中、已婚等不同状态
- 提供个性化的情感建议

### 互动游戏体验
- 心跳挽回战：学习恋爱沟通技巧
- 情绪识别训练：提升情感理解能力
- 游戏化学习：让恋爱教育更有趣

### 文档生成服务
- 自动生成恋爱成长报告
- 个性化建议文档
- 支持PDF格式导出

### 用户管理系统
- 用户注册和登录
- 文件上传和管理
- 会话状态管理

## API接口

**主要接口：**
- `POST /api/love/chat/rag` - RAG知识库对话
- `POST /api/love/game/chat` - 游戏对话
- `POST /api/love/game/emo` - 情绪检测
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录

**请求示例：**
```json
{
  "message": "我和女朋友最近总是吵架，不知道该怎么办",
  "chatId": "user123"
}
```

## 知识库内容

项目内置了丰富的恋爱知识库，包括：
- **单身篇**：如何提升魅力、社交技巧、线上交友等
- **恋爱篇**：沟通技巧、矛盾处理、关系维护等  
- **已婚篇**：家庭责任、亲属关系、长期规划等

## 适用场景

- **大学生恋爱指导**：提供专业的恋爱心理支持
- **情感教育平台**：通过游戏化方式学习恋爱技巧
- **心理咨询工具**：为恋爱中的困惑提供专业建议
- **AI应用开发**：学习Spring AI框架的实际应用

## 贡献

欢迎提交 issue 或 pull request！让我们一起打造更强大的 AI恋爱助手。

## 联系我们

如有疑问或建议，请通过 GitHub Issues 与我们联系。

---

**AI恋爱助手 - 让AI成为你恋爱路上的贴心助手！**
