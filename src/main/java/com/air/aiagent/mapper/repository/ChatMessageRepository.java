package com.air.aiagent.mapper.repository;

import com.air.aiagent.domain.entity.ChatMessage;
import com.air.aiagent.domain.entity.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天消息Repository接口
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * 根据用户ID查找消息
     */
    List<ChatMessage> findByChatId(String userId);

    /**
     * 根据用户ID分页查找消息
     */
    Page<ChatMessage> findByChatId(String userId, Pageable pageable);

    /**
     * 根据会话ID查找消息
     */
    List<ChatMessage> findBySessionId(String sessionId);

    // 历史上下文查询

    /**
     * 查询指定时间之前的消息,用于排除刚保存的消息
     */
    List<ChatMessage> findBySessionIdAndTimestampBefore(
            String sessionId,
            LocalDateTime beforeTime);

    /**
     * 查询最近N条消息，按时间排序
     */
    @Query("{'sessionId': ?0}")
    List<ChatMessage> findRecentBySessionId(String sessionId, Pageable pageable);

    /**
     * 获取历史消息，排除最新的N条
     *
     * @param sessionId     会话ID
     * @param limit         返回多少条历史
     * @param excludeLatest 排除最新的几条
     * @return 按时间升序排列的历史消息
     */
    default List<ChatMessage> findHistoryExcludingLatest(
            String sessionId,
            int limit,
            int excludeLatest) {

        List<ChatMessage> allMessages = findBySessionId(sessionId);

        if (allMessages.isEmpty()) {
            return List.of();
        }

        return allMessages.stream()
                // 按时间降序（最新的在前）
                .sorted(Comparator.comparing(ChatMessage::getTimestamp).reversed())
                // 跳过最新的N条
                .skip(excludeLatest)
                // 取前limit条
                .limit(limit)
                // 重新按时间升序（最早的在前，符合对话顺序）
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * 获取会话的最新N条消息（按时间升序）
     */
    default List<ChatMessage> findLatestMessages(String sessionId, int limit) {
        return findBySessionId(sessionId).stream()
                .sorted(Comparator.comparing(ChatMessage::getTimestamp).reversed())
                .limit(limit)
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
    }


    /**
     * 删除会话的所有消息
     * @return 删除的消息数量
     */
    long deleteBySessionId(String sessionId);


    /**
     * 根据会话ID分页查找消息
     */
    Page<ChatMessage> findBySessionId(String sessionId, Pageable pageable);

    /**
     * 根据用户ID和会话ID查找消息
     */
    List<ChatMessage> findByChatIdAndSessionId(String userId, String sessionId);

    /**
     * 根据用户ID和会话ID分页查找消息
     */
    Page<ChatMessage> findByChatIdAndSessionId(String userId, String sessionId, Pageable pageable);

    /**
     * 根据消息类型查找消息
     */
    List<ChatMessage> findByMessageType(MessageType messageType);

    /**
     * 根据是否为AI回复查找消息
     */
    List<ChatMessage> findByIsAiResponse(Boolean isAiResponse);

    /**
     * 根据用户ID和是否为AI回复查找消息
     */
    List<ChatMessage> findByChatIdAndIsAiResponse(String userId, Boolean isAiResponse);

    /**
     * 根据时间范围查找消息
     */
    List<ChatMessage> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据用户ID和时间范围查找消息
     */
    List<ChatMessage> findByChatIdAndTimestampBetween(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据内容模糊查询
     */
    @Query("{'content': {$regex: ?0, $options: 'i'}}")
    List<ChatMessage> findByContentContaining(String content);

    /**
     * 根据用户ID和内容模糊查询
     */
    @Query("{'userId': ?0, 'content': {$regex: ?1, $options: 'i'}}")
    List<ChatMessage> findByChatIdAndContentContaining(String userId, String content);

    /**
     * 统计用户消息数量
     */
    long countByChatId(String userId);

    /**
     * 统计会话消息数量
     */
    long countBySessionId(String sessionId);

    /**
     * 统计用户和会话的消息数量
     */
    long countByChatIdAndSessionId(String userId, String sessionId);

    /**
     * 删除用户的所有消息
     */
    void deleteByChatId(String userId);
}
