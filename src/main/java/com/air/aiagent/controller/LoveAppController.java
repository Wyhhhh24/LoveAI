package com.air.aiagent.controller;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.air.aiagent.annotation.ClearContext;
import com.air.aiagent.annotation.LoginCheck;
import com.air.aiagent.app.LoveApp;
import com.air.aiagent.common.BaseResponse;
import com.air.aiagent.common.ResultUtils;
import com.air.aiagent.domain.dto.ChatRequest;
import com.air.aiagent.domain.entity.ChatMessage;
import com.air.aiagent.domain.entity.ChatSession;
import com.air.aiagent.domain.entity.User;
import com.air.aiagent.domain.vo.*;
import com.air.aiagent.exception.BusinessException;
import com.air.aiagent.exception.ErrorCode;
import com.air.aiagent.mapper.repository.ChatMessageRepository;
import com.air.aiagent.mapper.repository.ChatSessionRepository;
import com.air.aiagent.service.UserFileService;
import com.air.aiagent.service.UserService;
import com.air.aiagent.service.impl.ChatMessageService;
import com.air.aiagent.service.impl.ChatSessionService;
import com.air.aiagent.service.impl.ProductRecommendService;
import com.air.aiagent.utils.SessionIdGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * 恋爱咨询应用控制器
 * 
 * @author AI Assistant
 */
