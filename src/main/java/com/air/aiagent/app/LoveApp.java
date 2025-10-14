package com.air.aiagent.app;
import com.air.aiagent.advisor.MyLoggerAdvisor;
import com.air.aiagent.constant.SystemConstants;
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
        //初始化基于内存的对话记忆，这个 InMemoryChatMemory 是 ChatMemory 接口的一个实现类，SpringAI已经帮我们实现好了
        chatMemory = new InMemoryChatMemory();
        gameMemory = new InMemoryChatMemory();
        emoMemory = new InMemoryChatMemory();

        /**
         * 初始化 ChatClient
         */
        chatClient=ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SystemConstants.CHAT_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志拦截器，可按需开启
                        new MyLoggerAdvisor()
                        //,new ReReadingAdvisor()
                )
                .build();

        /**
         * 初始化 GameClient
         */
        gameClient=ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(gameMemory),
                        //自定义日志拦截器，可按需开启
                        new MyLoggerAdvisor()
                )
                .build();

        emoClient=ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SystemConstants.EMOTION_DETECTION)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(emoMemory),
                        //自定义日志拦截器，可按需开启
                        new MyLoggerAdvisor()
                ).build();
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
        //这里需要为advisors提供参数，要去指定当前这个关联的历史上下文是哪一个对话的上下文，两个参数
        //spec 就是一个ChatClient.AdvisorSpec对象，这其实就当作一个Map，就是传递给拦截器的参数
        //1.当前需要取哪个对话的上下文，对应上下文的ID  2.指定你需要获取消息的条数，也就是这个参数可以设置传送给大模型多少条消息

        String content = response.getResult().getOutput().getText(); //先拿到结果，再拿到它的输出信息，也可以拿到原信息，比如消耗的多少token，这里拿到输出信息中大模型返回的文本
        log.info("content: {}", content);
        return content;
    }


    /**
     * AI 恋爱知识库问答功能
     */
    // 注入基于内存的 VectorStore Bean
    @Resource  // 这个注解就优先基于名称注入
    private VectorStore loveAppVectorStore;  //确保和我们自定义的知识库 Bean 名称一致

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;

    /**
     * AI 调用工具能力
     */
    @Resource
    private ToolCallback[] allTools;


    //RAG 知识库进行对话
    public Flux<String> doChatWithRagAndTools(String message, String chatId){
        return chatClient.prompt()
                .user("userId = "+chatId+","+message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //应用 RAG 知识库问答（基于内存的知识库服务）
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                //应用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                //应用 RAG 检索增强服务（基于 PgVector 向量存储）
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .tools(allTools)
                .stream()
                .content();  //流式处理
    }


//    /**
//     * AI 恋爱大师（支持调用工具）
//     */
//    public String doChatWithTools(String message, String chatId) {
//        ChatResponse chatResponse = chatClient
//                .prompt()
//                .user(message)
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//                .tools(allTools)
//                .call()
//                .chatResponse();  //同步阻塞调用
//        return chatResponse.getResult().getOutput().getText();
//    }


//    //RAG 知识库进行对话
//    public String doChatWithRag(String message, String chatId){
//        ChatResponse chatResponse = chatClient.prompt()
//                .user(message)
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//                //应用 RAG 知识库问答（基于本地知识库服务）
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
//                //应用 RAG 检索增强服务（基于云知识库服务）
////                .advisors(loveAppRagCloudAdvisor)
//                //应用 RAG 检索增强服务（基于 PgVector 向量存储）
////                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
//                .call()
//                .chatResponse();
//        //先拿到结果，再拿到它的输出信息，也可以拿到原信息，比如消耗的多少token，这里拿到输出信息中大模型返回的文本
//        String content = chatResponse.getResult().getOutput().getText();
//        log.info("content: {}", content);
//        return content;
//    }


    /**
     * AI调用MCP服务
     */
    //引入这个类，我们的Spring AI MCP 服务，它在启动的时候，会自动去读取我们刚刚所写的 mcp-servers.json 配置文件
    //从中找到所有的工具，然后自动注册到这个工具提供者类上，我们就可以直接使用它了
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


    //这里我们就要定义一个格式了，定义一个恋爱报告的格式
    //这个record语法可以快速的定义一个类，我们就可以理解为一个类，含有title、suggestion字段
    //和定义方法的方法一样，这样一个类就定义好了
    public record LoveReport(String title, List<String> suggestions){

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
    public Flux<String> gameStreamChat(String message, String chatId){
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


