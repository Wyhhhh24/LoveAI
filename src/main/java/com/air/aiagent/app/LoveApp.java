package com.air.aiagent.app;

import cn.hutool.core.lang.UUID;
import com.air.aiagent.advisor.MyLoggerAdvisor;
import com.air.aiagent.constant.SystemConstants;
import com.air.aiagent.domain.dto.ChatRequest;
import com.air.aiagent.domain.entity.ChatMessage;
import com.air.aiagent.domain.entity.MessageMetadata;
import com.air.aiagent.domain.entity.MessageType;
import com.air.aiagent.mapper.repository.ChatMessageRepository;
import com.air.aiagent.mapper.repository.ChatSessionRepository;
import com.air.aiagent.service.impl.AsyncTaskService;
import com.air.aiagent.service.impl.ChatSessionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author WyH524
 * @since 2025/7/24 上午10:48
 */
@Slf4j
@Component
public class LoveApp {

        private final ChatClient chatClient;

        private final ChatClient gameClient;

        private final ChatMemory chatMemory;

        private final ChatMemory gameMemory;

        private final ChatMemory emoMemory;

        private final ChatClient emoClient;

        /**
         * 初始化 AI 客户端 ChatClient
         */
        public LoveApp(ChatModel dashscopeChatModel) {
                // 初始化基于内存的对话记忆，这个 InMemoryChatMemory 是 ChatMemory 接口的一个实现类，SpringAI已经帮我们实现好了
                chatMemory = new InMemoryChatMemory();
                gameMemory = new InMemoryChatMemory();
                emoMemory = new InMemoryChatMemory();

                /**
                 * 初始化 ChatClient
                 */
                chatClient = ChatClient.builder(dashscopeChatModel)
                                .defaultSystem(SystemConstants.CHAT_SYSTEM_PROMPT)
                                .defaultAdvisors(
                                        // 自定义日志拦截器，可按需开启
                                        new MyLoggerAdvisor()
                                        // ,new ReReadingAdvisor()
                                )
                                .build();

                /**
                 * 初始化 GameClient
                 */
                gameClient = ChatClient.builder(dashscopeChatModel)
                                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                                .defaultAdvisors(
                                                new MessageChatMemoryAdvisor(gameMemory),
                                                // 自定义日志拦截器，可按需开启
                                                new MyLoggerAdvisor())
                                .build();

                emoClient = ChatClient.builder(dashscopeChatModel)
                                .defaultSystem(SystemConstants.EMOTION_DETECTION)
                                .defaultAdvisors(
                                                new MessageChatMemoryAdvisor(emoMemory),
                                                // 自定义日志拦截器，可按需开启
                                                new MyLoggerAdvisor())
                                .build();
        }

        /**
         * AI 基础对话（支持多轮对话记忆），这就是SpringAI框架带来的好处，对信息的存取有自己的框架
         */
        public String doChat(String message, String chatId) {
                ChatResponse response = chatClient
                                .prompt()
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                .call()
                                .chatResponse();
                // 这里需要为advisors提供参数，要去指定当前这个关联的历史上下文是哪一个对话的上下文，两个参数
                // spec 就是一个ChatClient.AdvisorSpec对象，这其实就当作一个Map，就是传递给拦截器的参数
                // 1.当前需要取哪个对话的上下文，对应上下文的ID 2.指定你需要获取消息的条数，也就是这个参数可以设置传送给大模型多少条消息

                String content = response.getResult().getOutput().getText(); // 先拿到结果，再拿到它的输出信息，也可以拿到原信息，比如消耗的多少token，这里拿到输出信息中大模型返回的文本
                log.info("content: {}", content);
                return content;
        }

        /**
         * AI 恋爱知识库问答功能
         */
        // 注入基于内存的 VectorStore Bean
        @Resource // 这个注解就优先基于名称注入
        private VectorStore loveAppVectorStore; // 确保和我们自定义的知识库 Bean 名称一致

        @Resource
        private Advisor loveAppRagCloudAdvisor;

        @Resource
        private VectorStore pgVectorVectorStore;

        @Resource
        private ChatMessageRepository chatMessageRepository;

        @Resource
        private ChatSessionRepository chatSessionRepository;

        @Resource
        private ChatSessionService chatSessionService;

        @Resource
        private AsyncTaskService asyncTaskService;

        /**
         * AI 调用工具能力
         */
        @Resource
        private ToolCallback[] allTools;

