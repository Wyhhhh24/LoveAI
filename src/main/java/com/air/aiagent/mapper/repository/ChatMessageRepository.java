package com.air.aiagent.mapper.repository;

import com.air.aiagent.domain.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
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
     * 统计用户消息数量
     */
    long countByChatId(String userId);


    /**
     * 统计会话消息数量
     */
    long countBySessionId(String sessionId);


    /**
     * 统计用户的某一个会话的消息数量
     */
    long countByChatIdAndSessionId(String userId, String sessionId);


    /**
     * 删除用户的所有消息
     */
    void deleteByChatId(String userId);
}
