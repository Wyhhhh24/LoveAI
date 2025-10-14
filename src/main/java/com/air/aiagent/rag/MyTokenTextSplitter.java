package com.air.aiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

//自定义基于 token 的切词器（切割效果不好）
@Component
class MyTokenTextSplitter {

    //无参构造
    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        // 使用默认的切割器
        return splitter.apply(documents);
    }

    //有参构造
    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(250, 100, 10, 5000, true);
        // 将读取到的 MarkDown 文档转换成 List<Document> 对象之后，然后再使用这个切词器进行切割，这些是对应的配置参数
        // defaultChunkSize：每个文本块的目标大小（以标记为单位）（默认：800）
        // minChunkSizeChars：每个文本块的最小大小（以字符为单位）（默认值：350）
        // minChunkLengthToEmbed：要包含的块的最小长度（默认值：5）
        // maxNumChunks：从文本生成的最大块数（默认：10000）
        // keepSeparator：是否在块中保留分隔符（如换行符）（默认值：true）

        // defaultChunkSize  设定每个文本块的目标Token数量，这是分割的主要依据
        // 如果设置为500，分割器会努力将文本切成接近500个 Token 的块，也就是如果一个 Document 有 2000 个token ，就会被切割

        // minChunkSizeChars  每个块的最小字符数，如果一个潜在的切割点导致块的大小低于此值，分割器会尝试寻找更后的切割点以维持块的规模  有助于避免产生过于琐碎、可能无意义的微小文本块

        // chunkOverlap 块重叠大小。这是保证语义连贯性的关键参数，但请注意在您提供的代码片段中未直接出现，通常由其他参数（如keepSeparator）或内部逻辑控制
        // 例如，如果重叠为50个字符，上一个块的结尾部分会与下一个块的开头部分重复，确保处于句子或段落边界的信息不会完全被割裂

        // keepSeparator 决定是否在块中保留用于分割的标点符号（如句号、换行符）。 设置为 true可以更好地保持原始文本的格式和停顿感。
        return splitter.apply(documents);
        // 这里我们表面是进行了切割，其实每一个 List<Document> 都是不需要被切割的，因为我们自定义的 md 文档 已经很优化了

        // 举例子：
        // 假设您有一个Markdown文件 chapter.md，内容被 MarkdownDocumentReader（配置了水平分割）解析成了 2 个 Document：
        // Document1: 文本内容约1500个Token。
        // Document2: 文本内容约200个Token。
        // 如果您使用默认的 TokenTextSplitter（chunkSize = 800）进行处理：
        // Document1 会被切割成大约2个新的 Document（例如，一个800Token，一个700Token）。
        // Document2 因为很短，可能保持不变。
        // 最终，您的 List<Document>将从最初的 2 个对象，变为处理后的 3 个对象。每个新 Document都会保留原始 Document1的元数据（如 filename: "chapter.md"）。
    }
}
