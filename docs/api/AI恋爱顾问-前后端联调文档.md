# AI恋爱顾问 - 前后端联调文档

## 📋 项目信息

- **项目名称**: LoveAI - AI恋爱助手
- **后端地址**: `http://localhost:8123`
- **API前缀**: `/api`
- **完整基础URL**: `http://localhost:8123/api`
- **技术栈**: Spring Boot + MongoDB + Redis + Spring AI
- **认证方式**: Session (基于Redis，30天有效期)

---

## 🎯 功能概述

AI恋爱顾问是一个支持多会话管理的智能聊天系统，用户可以创建多个独立的对话会话，每个会话保持独立的聊天历史，类似ChatGPT的交互体验。

---

## 📡 接口清单

### 核心接口列表

| 接口名称 | 接口路径 | 请求方法 | 功能描述 |
|---------|---------|---------|---------|
| 获取会话列表 | `/love/getChatSessionList` | POST | 获取用户的所有会话历史 |
| 获取最新会话历史 | `/love/getLatestChatHistory` | POST | 获取用户最新会话及其聊天记录 |
| 根据会话ID获取消息 | `/love/getChatMessageBySessionId` | POST | 切换会话时获取指定会话的聊天记录 |
| 创建新会话 | `/love/createChatSession` | POST | 创建新的聊天会话 |
| RAG对话 | `/love/chat/rag` | POST | 发送消息并接收AI流式回复 |

---

## 📝 接口详细说明

### 1. 获取会话列表

**接口路径**: `POST /api/love/getChatSessionList`

**功能说明**: 
- 获取当前登录用户的所有会话历史
- 用于在页面左侧渲染会话列表
- 支持滚动展示

**请求参数**:
```json
{
  "chatId": "23034480211"  // 用户ID（登录后自动获取）
}
```

**响应示例**:

**情况1 - 有会话历史**:
```json
{
  "code": 0,
  "data": [
    {
      "id": "1697123456789_23034480211",
      "userId": "23034480211",
      "sessionName": "新对话"
    },
    {
      "id": "1697123456790_23034480211",
      "userId": "23034480211",
      "sessionName": "恋爱咨询"
    },
    {
      "id": "1697123456791_23034480211",
      "userId": "23034480211",
      "sessionName": "分手挽回"
    }
  ],
  "message": "ok"
}
```

**情况2 - 无会话历史（首次使用）**:
```json
{
  "code": 0,
  "data": [],  // 空数组
  "message": "ok"
}
```

**前端处理逻辑**:
```javascript
// 1. 调用接口获取会话列表
const response = await getChatSessionList({ chatId: userId });

// 2. 判断返回的列表是否为空
if (response.data.length === 0) {
  // 情况1：空列表 - 首次使用
  // 自动调用创建会话接口
  const sessionId = await createChatSession();
  // 保存sessionId到状态
  setCurrentSessionId(sessionId);
  // 显示空白聊天界面
  setChatMessages([]);
  
} else {
  // 情况2：有会话历史
  // 渲染左侧会话列表（支持滚动）
  setSessionList(response.data);
  
  // 自动调用获取最新会话历史接口
  const chatHistory = await getLatestChatHistory();
  
  // 设置当前会话ID
  setCurrentSessionId(chatHistory.sessionId);
  
  // 渲染聊天记录
  setChatMessages(chatHistory.chatMessageVOList);
  
  // 高亮左侧对应的会话
  highlightSession(chatHistory.sessionId);
}
```

---

### 2. 获取最新会话历史

**接口路径**: `POST /api/love/getLatestChatHistory`

**功能说明**: 
- 用户进入聊天页面时自动调用
- 获取用户最新的会话及其最近10条聊天记录
- 提供无缝的用户体验

**请求参数**:
```json
{
  "chatId": "23034480211"
}
```

**响应示例**:

**情况1 - 有最新会话**:
```json
{
  "code": 0,
  "data": {
    "sessionId": "1697123456789_23034480211",
    "chatMessageVOList": [
      {
        "id": "msg_001",
        "userId": "23034480211",
        "sessionId": "1697123456789_23034480211",
        "content": "你好！我是你的AI恋爱顾问，有什么可以帮助你的吗？",
        "isAiResponse": true  // true表示AI回复，false表示用户消息
      },
      {
        "id": "msg_002",
        "userId": "23034480211",
        "sessionId": "1697123456789_23034480211",
        "content": "如何追女生？",
        "isAiResponse": false
      },
      {
        "id": "msg_003",
        "userId": "23034480211",
        "sessionId": "1697123456789_23034480211",
        "content": "追女生需要注意以下几点：1. 保持自信...",
        "isAiResponse": true
      }
    ]
  },
  "message": "ok"
}
```

**情况2 - 无最新会话（首次使用）**:
```json
{
  "code": 0,
  "data": {
    "sessionId": null,
    "chatMessageVOList": null
  },
  "message": "ok"
}
```

**前端处理逻辑**:
```javascript
// 调用接口
const response = await getLatestChatHistory({ chatId: userId });

if (response.data.sessionId) {
  // 有最新会话
  setCurrentSessionId(response.data.sessionId);
  setChatMessages(response.data.chatMessageVOList);
  highlightSession(response.data.sessionId);
} else {
  // 无最新会话，显示空白聊天界面
  setChatMessages([]);
}
```

---

### 3. 根据会话ID获取消息

**接口路径**: `POST /api/love/getChatMessageBySessionId`

**功能说明**: 
- 用户点击左侧会话列表中的某个会话时调用
- 实现会话切换功能
- 加载该会话的最近10条聊天记录

**请求参数**:
```json
{
  "chatId": "23034480211",
  "sessionId": "1697123456790_23034480211"  // 要切换到的会话ID
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "sessionId": "1697123456790_23034480211",
    "chatMessageVOList": [
      {
        "id": "msg_101",
        "userId": "23034480211",
        "sessionId": "1697123456790_23034480211",
        "content": "我和女朋友吵架了怎么办？",
        "isAiResponse": false
      },
      {
        "id": "msg_102",
        "userId": "23034480211",
        "sessionId": "1697123456790_23034480211",
        "content": "首先要冷静下来，然后...",
        "isAiResponse": true
      }
    ]
  },
  "message": "ok"
}
```

**错误响应**:
```json
{
  "code": 40000,
  "data": null,
  "message": "当前用户不存在该会话"
}
```

**前端处理逻辑**:
```javascript
// 用户点击左侧会话列表项时触发
async function handleSessionClick(sessionId) {
  try {
    // 1. 取消所有高亮
    unhighlightAllSessions();
    
    // 2. 高亮当前点击的会话
    highlightSession(sessionId);
    
    // 3. 调用接口获取该会话的聊天记录
    const response = await getChatMessageBySessionId({
      chatId: userId,
      sessionId: sessionId
    });
    
    // 4. 更新当前会话ID
    setCurrentSessionId(response.data.sessionId);
    
    // 5. 渲染聊天记录
    setChatMessages(response.data.chatMessageVOList);
    
  } catch (error) {
    console.error('切换会话失败:', error);
    showErrorMessage('会话加载失败，请重试');
  }
}
```

---

### 4. 创建新会话

**接口路径**: `POST /api/love/createChatSession`

**功能说明**: 
- 用户点击"新建对话"按钮时调用
- 自动生成唯一的sessionId
- 在MongoDB中创建新的会话记录

**请求参数**:
```json
{
  "chatId": "23034480211"
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": "1697123456792_23034480211",  // 新生成的sessionId
  "message": "ok"
}
```