        // RAG 知识库进行对话
        public Flux<String> doChatWithRagAndTools(ChatRequest request) {
                // 1. 同步保存用户消息
                saveUserMessage(request);

                // 2. 获取历史上下文（排除刚保存的用户消息），查询了 10 条历史记录
                List<ChatMessage> historyMessages = chatMessageRepository
                                .findHistoryExcludingLatest(request.getSessionId(), 10, 1);
                log.info("获取用户，id={}，历史上下文，数量={}", request.getChatId(), historyMessages.size());

                // 如果历史记录集合是空的话，将当前的消息作为这个 session 的 sessionName
                if (historyMessages.isEmpty()) {
                        // 更新会话名称
                        boolean success = chatSessionService.updateSessionName(request.getSessionId(),
                                        request.getChatId(), request.getMessage());
                        if (success) {
                                log.info("会话名称已更新为: {}", request.getMessage());
                        } else {
                                log.warn("会话名称更新失败，sessionId: {}", request.getSessionId());
                        }
                }

                // 3. 基于对话历史以及最新消息，拼接提示词
                String fullPrompt = buildPromptWithContext(
                                historyMessages,
                                request.getMessage(),
                                request.getChatId());
                log.info("用户id={}，完整提示词构建完成，长度={}", request.getChatId(), fullPrompt.length());
                if (log.isDebugEnabled()) {
                        log.info("提示词内容:\n{}", fullPrompt);
                }

                // 4. 创建 StringBuilder 来累积 AI 的流式响应
                StringBuilder aiResponseBuilder = new StringBuilder();
                String aiMessageId = UUID.randomUUID().toString();
                long startTime = System.currentTimeMillis();

                // 5. 流式调用AI并累积响应
                return chatClient.prompt()
                                .user("userId = " + request.getChatId() + "," + fullPrompt)
                                // .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) 改为使用
                                // MongoDB 存储会话历史
                                // .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                // 应用 RAG 知识库问答（基于内存的知识库服务）
                                // .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                                // 应用 RAG 检索增强服务（基于云知识库服务）
                                // .advisors(loveAppRagCloudAdvisor)
                                // 应用 RAG 检索增强服务（基于 PgVector 向量存储）
                                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                                .tools(allTools)
                                .stream()
                                .content() // 流式处理
                                // 关键：doOnNext累积每个chunk
                                .doOnNext(chunk -> {
                                        aiResponseBuilder.append(chunk);
                                })
                                // 关键：doOnComplete 在流式结束后保存
                                .doOnComplete(() -> {
                                        // 计算耗时
                                        long duration = System.currentTimeMillis() - startTime;
                                        // 同步保存完整的 AI 回复
                                        String aiContent = aiResponseBuilder.toString();
                                        // 构建消息实体
                                        ChatMessage aiMessage = ChatMessage.builder()
                                                        .id(aiMessageId)
                                                        .chatId(request.getChatId())
                                                        .sessionId(request.getSessionId())
                                                        .content(aiContent)
                                                        .messageType(MessageType.TEXT)
                                                        .isAiResponse(true)
                                                        .metadata(MessageMetadata.builder()
                                                                        .responseTimeMs((int) duration)
                                                                        .tokenCount(estimateTokens(aiContent))
                                                                        .build())
                                                        .build();
                                        // 保存到 MongoDB
                                        chatMessageRepository.save(aiMessage);
                                        log.info("AI消息已保存，sessionId={}, 长度={}", request.getSessionId(),
                                                        aiContent.length());

                                        // 更新会话的消息计数（+2，用户消息1条 + AI回复1条）
                                        chatSessionService.incrementMessageCount(request.getSessionId());
                                        chatSessionService.incrementMessageCount(request.getSessionId());
                                })
                                // 关键：doOnError处理异常
                                .doOnError(error -> {
                                        log.error("AI流式输出异常，sessionId={}", request.getSessionId(), error);
                                        // 即使出错，也保存已有的部分内容
                                        if (aiResponseBuilder.length() > 0) {
                                                String errorContent = aiResponseBuilder.toString() + "\n[流式输出中断]";
                                                ChatMessage errorMessage = ChatMessage.builder()
                                                                .id(aiMessageId)
                                                                .chatId(request.getChatId())
                                                                .sessionId(request.getSessionId())
                                                                .content(errorContent)
                                                                .messageType(MessageType.TEXT)
                                                                .isAiResponse(true)
                                                                .build();
                                                chatMessageRepository.save(errorMessage);
                                                log.warn("AI错误消息已保存，sessionId={}, 长度={}", request.getSessionId(),
                                                                errorContent.length());

                                                // 更新会话的消息计数（+2，用户消息1条 + AI错误回复1条）
                                                chatSessionService.incrementMessageCount(request.getSessionId());
                                                chatSessionService.incrementMessageCount(request.getSessionId());
                                        }
                                });
        }

        /**
         * 估算Token数量，后面可以从 ai 回复中进行提取
         */
        private int estimateTokens(String content) {
                if (content == null || content.isEmpty()) {
                        return 0;
                }
                return content.length() / 3; // 粗略估算
        }

