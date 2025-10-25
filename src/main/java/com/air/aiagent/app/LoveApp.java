package com.air.aiagent.app;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.air.aiagent.advisor.MyLoggerAdvisor;
import com.air.aiagent.constant.SystemConstants;
import com.air.aiagent.domain.dto.ChatRequest;
import com.air.aiagent.domain.entity.*;
import com.air.aiagent.domain.vo.GameChatVO;
import com.air.aiagent.domain.vo.ProductVO;
import com.air.aiagent.mapper.repository.ChatMessageRepository;
import com.air.aiagent.mapper.repository.ChatSessionRepository;
import com.air.aiagent.service.impl.AsyncTaskService;
import com.air.aiagent.service.impl.ChatMessageService;
import com.air.aiagent.service.impl.ChatSessionService;
import com.air.aiagent.service.impl.ProductRecommendService;
import com.air.aiagent.utils.IntentRecognizer;
import com.air.aiagent.utils.SessionIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static com.air.aiagent.constant.Constant.GAME_SESSION_NAME;
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

        private final ChatClient emoClient;

        /**
         * 初始化 AI 客户端 ChatClient
         */
        public LoveApp(ChatModel dashscopeChatModel) {
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
                                                // 自定义日志拦截器，可按需开启
                                                new MyLoggerAdvisor())
                                .build();

                emoClient = ChatClient.builder(dashscopeChatModel)
                                .defaultSystem(SystemConstants.EMOTION_DETECTION)
                                .defaultAdvisors(
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
        private ChatSessionService chatSessionService;

        @Resource
        private ChatMessageService chatMessageService;

        // 引入这个类，我们的Spring AI MCP 服务，它在启动的时候，会自动去读取我们刚刚所写的 mcp-servers.json 配置文件
        // 从中找到所有的工具，然后自动注册到这个工具提供者类上，我们就可以直接使用它了
        @Resource
        private ToolCallbackProvider toolCallbackProvider;

        /**
         * 意图识别器
         */
        @Resource
        private IntentRecognizer intentRecognizer;

        /**
         * 商品推荐服务
         */
        @Resource
        private ProductRecommendService productRecommendService;

        /**
         * AI 调用工具能力
         */
        @Resource
        private ToolCallback[] allTools;

        /**
         * 智能对话入口 - 根据意图选择是否推荐商品
         */
        public Flux<String> smartChat(ChatRequest request) {
                // 1.调用AI意图识别，判断当前提示词有没有购买意图
                boolean needRecommend = intentRecognizer.needProductRecommend(request.getMessage());

                // 2.根据意图选择处理方式
                if (needRecommend) {
                        log.info("检测到购买意图，启用商品推荐模式");
                        String scene = intentRecognizer.recognizeScene(request.getMessage());
                        return chatWithProductRecommend(request, scene);
                } else {
                        log.info("纯咨询对话，不推荐商品");
                        return doChatWithRagAndTools(request, false); // 你现有的方法
                }
        }

        /**
         * 带商品推荐的 RAG 对话
         */
        private Flux<String> chatWithProductRecommend(ChatRequest request, String scene) {
                // 0.同步保存用户消息
                saveUserMessage(request);

                // 1.查询历史对话
                List<ChatMessage> historyMessages = chatMessageService
                                .findHistoryExcludingLatest(request.getSessionId(), 10, 1);
                // 如果历史对话为空，当前消息作为会话名称
                if (historyMessages.isEmpty()) {
                        // 更新会话名称
                        boolean success = chatSessionService.updateSessionName(
                                        request.getSessionId(),
                                        request.getChatId(),
                                        request.getMessage());
                        if (success) {
                                log.info("会话名称已更新为: {}", request.getMessage());
                        }
                }

                // 2.根据场景，从数据库中查询相关商品，只查询 3 个商品 todo 这里的推荐算法得要重新考虑一下
                List<Product> productList = productRecommendService.recommendByScene(scene, 3);

                // 3.如果没有商品，降级为普通对话
                if (productList.isEmpty()) {
                        log.warn("场景 [{}] 未找到相关商品，降级为纯咨询模式", scene);
                        return doChatWithRagAndTools(request, true);
                }

                // 4.将商品 List 转换成 String
                String productInfo = formatProductsForPrompt(productList);
                // 构造包含商品信息的系统提示词
                String enhancedSystemPrompt = SystemConstants.ENHANCEDSYSTEMPROMPT.formatted(scene, productInfo);

                // 5.构建完整提示词
                String fullPrompt = buildPromptWithContext(
                                historyMessages,
                                request.getMessage(),
                                request.getChatId());
                log.info("用户 id={}，完整提示词构建完成，长度={}", request.getChatId(), fullPrompt.length());

                // 7. 创建 StringBuilder 来累积 AI 的流式响应
                StringBuilder aiResponseBuilder = new StringBuilder();
                // 创建一个 AI 回复的消息 ID
                String aiMessageId = UUID.randomUUID().toString();
                // 创建一个计时器，用于记录流式响应的时长
                long startTime = System.currentTimeMillis();

                // 8. 处理流式响应，提取商品ID todo 这里需要解读一下
                AtomicReference<List<Long>> recommendedProductIds = new AtomicReference<>(new ArrayList<>());

                return chatClient
                                .prompt()
                                .system(enhancedSystemPrompt) // 设置新的系统提示词，推荐商品提示词
                                .user(fullPrompt)
                                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                                .stream()
                                .content()
                                .doOnNext(
                                                chunk -> aiResponseBuilder.append(chunk))
                                .doOnComplete(() -> {
                                        long duration = System.currentTimeMillis() - startTime;
                                        String reply = aiResponseBuilder.toString();

                                        // 从 AI 回复中，提取商品ID
                                        List<Long> productIds = extractProductIds(reply);
                                        recommendedProductIds.set(productIds);

                                        // 清理标记符，也就是 ai 回复中可能包含一些标记符，也就是历史记录中是没有这些标识的，但是前端流式输出的时候没有去除
                                        String cleanReply = removeProductMarkers(reply);

                                        // 保存消息
                                        ChatMessage aiMessage = ChatMessage.builder()
                                                        .id(aiMessageId)
                                                        .chatId(request.getChatId())
                                                        .sessionId(request.getSessionId())
                                                        .content(cleanReply)
                                                        .messageType(MessageType.TEXT)
                                                        .isAiResponse(true)
                                                        .metadata(MessageMetadata.builder()
                                                                        .responseTimeMs((int) duration)
                                                                        .tokenCount(estimateTokens(cleanReply))
                                                                        .recommendedProductIds(productIds) // 如果该条记录推荐了商品，ID
                                                                                                           // 列表，就保存
                                                                        .build())
                                                        .build();
                                        // 保存消息到数据库
                                        chatMessageService.save(aiMessage);

                                        // 更新消息计数，这个方法就是 +2
                                        chatSessionService.incrementMessageCount(request.getSessionId());
                                })
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
                                                chatMessageService.save(errorMessage);
                                                log.warn("AI错误消息已保存，sessionId={}, 长度={}", request.getSessionId(),
                                                                errorContent.length());

                                                // 更新消息计数，这个方法就是 +2
                                                chatSessionService.incrementMessageCount(request.getSessionId());
                                        }
                                })
                                // ai流式输出之后，最后根据商品 id 查找对应的商品信息，进行拼接
                                .concatWith(Flux.defer(() -> {
                                        List<Long> ids = recommendedProductIds.get();
                                        if (ids.isEmpty()) {
                                                return Flux.empty();
                                        }
                                        // 查询数据库，返回商品信息
                                        List<ProductVO> vos = productRecommendService.getProductVOsByIds(ids);
                                        String productJson = "\n[PRODUCTS]" + JSONUtil.toJsonStr(vos) + "[/PRODUCTS]";

                                        log.info("返回推荐商品，数量={}", vos.size());
                                        return Flux.just(productJson);
                                }));
        }

        /**
         * 基于知识库文档，支持工具调用
         */
        public Flux<String> doChatWithRagAndTools(ChatRequest request, Boolean isNotDirect) {
                // 如果是降级调用的话，不需要保存用户信息，否则就保存用户信息
                if (!isNotDirect) {
                        // 1. 同步保存用户消息
                        saveUserMessage(request);
                }

                // 2. 获取历史上下文（排除刚保存的用户消息），查询了 10 条历史记录
                List<ChatMessage> historyMessages = chatMessageService
                                .findHistoryExcludingLatest(request.getSessionId(), 10, 1);
                log.info("获取用户，id={}，历史上下文，数量={}", request.getChatId(), historyMessages.size());

                // 如果历史记录集合是空的，并且不是降级调用，将当前的消息作为这个 session 的 sessionName
                if (historyMessages.isEmpty() && !isNotDirect) {
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
                return chatClient
                                .prompt()
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
                                .tools(toolCallbackProvider)
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
                                        chatMessageService.save(aiMessage);
                                        log.info("AI消息已保存，sessionId={}, 长度={}", request.getSessionId(),
                                                        aiContent.length());

                                        // 更新会话的消息计数（+2，用户消息1条 + AI回复1条）
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
                                                chatMessageService.save(errorMessage);
                                                log.warn("AI错误消息已保存，sessionId={}, 长度={}", request.getSessionId(),
                                                                errorContent.length());

                                                // 更新会话的消息计数（+2，用户消息1条 + AI错误回复1条）
                                                chatSessionService.incrementMessageCount(request.getSessionId());
                                        }
                                });
        }

        /**
         * 将商品列表格式化为提示词
         * 清晰展示商品ID、名称、价格等信息，防止AI推荐错误商品
         */
        private String formatProductsForPrompt(List<Product> products) {
                if (products == null || products.isEmpty()) {
                        return "暂无可推荐商品";
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < products.size(); i++) {
                        Product p = products.get(i);
                        sb.append(String.format(
                                        "【商品%d】\n" +
                                                        "  商品ID: %d（必须使用此ID）\n" +
                                                        "  商品名称: %s\n" +
                                                        "  分类: %s\n" +
                                                        "  价格: %.2f元\n" +
                                                        "  描述: %s\n" +
                                                        "  适用场景: %s\n",
                                        i + 1,
                                        p.getId(),
                                        p.getProductName(),
                                        p.getCategory(),
                                        p.getPrice(),
                                        p.getDescription(),
                                        p.getScene()));
                        if (i < products.size() - 1) {
                                sb.append("\n");
                        }
                }
                return sb.toString();
        }

        /**
         * 从 AI 回复中提取推荐的商品 ID 正则表达式提取
         * 解析格式：【推荐商品】商品ID: 1\n商品ID: 3【结束】
         */
        private List<Long> extractProductIds(String aiReply) {
                List<Long> ids = new ArrayList<>();

                // 首先检查是否包含完整的标记符
                if (!aiReply.contains("【推荐商品】") || !aiReply.contains("【结束】")) {
                        log.warn("AI回复中缺少商品推荐标记符，无法提取商品ID");
                        return ids;
                }

                // 提取标记符之间的内容
                Pattern markerPattern = Pattern.compile("【推荐商品】([\\s\\S]*?)【结束】");
                Matcher markerMatcher = markerPattern.matcher(aiReply);

                if (markerMatcher.find()) {
                        String content = markerMatcher.group(1);
                        // 在标记符内提取商品ID
                        Pattern idPattern = Pattern.compile("商品ID[：:]*\\s*(\\d+)");
                        Matcher idMatcher = idPattern.matcher(content);

                        while (idMatcher.find()) {
                                try {
                                        ids.add(Long.parseLong(idMatcher.group(1)));
                                } catch (NumberFormatException e) {
                                        log.warn("解析商品ID失败: {}", idMatcher.group(1));
                                }
                        }
                } else {
                        log.warn("无法找到商品推荐标记符内容");
                }

                log.info("从AI回复中提取到商品ID: {}", ids);
                return ids;
        }

        /**
         * 移除商品推荐的标记符，只保留用户可读的内容
         */
        private String removeProductMarkers(String aiReply) {
                return aiReply
                                .replaceAll("【推荐商品】[\\s\\S]*?【结束】", "")
                                .trim();
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
        private void saveUserMessage(ChatRequest request) {
                String userMessageId = UUID.randomUUID().toString();
                ChatMessage userMessage = ChatMessage.builder()
                                .id(userMessageId)
                                .chatId(request.getChatId())
                                .sessionId(request.getSessionId())
                                .messageType(MessageType.TEXT)
                                .content(request.getMessage())
                                .isAiResponse(false)
                                .build();
                chatMessageService.save(userMessage);
        }

        /**
         * 保存用户游戏记录到 MongoDB
         */
        private void saveUserGameMessage(ChatRequest request) {
                String userMessageId = UUID.randomUUID().toString();
                ChatMessage userMessage = ChatMessage.builder()
                                .id(userMessageId)
                                .chatId(request.getChatId())
                                .sessionId(request.getSessionId())
                                .messageType(MessageType.GAME)
                                .content(request.getMessage())
                                .isAiResponse(false)
                                .build();
                chatMessageService.save(userMessage);
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
                prompt.append("请根据历史对话上下文，针对性地回答用户的当前问题");

                return prompt.toString();
        }

        /**
         * 情绪返回
         */
        public GameChatVO doChatWithEmo(String message, String chatId) {
                // 1.情绪分析
                ChatResponse chatResponse = emoClient
                                .prompt()
                                .user(message)
                                .call()
                                .chatResponse();
                String emo = chatResponse.getResult().getOutput().getText();
                // 2.创建 sessionId ，并构造对象返回
                String sessionId = SessionIdGenerator.generateSessionId(Long.valueOf(chatId));

                // 3.创建一个游戏会话，并保存，同时保存这轮游戏中的女友情绪
                ChatSession gameChatSession = ChatSession.builder()
                                .sessionName(GAME_SESSION_NAME)
                                .id(sessionId)
                                .chatId(chatId)
                                .build();
                chatSessionService.save(gameChatSession);
                ChatMessage chatMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .chatId(chatId)
                                .messageType(MessageType.GAME)
                                .isAiResponse(false)
                                .content("女友生气的原因：" + message + ",女友现在的心情：" + emo)
                                .build();
                chatMessageService.save(chatMessage);

                // 4.构建返回对象，并返回
                return GameChatVO.builder()
                                .emo(emo)
                                .sessionId(sessionId)
                                .build();
        }

        /**
         * 心跳挽回战
         */
        public Flux<String> gameStreamChat(ChatRequest request) {
                // 1.同步保存用户消息
                saveUserGameMessage(request);

                // 2. 获取历史上下文（排除刚保存的用户消息），查询了 10 条历史记录
                List<ChatMessage> historyMessages = chatMessageService
                                .findHistoryExcludingLatest(request.getSessionId(), 10, 1);
                log.info("获取用户，id={}，历史上下文，数量={}", request.getChatId(), historyMessages.size());

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

                // 5. 流式调用AI并累积响应
                return gameClient
                                .prompt()
                                .user(fullPrompt)
                                .stream()
                                .content() // 流式处理
                                // 关键：doOnNext累积每个chunk
                                .doOnNext(chunk -> {
                                        aiResponseBuilder.append(chunk);
                                })
                                // 关键：doOnComplete 在流式结束后保存
                                .doOnComplete(() -> {
                                        // 同步保存完整的 AI 回复
                                        String aiContent = aiResponseBuilder.toString();
                                        // 构建消息实体
                                        ChatMessage aiMessage = ChatMessage.builder()
                                                        .id(aiMessageId)
                                                        .chatId(request.getChatId())
                                                        .sessionId(request.getSessionId())
                                                        .content(aiContent)
                                                        .messageType(MessageType.GAME)
                                                        .isAiResponse(true)
                                                        .build();
                                        // 保存到 MongoDB
                                        chatMessageService.save(aiMessage);
                                        log.info("AI消息已保存，sessionId={}, 长度={}", request.getSessionId(),
                                                        aiContent.length());
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
                                                                .messageType(MessageType.GAME)
                                                                .isAiResponse(true)
                                                                .build();
                                                chatMessageService.save(errorMessage);
                                                log.warn("AI错误消息已保存，sessionId={}, 长度={}", request.getSessionId(),
                                                                errorContent.length());
                                        }
                                });
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
        // public String doChatWithMCP(String message, String chatId) {
        // ChatResponse chatResponse = chatClient
        // .prompt()
        // .user(message)
        // .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
        // .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
        // .tools(toolCallbackProvider)
        // .call()
        // .chatResponse();
        // return chatResponse.getResult().getOutput().getText();
        // }

        // 这里我们就要定义一个格式了，定义一个恋爱报告的格式
        // 这个record语法可以快速的定义一个类，我们就可以理解为一个类，含有title、suggestion字段
        // 和定义方法的方法一样，这样一个类就定义好了
        // public record LoveReport(String title, List<String> suggestions) {
        //
        // }
        //
        // /**
        // * 结构化输出，恋爱报告功能，也就是将大模型的返回直接封装到实体类里面
        // */
        // public LoveReport doChatWithReport(String message, String chatId) {
        // LoveReport loveReport = chatClient
        // .prompt()
        // .system(SystemConstants.CHAT_SYSTEM_PROMPT +
        // "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
        // .user(message)
        // .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
        // .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
        // .call()
        // .entity(LoveReport.class);
        // log.info("loveReport: {}", loveReport);
        // return loveReport;
        // }
}