**前端处理逻辑**:
```javascript
// 用户点击"新建对话"按钮时触发
async function handleNewChat() {
  try {
    // 1. 调用接口创建新会话
    const response = await createChatSession({ chatId: userId });
    const newSessionId = response.data;
    
    // 2. 保存新的sessionId
    setCurrentSessionId(newSessionId);
    
    // 3. 清空聊天区域
    setChatMessages([]);
    
    // 4. 刷新左侧会话列表
    const sessionList = await getChatSessionList({ chatId: userId });
    setSessionList(sessionList.data);
    
    // 5. 高亮新创建的会话（通常在列表顶部）
    highlightSession(newSessionId);
    
    // 6. 聚焦到输入框
    focusMessageInput();
    
  } catch (error) {
    console.error('创建会话失败:', error);
    showErrorMessage('创建对话失败，请重试');
  }
}
```

---

### 5. RAG对话（流式响应）

**接口路径**: `POST /api/love/chat/rag`

**功能说明**: 
- 发送用户消息并接收AI的流式回复
- 支持Server-Sent Events (SSE)实时推送
- 每条消息必须携带有效的sessionId

**请求头**:
```
Content-Type: application/json
Accept: text/event-stream  // 接收流式响应
```

**请求参数**:
```json
{
  "message": "如何追女生？",
  "chatId": "23034480211",
  "sessionId": "1697123456789_23034480211"  // 必须！否则报错
}
```

**响应格式**: 
- Content-Type: `text/html;charset=UTF-8`
- 流式推送，逐字返回AI回复

**响应示例（流式）**:
```
追
女
生
需
要
注
意
以
下
几
点
：
1
.
保
持
自
信
...
```

**错误响应**:
```json
{
  "code": 40000,
  "data": null,
  "message": "无效的 sessionId"
}
```

**前端处理逻辑（使用Fetch API + SSE）**:
```javascript
// 发送消息
async function handleSendMessage(userMessage) {
  try {
    // 1. 验证sessionId是否存在
    if (!currentSessionId) {
      showErrorMessage('请先创建或选择一个会话');
      return;
    }
    
    // 2. 立即显示用户消息（乐观更新）
    const userMsg = {
      id: generateTempId(),
      content: userMessage,
      isAiResponse: false,
      userId: userId,
      sessionId: currentSessionId
    };
    setChatMessages(prev => [...prev, userMsg]);
    
    // 3. 创建AI消息占位符
    const aiMsg = {
      id: generateTempId(),
      content: '',
      isAiResponse: true,
      userId: userId,
      sessionId: currentSessionId,
      isStreaming: true  // 标记正在接收中
    };
    setChatMessages(prev => [...prev, aiMsg]);
    
    // 4. 调用流式接口
    const response = await fetch('http://localhost:8123/api/love/chat/rag', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      credentials: 'include',  // 携带Cookie
      body: JSON.stringify({
        message: userMessage,
        chatId: userId,
        sessionId: currentSessionId
      })
    });
    
    // 5. 读取流式响应
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      
      const chunk = decoder.decode(value);
      
      // 6. 逐字追加到AI消息
      setChatMessages(prev => {
        const newMessages = [...prev];
        const lastMsg = newMessages[newMessages.length - 1];
        lastMsg.content += chunk;
        return newMessages;
      });
    }
    
    // 7. 标记AI消息完成
    setChatMessages(prev => {
      const newMessages = [...prev];
      const lastMsg = newMessages[newMessages.length - 1];
      lastMsg.isStreaming = false;
      return newMessages;
    });
    
    // 8. 清空输入框
    clearMessageInput();
    
  } catch (error) {
    console.error('发送消息失败:', error);
    showErrorMessage('发送失败，请重试');
    
    // 移除失败的AI消息占位符
    setChatMessages(prev => prev.slice(0, -1));
  }
}
```

---

## 🎨 前端实现详细逻辑

### 一、页面初始化流程

#### 1. 进入聊天页面