@Slf4j
@RestController
@RequestMapping("/love")
public class LoveAppController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private UserService userService;

    @Resource
    private UserFileService userFileService;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ProductRecommendService productRecommendService;

    /**
     * RAG知识库对话，支持工具调用
     */
    @LoginCheck
    @PostMapping(value = "/chat/rag", produces = "text/event-stream;charset=UTF-8")
    @ClearContext
    public Flux<String> chatWithRag(@RequestBody ChatRequest request) {
        log.info("收到RAG知识库对话请求: {}", request);
        // 1.已经做了用户是否登录的检测也就是 chatId 是否存在的判断，接下来判断该 sessionId 在数据库中是否存在
        Optional<ChatSession> session = chatSessionService.findById(request.getSessionId());
        if (!session.isPresent()) {
            // 2.如果该 session 不存在，抛异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的 sessionId");
        }
        return loveApp.smartChat(request);
    }


    @LoginCheck
    @PostMapping("/game/emo")
    public BaseResponse<GameChatVO> gameEmo(@RequestBody ChatRequest request) {
        log.info("收到判断情绪请求: {}", request);
        GameChatVO gameChatVO = loveApp.doChatWithEmo(request.getMessage(), request.getChatId());
        return ResultUtils.success(gameChatVO);
    }

    @LoginCheck
    @PostMapping(value = "/game/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> gameChat(@RequestBody ChatRequest request) {
        log.info("收到游戏请求: {}", request);
        return loveApp.gameStreamChat(request);
    }

    /**
     * 获取用户文件列表
     */
    @LoginCheck
    @PostMapping("/getUserFile")
    public BaseResponse<List<UserFileVO>> getUserFileList(@RequestBody ChatRequest request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        List<UserFileVO> userFileList = userFileService.getUserFileList(loginUser.getId());
        return ResultUtils.success(userFileList);
    }

    /**
     * 查询最新的会话历史，也就是进入聊天页面之后，默认进行展示的聊天历史
     */
    @LoginCheck
    @PostMapping("/getLatestChatHistory")
    public BaseResponse<ChatHistory> getLatestChatSession(@RequestBody ChatRequest request, HttpServletRequest httpServletRequest) {
        // 1.获取当前登录用户
        User loginUser = userService.getLoginUser(httpServletRequest);

        // 2.创建返回对象
        ChatHistory chatHistory = new ChatHistory();

        // 3.获取当前用户的最新会话记录
        ChatSession latestChatSession = chatSessionService.getLatestSessionByUserId(loginUser.getId().toString());
        if (latestChatSession == null) {
            // 如果不存在最新的会话历史，直接返回一个空对象
            return ResultUtils.success(chatHistory);
        }

        // 4.存在最新的会话，设置会话Id
        chatHistory.setSessionId(latestChatSession.getId());

        // 5.根据会话Id 查找该记录中的聊天历史，包含最新消息在内的最近10条记录，转换成 VO
        List<ChatMessage> latestChatMessageList = chatMessageService
                .findHistoryExcludingLatest(latestChatSession.getId(), 10, 0);
        List<ChatMessageVO> latestChatMessageVOList = latestChatMessageList.stream().map(chatMessage -> {
            ChatMessageVO chatMessageVO = ChatMessageVO.builder()
                    .id(chatMessage.getId())
                    .chatId(chatMessage.getChatId())
                    .sessionId(chatMessage.getSessionId())
                    .content(chatMessage.getContent())
                    .isAiResponse(chatMessage.getIsAiResponse())
                    .build();
            if(chatMessage.getMetadata() != null
                    && chatMessage.getMetadata().getRecommendedProductIds() != null
                    && !chatMessage.getMetadata().getRecommendedProductIds().isEmpty()){
                chatMessageVO.setRecommendedProducts(productRecommendService.getProductVOsByIds(chatMessage.getMetadata().getRecommendedProductIds()));
            }
            return chatMessageVO;
        }).toList();

        // 6.进行封装返回
        chatHistory.setChatMessageVOList(latestChatMessageVOList);
        return ResultUtils.success(chatHistory);
    }


    /**
     * 根据 sessionId 查询该会话的聊天记录
     */
    @LoginCheck
    @PostMapping("/getChatMessageBySessionId")
    public BaseResponse<ChatHistory> getChatMessageBySessionId(@RequestBody ChatRequest request,
            HttpServletRequest httpServletRequest) {
        // 1.获取当前登录用户
        User loginUser = userService.getLoginUser(httpServletRequest);

        // 2.创建返回对象
        ChatHistory chatHistory = new ChatHistory();

        // 3.判断当前用户是否存在该 会话Id
        String sessionId = request.getSessionId();
        if (sessionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话Id为空，请选择会话");
        }

        // 4.判断当前用户是否存在该会话Id，不存在的话抛出异常
        ChatSession chatSession = chatSessionService.findByIdAndChatId(sessionId, loginUser.getId().toString())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "当前用户不存在该会话"));

        // 5.存在最新的会话，设置会话Id
        chatHistory.setSessionId(chatSession.getId());

        // 6.根据会话Id 查找该记录中的聊天历史，包含最新消息在内的最近10条记录，转换成 VO
        List<ChatMessage> latestChatMessageList = chatMessageService
                .findHistoryExcludingLatest(chatSession.getId(), 10, 0);
        List<ChatMessageVO> latestChatMessageVOList = latestChatMessageList.stream().map(chatMessage -> {
            ChatMessageVO chatMessageVO = ChatMessageVO.builder()
                    .id(chatMessage.getId())
                    .chatId(chatMessage.getChatId())
                    .sessionId(chatMessage.getSessionId())
                    .content(chatMessage.getContent())
                    .isAiResponse(chatMessage.getIsAiResponse())
                    .build();
            if(chatMessage.getMetadata() != null
                    && chatMessage.getMetadata().getRecommendedProductIds() != null
                    && !chatMessage.getMetadata().getRecommendedProductIds().isEmpty()){
                chatMessageVO.setRecommendedProducts(productRecommendService.getProductVOsByIds(chatMessage.getMetadata().getRecommendedProductIds()));
            }
            return chatMessageVO;
        }).toList();

        // 7.进行封装返回
        chatHistory.setChatMessageVOList(latestChatMessageVOList);
        return ResultUtils.success(chatHistory);
    }

    /**
     * 创建会话返回 sessionId ，并将会话记录保存到数据库中，之后的用户发送的消息必须携带这个生成的 sessionId
     */
    @LoginCheck
    @PostMapping("/createChatSession")
    public BaseResponse<String> createChatSession(@RequestBody ChatRequest request,
            HttpServletRequest httpServletRequest) {
        // 1.获取当前登录用户
        User loginUser = userService.getLoginUser(httpServletRequest);

        // 2.创建会话Id (时间戳 + "_" + 用户Id)
        String sessionId = SessionIdGenerator.generateSessionId(loginUser.getId());

        // 3.保存会话记录到 MongoDB 中
        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .chatId(loginUser.getId().toString())
                .sessionName("新对话")
                .build();
        chatSessionService.save(chatSession);

        // 3.返回会话Id
        return ResultUtils.success(sessionId);
    }


    /**
     * 查询用户的所有会话历史，返回 sessionList ，用于在页面左侧列表展示会话历史
     * 后面可以完善一下，例如只查询一些比较活跃的 session
     */
    @LoginCheck
    @PostMapping("/getChatSessionList")
    public BaseResponse<List<ChatSessionVO>> getChatSessionList(@RequestBody ChatRequest request,
            HttpServletRequest httpServletRequest) {
        // 1.根据 用户Id 查询所有会话记录
        List<ChatSession> chatSessionList = chatSessionService.findByChatId(request.getChatId());
        if (chatSessionList.isEmpty()) {
            return ResultUtils.success(List.of());
        }
        // 2.转换成 ChatSessionVO 集合
        List<ChatSessionVO> list = chatSessionList.stream()
                .map(chatSession -> BeanUtil.copyProperties(chatSession, ChatSessionVO.class)).toList();
        return ResultUtils.success(list);
    }


    /**
     * 删除会话（包括会话记录和所有聊天消息）
     */
    @LoginCheck
    @PostMapping("/deleteChatSession")
    public BaseResponse<Boolean> deleteChatSession(@RequestBody ChatRequest request,
            HttpServletRequest httpServletRequest) {
        // 1.获取当前登录用户
        Long chatId = userService.getLoginUser(httpServletRequest).getId();
        String sessionId = request.getSessionId();

        // 2.判断是否有传 sessionId 参数
        if (StrUtil.isBlank(sessionId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择会话");
        }

        // 3.验证会话是否存在且属于当前用户
        ChatSession currentChatSession = chatSessionService.getSessionByChatIdAndSessionId(chatId.toString(),
                sessionId);
        if (currentChatSession == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前用户不存在该会话");
        }

        // 4.删除该会话中的所有聊天记录
        long deletedMessages = chatMessageService.deleteBySessionId(sessionId);
        log.info("删除会话消息，sessionId={}，删除消息数量={}", sessionId, deletedMessages);

        // 5.删除该会话
        long deletedSession = chatSessionService.deleteSession(sessionId, chatId.toString());

        // 6.判断删除结果
        if (deletedSession > 0) {
            log.info("会话删除成功，sessionId={}，删除消息数={}，删除会话数={}",
                    sessionId, deletedMessages, deletedSession);
            return ResultUtils.success(true);
        } else {
            log.warn("会话删除失败，sessionId={}，未找到对应会话", sessionId);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败，会话可能已被删除");
        }
    }


    /**
     * 清空会话中的所有聊天记录（保留会话本身）
     */
    @LoginCheck
    @PostMapping("/deleteChatSessionBySessionId")
    public BaseResponse<Boolean> deleteChatSessionBySessionId(@RequestBody ChatRequest request,
            HttpServletRequest httpServletRequest) {
        // 1.获取当前登录用户
        Long chatId = userService.getLoginUser(httpServletRequest).getId();
        String sessionId = request.getSessionId();

        // 2.判断是否有传 sessionId 参数
        if (StrUtil.isBlank(sessionId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择会话");
        }

        // 3.验证会话是否存在且属于当前用户
        ChatSession currentChatSession = chatSessionService.getSessionByChatIdAndSessionId(chatId.toString(),
                sessionId);
        if (currentChatSession == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前用户不存在该会话");
        }

        // 4.删除该会话中的所有聊天记录（保留会话本身）
        long deletedMessages = chatMessageService.deleteBySessionId(sessionId);

        // 5.判断删除结果并返回
        if (deletedMessages > 0) {
            log.info("清空会话消息成功，sessionId={}，删除消息数={}", sessionId, deletedMessages);
            return ResultUtils.success(true);
        } else {
            log.info("会话消息为空，无需清空，sessionId={}", sessionId);
            return ResultUtils.success(true);
        }
    }

    // POST http://localhost:8123/api/love/game/emo
    // 请求体：{ message: 用户输入, chatId: '23034480211' }
    // @PostMapping("/game/emo")
    // public String gameEmo(@RequestBody ChatRequest request) {
    // log.info("收到判断情绪请求: {}", request);
    // UserContext.setUserId(request.getChatId());
    // return loveApp.doChatWithEmo(request.getMessage(), request.getChatId());
    // }
    // 这个接口返回的是一个情绪
    // 你现在添加一个逻辑，在进入游戏页面之前可以有一个符合当前主题的弹窗可以给用户输入女朋友不开心的原因，然后后端会返回的是一个情绪
    // 你根据返回的情绪渲染不同的颜色效果的游戏界面

    // /**
    // * 工具调用对话接口
    // */
    // @ClearContext //方法执行后清理内存中 ThreadLocal ，防止内存泄露
    // @PostMapping("/chat/tools")
    // public ChatResponse chatWithTools(@RequestBody ChatRequest request) {
    // log.info("收到工具调用对话请求: {}", request);
    // UserContext.setUserId(request.getChatId());
    // String response = loveApp.doChatWithTools(request.getMessage(),
    // request.getChatId());
    // return new ChatResponse(response);
    // }
    //
    //
    //
    // /**
    // * 基础对话接口
    // */
    // @PostMapping("/chat")
    // public ChatResponse chat(@RequestBody ChatRequest request) {
    // log.info("收到对话请求: {}", request);
    // String response = loveApp.doChat(request.getMessage(), request.getChatId());
    // return new ChatResponse(response);
    // }
    //
    //
    // /**
    // * 恋爱报告生成接口
    // */
    // @PostMapping("/report")
    // public LoveReportResponse generateReport(@RequestBody ChatRequest request) {
    // log.info("收到恋爱报告生成请求: {}", request);
    // LoveApp.LoveReport report = loveApp.doChatWithReport(request.getMessage(),
    // request.getChatId());
    // return new LoveReportResponse(report.title(), report.suggestions());
    // }
    //
    //
    // /**
    // * MCP服务调用接口
    // */
    // @PostMapping("/chat/mcp")
    // public ChatResponse chatWithMCP(@RequestBody ChatRequest request) {
    // log.info("收到MCP服务调用请求: {}", request);
    // String response = loveApp.doChatWithMCP(request.getMessage(),
    // request.getChatId());
    // return new ChatResponse(response);
    // }
}
