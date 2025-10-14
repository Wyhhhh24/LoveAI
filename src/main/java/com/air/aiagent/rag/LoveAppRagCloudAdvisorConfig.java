package com.air.aiagent.rag;


import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author WyH524
 * @since 2025/8/1 下午8:49
 * 自定义基于阿里云知识库云服务的 RAG 增强 Advisor
 */
@Configuration
@Slf4j
public class LoveAppRagCloudAdvisorConfig {
    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    @Bean
    public Advisor loveAppRagCloudAdvisor() {
        //得到DashScopeApi
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);

        //调用阿里云的知识库的名称
        final String KNOWLEDGE_INDEX = "air恋爱大师";

        //接下来就可以创建 DocumentRetriever 文档检索器,参数为 DashScopeApi 、配置属性对象（可以自己定义检索的配置的方式，比如知识库的名称）
        DashScopeDocumentRetriever dashScopeDocumentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());

        //最后我们再创建这个 RetrievalAugmentationAdvisor 专门用于文档检索增强的增强器，里面运用了上面创建的 文档检索器
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(dashScopeDocumentRetriever)  //运用这个文档检索器
                .build();

        //将这个 RetrievalAugmentationAdvisor 添加到容器中，这样我们就可以调用这个Advisor
        return retrievalAugmentationAdvisor;
    }
}




