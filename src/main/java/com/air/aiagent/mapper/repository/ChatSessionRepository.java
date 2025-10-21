package com.air.aiagent.mapper.repository;

import com.air.aiagent.domain.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天会话Repository接口
 */
@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {

    /**
     * 根据用户ID查找会话
     */
    List<ChatSession> findByChatId(String userId);


    /**
     * 根据用户ID分页查找会话
     */
    Page<ChatSession> findByChatId(String userId, Pageable pageable);


    /**
     * 根据方法名自动推断
     * 查询逻辑：
     * - findFirst: 返回第一条
     * - ByChatId: 根据 userId 查询
     * - OrderByCreatedAtDesc: 按创建时间降序排序（最新的在前）
     *
     * @param userId 用户ID
     * @return 最新的会话（可能为空）
     */
    Optional<ChatSession> findFirstByChatIdOrderByCreatedAtDesc(String userId);


    /**
     * 根据会话ID和用户ID查找会话
     */
    Optional<ChatSession> findByIdAndChatId(String id, String chatId);


    /**
     * 根据 sessionId 和 chatId 删除会话
     * 
     * @return 删除的会话数量（0 表示没有删除任何数据，1 表示删除成功）
     */
    long deleteByIdAndChatId(String id, String chatId);


    /**
     * 统计用户会话数量
     */
    long countByChatId(String userId);
}
