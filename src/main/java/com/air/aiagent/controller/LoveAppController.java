package com.air.aiagent.controller;
import com.air.aiagent.annotation.ClearContext;
import com.air.aiagent.annotation.LoginCheck;
import com.air.aiagent.app.LoveApp;
import com.air.aiagent.common.BaseResponse;
import com.air.aiagent.common.ResultUtils;
import com.air.aiagent.context.UserContext;
import com.air.aiagent.domain.dto.ChatRequest;
import com.air.aiagent.domain.entity.User;
import com.air.aiagent.domain.vo.UserFileVO;
import com.air.aiagent.service.UserFileService;
import com.air.aiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.List;


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

    /**
     * RAG知识库对话，支持工具调用
     */
    @LoginCheck
    @PostMapping(value = "/chat/rag", produces = "text/html;charset=UTF-8")
    @ClearContext
    public Flux<String> chatWithRag(@RequestBody ChatRequest request) {
        log.info("收到RAG知识库对话请求: {}", request);
        UserContext.setUserId(request.getChatId());
        return loveApp.doChatWithRagAndTools(request.getMessage(), request.getChatId());
    }

//    @RequestMapping(value = "/game", produces = "text/html;charset=UTF-8")
//    public Flux<String> gameChat(@RequestBody ChatRequest request) {
//
//        log.info("收到游戏请求: {}", request);
//
//    }

    @LoginCheck
    @PostMapping("/game/emo")
    public String gameEmo(@RequestBody ChatRequest request) {
        log.info("收到判断情绪请求: {}", request);
        UserContext.setUserId(request.getChatId());
        return loveApp.doChatWithEmo(request.getMessage(), request.getChatId());
    }

    @LoginCheck
    @PostMapping(value = "/game/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> gameChat(@RequestBody ChatRequest request) {
        log.info("收到游戏请求: {}", request);
        UserContext.setUserId(request.getChatId());
        return loveApp.gameStreamChat(request.getMessage(), request.getChatId());
    }

    /**
     * 获取用户文件
     */
    @LoginCheck
    @PostMapping("/getUserFile")
    public BaseResponse<List<UserFileVO>> getUserFileList(@RequestBody ChatRequest request, HttpServletRequest httpServletRequest){
        User loginUser = userService.getLoginUser(httpServletRequest);
        List<UserFileVO> userFileList = userFileService.getUserFileList(loginUser.getId());
        return ResultUtils.success(userFileList);
    }


    //POST http://localhost:8123/api/love/game/emo
    //请求体：{ message: 用户输入, chatId: '23034480211' }
    //    @PostMapping("/game/emo")
    //    public String gameEmo(@RequestBody ChatRequest request) {
    //        log.info("收到判断情绪请求: {}", request);
    //        UserContext.setUserId(request.getChatId());
    //        return loveApp.doChatWithEmo(request.getMessage(), request.getChatId());
    //    }
    //这个接口返回的是一个情绪
    //你现在添加一个逻辑，在进入游戏页面之前可以有一个符合当前主题的弹窗可以给用户输入女朋友不开心的原因，然后后端会返回的是一个情绪
    //你根据返回的情绪渲染不同的颜色效果的游戏界面


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