```javascript
/**
 * 聊天页面初始化函数
 * 执行时机：从首页点击"AI恋爱顾问"进入聊天页面时
 */
async function initChatPage() {
  try {
    // 显示加载状态
    setLoading(true);
    
    // 步骤1：获取会话列表
    const sessionListResponse = await getChatSessionList({ 
      chatId: userId 
    });
    
    const sessionList = sessionListResponse.data;
    
    // 步骤2：判断会话列表是否为空
    if (sessionList.length === 0) {
      // ========== 情况A：首次使用（空列表） ==========
      
      // 2.1 自动创建新会话
      const createResponse = await createChatSession({ 
        chatId: userId 
      });
      const newSessionId = createResponse.data;
      
      // 2.2 保存sessionId到状态
      setCurrentSessionId(newSessionId);
      
      // 2.3 显示空白聊天界面
      setSessionList([]);
      setChatMessages([]);
      
      // 2.4 显示欢迎消息（可选）
      showWelcomeMessage();
      
    } else {
      // ========== 情况B：有历史会话（非空列表） ==========
      
      // 2.1 渲染左侧会话列表
      setSessionList(sessionList);
      
      // 2.2 获取最新会话的聊天记录
      const chatHistoryResponse = await getLatestChatHistory({ 
        chatId: userId 
      });
      
      const chatHistory = chatHistoryResponse.data;
      
      // 2.3 保存当前会话ID
      setCurrentSessionId(chatHistory.sessionId);
      
      // 2.4 渲染聊天记录
      setChatMessages(chatHistory.chatMessageVOList || []);
      
      // 2.5 高亮左侧对应的会话
      highlightSession(chatHistory.sessionId);
      
      // 2.6 滚动到聊天区域底部
      scrollToBottom();
    }
    
  } catch (error) {
    console.error('页面初始化失败:', error);
    showErrorMessage('加载失败，请刷新页面重试');
  } finally {
    // 隐藏加载状态
    setLoading(false);
  }
}
```

---

### 二、左侧会话列表渲染

#### 1. 会话列表组件结构

```javascript
/**
 * 会话列表渲染函数
 * 特点：支持滚动展示、高亮当前会话
 */
function renderSessionList() {
  return (
    <div className="session-list-container">
      {/* 新建对话按钮 */}
      <div className="new-chat-button" onClick={handleNewChat}>
        <PlusIcon />
        <span>新建对话</span>
      </div>
      
      {/* 会话列表（可滚动） */}
      <div className="session-list-scroll">
        {sessionList.map(session => (
          <div
            key={session.id}
            className={`session-item ${
              session.id === currentSessionId ? 'active' : ''
            }`}
            onClick={() => handleSessionClick(session.id)}
          >
            <ChatIcon />
            <span className="session-name">{session.sessionName}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
```

#### 2. 会话列表样式（CSS）

```css
/* 会话列表容器 */
.session-list-container {
  width: 260px;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  padding: 20px 10px;
}

/* 新建对话按钮 */
.new-chat-button {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-weight: 500;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.new-chat-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

/* 会话列表滚动区域（关键！） */
.session-list-scroll {
  flex: 1;
  overflow-y: auto;  /* 启用垂直滚动 */
  overflow-x: hidden;
  padding-right: 6px;
}

/* 自定义滚动条样式 */
.session-list-scroll::-webkit-scrollbar {
  width: 6px;
}

.session-list-scroll::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.session-list-scroll::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.session-list-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* 会话列表项 */
.session-item {
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
  transition: all 0.3s;
}

.session-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

/* 高亮当前会话（关键样式！） */
.session-item.active {
  background: rgba(255, 255, 255, 0.2);
  border-left: 3px solid #ff6b9d;
  color: white;
  font-weight: 600;
}

.session-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
```

---

### 三、聊天消息渲染

#### 1. 消息列表组件