        /**
         * 保存用户消息到 MongoDB
         */
        private ChatMessage saveUserMessage(ChatRequest request) {
                String userMessageId = UUID.randomUUID().toString();
                ChatMessage userMessage = ChatMessage.builder()
                                .id(userMessageId)
                                .chatId(request.getChatId())
                                .sessionId(request.getSessionId())
                                .messageType(MessageType.TEXT)
                                .content(request.getMessage())
                                .isAiResponse(false)
                                .build();
                // TODO 需要优化
                return chatMessageRepository.save(userMessage);
        }

        /**
         * 构建提示词（带历史上下文）
         */
        private String buildPromptWithContext(
                        List<ChatMessage> historyMessages,
                        String currentQuestion,
                        String userId) {

                StringBuilder prompt = new StringBuilder();

                // 用户标识
                prompt.append("userId = ").append(userId).append("\n\n");

                // 历史对话
                if (!historyMessages.isEmpty()) {
                        prompt.append("===== 历史对话上下文 =====\n");

                        for (ChatMessage msg : historyMessages) {
                                String role = msg.getIsAiResponse() ? "AI助手" : "用户";
                                String timestamp = msg.getTimestamp().format(
                                                DateTimeFormatter.ofPattern("HH:mm:ss"));
                                prompt.append("[").append(timestamp).append("] ")
                                                .append(role).append(": ")
                                                .append(msg.getContent()).append("\n\n");
                        }
                }

                // 当前问题（明确标注）
                prompt.append("===== 当前问题 =====\n");
                prompt.append("用户: ").append(currentQuestion).append("\n\n");

                // 指令
                prompt.append("请根据历史对话上下文，针对性地回答用户的当前问题。");

                return prompt.toString();
        }

        // /**
        // * AI 恋爱大师（支持调用工具）
        // */
        // public String doChatWithTools(String message, String chatId) {
        // ChatResponse chatResponse = chatClient
        // .prompt()
        // .user(message)
        // .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
        // .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
        // .tools(allTools)
        // .call()
        // .chatResponse(); //同步阻塞调用
        // return chatResponse.getResult().getOutput().getText();
        // }

        // //RAG 知识库进行对话
        // public String doChatWithRag(String message, String chatId){
        // ChatResponse chatResponse = chatClient.prompt()
        // .user(message)
        // .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
        // .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
        // //应用 RAG 知识库问答（基于本地知识库服务）
        // .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
        // //应用 RAG 检索增强服务（基于云知识库服务）
        //// .advisors(loveAppRagCloudAdvisor)
        // //应用 RAG 检索增强服务（基于 PgVector 向量存储）
        //// .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
        // .call()
        // .chatResponse();
        // //先拿到结果，再拿到它的输出信息，也可以拿到原信息，比如消耗的多少token，这里拿到输出信息中大模型返回的文本
        // String content = chatResponse.getResult().getOutput().getText();
        // log.info("content: {}", content);
        // return content;
        // }

        /**
         * AI调用MCP服务
         */
        // 引入这个类，我们的Spring AI MCP 服务，它在启动的时候，会自动去读取我们刚刚所写的 mcp-servers.json 配置文件
        // 从中找到所有的工具，然后自动注册到这个工具提供者类上，我们就可以直接使用它了
        @Resource
        private ToolCallbackProvider toolCallbackProvider;

        public String doChatWithMCP(String message, String chatId) {
                ChatResponse chatResponse = chatClient
                                .prompt()
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                .tools(toolCallbackProvider)
                                .call()
                                .chatResponse();
                return chatResponse.getResult().getOutput().getText();
        }

        // 这里我们就要定义一个格式了，定义一个恋爱报告的格式
        // 这个record语法可以快速的定义一个类，我们就可以理解为一个类，含有title、suggestion字段
        // 和定义方法的方法一样，这样一个类就定义好了
        public record LoveReport(String title, List<String> suggestions) {

        }

        /**
         * 结构化输出，恋爱报告功能，也就是将大模型的返回直接封装到实体类里面
         */
        public LoveReport doChatWithReport(String message, String chatId) {
                LoveReport loveReport = chatClient
                                .prompt()
                                .system(SystemConstants.CHAT_SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                .call()
                                .entity(LoveReport.class);
                log.info("loveReport: {}", loveReport);
                return loveReport;
        }

        /**
         * 心跳挽回战
         */
        public Flux<String> gameStreamChat(String message, String chatId) {
                return gameClient.prompt()
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                .stream()
                                .content();
        }

        /**
         * 情绪返回
         */
        public String doChatWithEmo(String message, String chatId) {
                ChatResponse chatResponse = emoClient
                                .prompt()
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                .call()
                                .chatResponse();
                return chatResponse.getResult().getOutput().getText();
        }
}
