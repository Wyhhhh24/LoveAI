package com.air.aiagent.rag;
import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * @author WyH524
 * @since 2025/8/11 下午8:45
 */
@Configuration
public class PgVectorVectorStoreConfig {

    // 引入文档加载器
    @Resource
    private LoveAppDocumentLoader loader;

    // 初始化Bean
    @Bean
    public VectorStore pgVectorVectorStore(@Qualifier("pgJdbcTemplate") JdbcTemplate pgJdbcTemplate,
            EmbeddingModel dashscopeEmbeddingModel) {
        // 最终得到一个PgVectorStore对象
        PgVectorStore pgVectorStore = PgVectorStore.builder(pgJdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536) // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE) // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW) // Optional: defaults to HNSW
                .initializeSchema(true) // Optional: defaults to false 自动初始化建表，这里设置了 true ，但是它并不会建表，得要在虚拟机中手动建表
                .schemaName("public") // Optional: defaults to "public" 向量数据库的名称
                .vectorTableName("vector_store") // Optional: defaults to "vector_store" 表的名称
                .maxDocumentBatchSize(10000) // Optional: defaults to 10000 最大批量插入的文档数
                .build();
        return pgVectorStore;
    }
}