```javascript
/**
 * 聊天消息列表渲染函数
 * 关键：根据isAiResponse字段决定消息位置
 */
function renderChatMessages() {
  return (
    <div className="chat-messages-container" ref={messagesEndRef}>
      {chatMessages.map((message, index) => (
        <div key={message.id || index}>
          {message.isAiResponse ? (
            // AI消息 - 靠左
            <div className="message-row ai-message">
              <div className="ai-avatar">
                <img src="/ai-avatar.png" alt="AI" />
              </div>
              <div className="message-bubble ai-bubble">
                {message.content}
                {message.isStreaming && <span className="cursor">|</span>}
              </div>
            </div>
          ) : (
            // 用户消息 - 靠右
            <div className="message-row user-message">
              <div className="message-bubble user-bubble">
                {message.content}
              </div>
              <div className="user-avatar">
                <img src="/user-avatar.png" alt="User" />
              </div>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
```

#### 2. 消息样式

```css
/* 聊天消息容器 */
.chat-messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 消息行 */
.message-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  max-width: 80%;
}

/* AI消息 - 靠左 */
.message-row.ai-message {
  align-self: flex-start;
  justify-content: flex-start;
}

/* 用户消息 - 靠右 */
.message-row.user-message {
  align-self: flex-end;
  justify-content: flex-end;
  flex-direction: row-reverse;
}

/* 消息气泡 */
.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
  max-width: 100%;
  word-wrap: break-word;
  animation: slideIn 0.3s ease;
}

/* AI消息气泡 */
.ai-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-left-radius: 4px;
}

/* 用户消息气泡 */
.user-bubble {
  background: #f0f0f0;
  color: #333;
  border-bottom-right-radius: 4px;
}

/* 头像 */
.ai-avatar,
.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.ai-avatar img,
.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 流式响应光标动画 */
.cursor {
  display: inline-block;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

---

### 四、状态管理

```javascript
/**
 * 聊天页面状态管理
 * 建议使用React的useState或Zustand等状态管理库
 */

// 1. 用户ID（登录后获取）
const [userId, setUserId] = useState(null);

// 2. 当前会话ID（非常重要！）
const [currentSessionId, setCurrentSessionId] = useState(null);

// 3. 会话列表
const [sessionList, setSessionList] = useState([]);

// 4. 聊天消息列表
const [chatMessages, setChatMessages] = useState([]);

// 5. 加载状态
const [loading, setLoading] = useState(false);

// 6. 输入框内容
const [messageInput, setMessageInput] = useState('');

// 7. 发送中状态
const [isSending, setIsSending] = useState(false);
```

---

### 五、辅助函数

```javascript
/**
 * 高亮指定会话
 */
function highlightSession(sessionId) {
  // DOM操作或状态更新
  setCurrentSessionId(sessionId);
}

/**
 * 取消所有高亮
 */
function unhighlightAllSessions() {
  // 通过CSS类名控制
  document.querySelectorAll('.session-item').forEach(item => {
    item.classList.remove('active');
  });
}

/**
 * 滚动到聊天区域底部
 */
function scrollToBottom() {
  if (messagesEndRef.current) {
    messagesEndRef.current.scrollIntoView({ 
      behavior: 'smooth' 
    });
  }
}

/**
 * 生成临时ID
 */
