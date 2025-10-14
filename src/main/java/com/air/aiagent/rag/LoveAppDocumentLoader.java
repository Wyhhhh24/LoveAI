package com.air.aiagent.rag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WyH524
 * @since 2025/8/1 上午11:11
 * MarkDown文档加载器
 */
@Slf4j
@Component
public class LoveAppDocumentLoader {

    // 我们要去本地的 resources 目录下去读取这些文档
    // 我们可以用 Spring 内置的资源解析类，这个资源解析类我们可以直接用 Spring 进行注入，构造方法的方式注入
    // 可以通过这个东西，快速的加载多个文档，因为我们不只是一篇文档，官方文档中的代码是读一篇文档的，这里我们换一种
    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇MarkDown文档
     */
    public List<Document> loadMarkdown() {
        // 总的 List<Document> ，每一个 MarkDown 文件对应的 List<Document> 都添加到这个总的 List<Document> 中
        // 所有文档的总集合
        List<Document> allDocumentList = new ArrayList<>();

        try {
            // 加载了之后，得到一个 Resource 目录，就可以用我们官方提供的示例代码
            // 遍历数组去加载单篇 MarkDown 文档到我们的内存中然后转化成 List<Document> 这样对象
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
                                  //指向的就是 resource 目录下的 document 目录下的所有 MarkDown 文件

            // 加载所有的MarkDown资源，每一个 Resource 对象对应一个文件
            for (Resource resource : resources) {
                String filename = resource.getFilename(); // 获取文件名
                // 为什么要得到文件名呢，后续我们可以基于这个文件名，去做一些处理

                // withAdditionalMetadata ：
                // 这个方法允许我们在解析文档的过程中去设置某篇文档的信息到 Document 对象中
                // 这里我们就可以把这个文件名去作为额外信息保存到我们的 Document 中
                // 这样我们如果要检索的话，不仅可以按照相似度去检索，还可以按照文件名或者按照某些特定的元信息进行检索，相当于打了一个标签

                // 这个MarkdownDocumentReaderConfig 就是指定了怎么样去读取 markdown 文件，可以设置一些配置
                // 通过构造器你可以指定各种各样的选项，详细看官方文档
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        // 将Markdown中的水平规则 ---作为文档分割符
                        .withHorizontalRuleCreateDocument(true) // 读取器会将 Markdown 文件中用水平分隔线 (---) 隔开的不同部分，解析为多个独立的 Document对象
                        // 控制代码块（\\\\...\\\\\）是与周围文本合并还是独立成文
                        .withIncludeCodeBlock(false) // false：在构建知识库时，将代码和解释说明分离，避免代码干扰文本的语义检索 true：需要保持代码与上下文的完整性时使用。
                        // 控制引用块（>...）是与周围文本合并还是独立成文
                        .withIncludeBlockquote(false) // false：将核心内容与引用的外部内容分离，确保检索结果直接相关核心观点
                        // 为所有生成的 Document对象添加统一的自定义元数据（键值对）
                        .withAdditionalMetadata("filename", filename) // 这里为每一个 Document 添加一些额外的信息
                        .build();

                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                List<Document> documents = reader.get();

                //将得到的 List 都添加到总的List中
                allDocumentList.addAll(documents);
            }
        } catch (IOException e) {
            log.error("MarkDown 文档加载失败",e);
        }
        // 返回总的 List<Document>
        return allDocumentList;
    }
}















