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
     * 根据用户ID和活跃状态查找会话
     */
    List<ChatSession> findByChatIdAndIsActive(String userId, Boolean isActive);

    /**
     * 根据用户ID和活跃状态分页查找会话
     */
    Page<ChatSession> findByChatIdAndIsActive(String userId, Boolean isActive, Pageable pageable);

    /**
     * 根据会话名称模糊查询
     */
    @Query("{'sessionName': {$regex: ?0, $options: 'i'}}")
    List<ChatSession> findBySessionNameContaining(String sessionName);

    /**
     * 根据用户ID和会话名称模糊查询
     */
    @Query("{'userId': ?0, 'sessionName': {$regex: ?1, $options: 'i'}}")
    List<ChatSession> findByChatIdAndSessionNameContaining(String userId, String sessionName);

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
     * 根据创建时间范围查找会话
     */
    List<ChatSession> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据用户ID和创建时间范围查找会话
     */
    List<ChatSession> findByChatIdAndCreatedAtBetween(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据更新时间范围查找会话
     */
    List<ChatSession> findByUpdatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据用户ID和更新时间范围查找会话
     */
    List<ChatSession> findByChatIdAndUpdatedAtBetween(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户会话数量
     */
    long countByChatId(String userId);

    /**
     * 统计活跃会话数量
     */
    long countByIsActive(Boolean isActive);

    /**
     * 统计用户活跃会话数量
     */
    long countByChatIdAndIsActive(String userId, Boolean isActive);

    /**
     * 删除用户的所有会话
     */
    void deleteByChatId(String userId);

    /**
     * 删除非活跃会话
     */
    void deleteByIsActive(Boolean isActive);

    /**
     * 删除用户非活跃会话
     */
    void deleteByChatIdAndIsActive(String userId, Boolean isActive);
}