function generateTempId() {
  return `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
}

/**
 * 清空输入框
 */
function clearMessageInput() {
  setMessageInput('');
}

/**
 * 聚焦到输入框
 */
function focusMessageInput() {
  document.querySelector('.message-input')?.focus();
}

/**
 * 显示欢迎消息
 */
function showWelcomeMessage() {
  setChatMessages([
    {
      id: 'welcome',
      content: '你好！我是你的AI恋爱顾问，有什么可以帮助你的吗？',
      isAiResponse: true
    }
  ]);
}

/**
 * 显示错误提示
 */
function showErrorMessage(message) {
  // 可使用Toast或Modal组件
  alert(message);
}
```

---

## 🔄 完整交互流程图

```
用户操作                   前端处理                    后端API
─────────────────────────────────────────────────────────────
点击"AI恋爱顾问"
     ↓
进入聊天页面              initChatPage()
     ↓                          ↓
                      getChatSessionList()  →  /api/love/getChatSessionList
     ↓                          ↓
判断返回的List         sessionList.length
     ├─ 空List (首次)           ↓
     │      ↓              createChatSession() →  /api/love/createChatSession
     │      ↓                   ↓
     │  返回sessionId     setCurrentSessionId()
     │      ↓              setChatMessages([])
     │      ↓              showWelcomeMessage()
     │
     └─ 非空List (有历史)       ↓
            ↓              setSessionList()
            ↓              getLatestChatHistory() → /api/love/getLatestChatHistory
            ↓                   ↓
       返回chatHistory     setCurrentSessionId()
            ↓              setChatMessages()
            ↓              highlightSession()
            ↓              scrollToBottom()
─────────────────────────────────────────────────────────────
点击左侧会话
     ↓
切换会话               handleSessionClick()
     ↓                          ↓
                      unhighlightAllSessions()
     ↓                  highlightSession()
                      getChatMessageBySessionId() → /api/love/getChatMessageBySessionId
     ↓                          ↓
                      setChatMessages()
                      scrollToBottom()
─────────────────────────────────────────────────────────────
点击"新建对话"
     ↓
创建新会话             handleNewChat()
     ↓                          ↓
                      createChatSession() →  /api/love/createChatSession
     ↓                          ↓
                      setCurrentSessionId()
                      setChatMessages([])
                      getChatSessionList() →  /api/love/getChatSessionList
                      setSessionList()
                      highlightSession()
─────────────────────────────────────────────────────────────
输入消息并点击发送
     ↓
发送消息               handleSendMessage()
     ↓                          ↓
                      addUserMessage()  (乐观更新)
                      createAiMessagePlaceholder()
                      fetch() + SSE →  /api/love/chat/rag (流式)
     ↓                          ↓
接收AI流式回复         逐字追加到AI消息
     ↓                  appendToLastMessage()
AI回复完成             markMessageComplete()
                      clearMessageInput()
                      scrollToBottom()
```

---

## ⚠️ 注意事项

### 1. 认证机制
- 所有接口都需要登录认证（`@LoginCheck`）
- 使用Cookie携带Session信息
- 前端请求必须设置 `credentials: 'include'`

### 2. SessionId管理
- **非常重要**：发送消息时必须携带有效的sessionId
- 如果sessionId无效，后端会返回错误
- 建议在全局状态中维护currentSessionId

### 3. 流式响应处理
- 使用Fetch API或axios的流式支持
- 需要设置正确的请求头：`Accept: text/event-stream`
- 逐字追加到UI，提升用户体验

### 4. 错误处理
- 所有API调用都应包裹在try-catch中
- 显示友好的错误提示
- 失败时提供重试机制

### 5. 性能优化
- 会话列表支持虚拟滚动（列表很长时）
- 聊天消息支持分页加载（当前只加载10条）
- 使用防抖优化输入事件

### 6. 滚动优化
- 新消息到达时自动滚动到底部
- 使用smooth滚动提升体验
- 用户主动滚动时暂停自动滚动

---

## 🎨 UI/UX建议

### 1. 左侧会话列表
- 固定宽度260px
- 支持垂直滚动
- 当前会话高亮显示
- 悬停效果
- 支持长按删除/重命名（可选）

### 2. 聊天区域
- AI消息靠左，用户消息靠右
- 消息气泡带圆角
- 流式响应时显示光标动画
- 支持Markdown渲染（可选）
- 支持代码高亮（可选）

### 3. 输入区域
- 固定在底部
- 支持多行输入
- Enter发送，Shift+Enter换行
- 发送按钮禁用状态
- 字数统计（可选）

### 4. 加载状态
- 页面初始化时显示骨架屏
- 会话切换时显示加载动画
- AI回复时显示打字动画

