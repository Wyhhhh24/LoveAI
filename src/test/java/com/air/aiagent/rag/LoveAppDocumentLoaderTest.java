package com.air.aiagent.rag;
import com.air.aiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.UUID;

@SpringBootTest
class LoveAppDocumentLoaderTest {


    @Resource
    private LoveApp loveApp;

    @Resource
    LoveAppDocumentLoader loveAppDocumentLoader;


    @Test
    void loadMarkdown() {
        loveAppDocumentLoader.loadMarkdown();
    }

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        //三轮对话
        // 第一轮
        String message = "你好，我是程序员鱼皮";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想让另一半（编程导航）更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        // Assertions.assertNotNull(answer)是一个 单元测试断言 ，用于验证变量 answer 的值是否 不为 null。
        // 如果 answer 是 null，测试会立即失败并抛出异常；否则测试继续执行
    }

//    @Test
//    void doChatWithReport() {
//        String chatId = UUID.randomUUID().toString();
//        String message = "你好，我是程序员鱼皮,让另一半（编程导航）更爱我，但我不知道怎么做";
//        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
//        Assertions.assertNotNull(loveReport);
//    }

//    @Test
//    void doChatWithRag() {
//        String chatId = UUID.randomUUID().toString();
//        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
//        loveApp.doChatWithRag(message, chatId)
//                .doOnNext(chunk -> System.out.println("[实时] " + chunk))
//                .doOnComplete(() -> System.out.println("=== 流结束 ==="))
//                .blockLast(); // 阻塞直到流结束
//    }



//    private void testMessage(String message) {
//        String chatId = UUID.randomUUID().toString();
//        String answer = loveApp.doChatWithRagAndTools(message, chatId);
//        Assertions.assertNotNull(answer);
//    }

//    @Test
//    void doChatWithTools() {
//        // 测试联网搜索问题的答案
//        //testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");
//
//        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，看看其它网站中的情侣是怎么解决矛盾的？");
//
//        // 测试资源下载：图片下载
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
//
//        // 测试终端操作：执行代码
//        //testMessage("执行 Python3 脚本来生成数据分析报告");
//
//        // 测试文件操作：保存用户档案
//        testMessage("保存我的恋爱档案为文件");
//
//        // 测试 PDF 生成
//        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
//    }

//    @Test
//    void doChatWithMCP() {
//        String chatId = UUID.randomUUID().toString();
//        //测试地图MCP
//        String message = "我的女朋友住在广东省东莞市虎门镇，请推荐一下该地区的公园公园";
//        String answer = loveApp.doChatWithMCP(message, chatId);
//        Assertions.assertNotNull(answer);
//    }


}