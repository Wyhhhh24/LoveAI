package com.air.aiagent.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * 当返回 JSON 数据时，如果对象中的 Long 类型数值过大（超过 JavaScript 的 Number 类型安全范围），会导致前端精度丢失
 */
@Configuration
public class JacksonConfig {

    /**
     * 在 Spring Boot 中，@Primary 注解的作用是解决依赖注入时的歧义性，它标记在某个 Bean 定义上
     * 表示当存在多个相同类型的 Bean 时，优先选择被标记为 @Primary 的 Bean。
     * Spring Boot 自动配置会创建一个默认的 ObjectMapper Bean（用于 JSON 序列化/反序列化）。
     * 如果直接定义新的 ObjectMapper Bean，会导致容器中存在两个同类型 Bean，引发注入冲突。
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        // 方案1：将所有 Long 类型转为String
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);
        return objectMapper;
    }
}