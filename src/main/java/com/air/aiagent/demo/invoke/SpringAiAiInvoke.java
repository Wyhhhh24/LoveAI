package com.air.aiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
/**
 * @author WyH524
 * @since 2025/6/16 下午4:35
 * SpringAI 框架调用大模型
 * 我们怎么去启动一个spring项目，可以单次去测试这个ai调用是否成功呢？
 * 怎么单次测试不用写接口测试呢？
 * 可以实现一个CommandLineRunner接口，实现一个单次执行的方法，项目启动时，spring会扫描这个Bean，发现它有实现CommandLineRunner接口
 * 就会自动注入依赖，并且执行run方法了
 */
@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    /**
     * 写好配置之后，springboot会自动为我们注入一个ai大模型的调用
     * @ Resource
     * 这个注解是通过名称来进行优先注入的，名称找不到才会通过类型进行注入
     * 由于我们的springAi可以同时引入多种不同的 ChatModel ， ChatModel是一个接口，有好多种不同的ChatModel，dashscopeChatModel只是其中一个
     * 所以这里，我注入的时候一定要是dashscopeChatModel 这个名称
     */
    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("你是哪一个大模型？"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }
    /**
     * 用springAi框架就这几行代码，想要调用什么大模型，直接配置好，启动项目之后，就自动给注入了，可以直接使用这个大模型对象去发起请求了
     */
}
