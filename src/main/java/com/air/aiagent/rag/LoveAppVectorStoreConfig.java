package com.air.aiagent.rag;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * @author WyH524
 * @since 2025/8/1 下午3:27
 * 向量数据库配置（初始化基于内存的向量数据库Bean）
 */
//@Configuration
public class LoveAppVectorStoreConfig {
    // 我们等会要创建一个自己的向量数据库，自己的Vector，我们这个Vector中
    // 是要自己加载我们刚刚切分的处理的文档，所以我们自定义一个VectorStore
    // 我们可以写一个配置类，在配置类中去定义一个自己的VectorStore的Bean

    // 我们要引入我们刚刚创建的文档加载器了，因为我们要给向量数据库加载这些文档
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    // 注入自定义的切词器
    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    // 创建一个向量数据库
    // 定义一个Bean，它的返回类型一定是 VectorStore 类型，因为我们要实现这个VectorStore接口，等会才能在我们的大模型，基于这个 VectorStore（向量数据库） 检索内容
    @Bean  // loveAppVectorStore 这个其实就是 Bean 的名字，依赖注入的时候也要用相同的名称
    VectorStore loveAppVectorStore(EmbeddingModel embeddingModel){
        // 这里还有一个注意事项，刚才提到我们想要把文档转换成向量，得要调用一个Embedding模型（嵌入模型），SpringAI为我们提供了 Embedding 模型（嵌入模型）的注入
        // 注入的一定是SpringAI的 EmbeddingModel
        // 有了嵌入大模型了，就可以定义一个简易的基于内存的 VectorStore 存储 SimpleVectorStore
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 调用方法加载文档，这里把 文档 切割成多个 Document 对象，返回 Document 对象列表
        List<Document> list = loveAppDocumentLoader.loadMarkdown();

        // 调用我们自定义的切词器，对 Document 列表进行切词，自主切分
        List<Document> splitCustomizedList = myTokenTextSplitter.splitCustomized(list);

        // 将得到的新的List列表添加到向量数据库中
        // 将 list 中包含的 Document 对象（文档）转换为向量表示，并将其存储到 SimpleVectorStore 向量数据库中
        simpleVectorStore.add(splitCustomizedList);

        // 最后将这个 VectorStore 返回出去，后续可以给大模型去调用
        return simpleVectorStore;
    }
}
