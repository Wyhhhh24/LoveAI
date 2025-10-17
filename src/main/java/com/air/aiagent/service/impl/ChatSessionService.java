package com.air.aiagent.service.impl;// ChatSessionService.java

import com.air.aiagent.domain.entity.ChatSession;
import com.air.aiagent.domain.vo.ChatSessionVO;
import com.air.aiagent.mapper.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;

    private final MongoTemplate mongoTemplate;

    /**
     * 更新会话名称
     * 
     * @param id          会话ID
     * @param sessionName 新的会话名称
     * @return 是否更新成功
     */
    public boolean updateSessionName(String id, String sessionName) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update()
                .set("session_name", sessionName)
                .set("updated_at", LocalDateTime.now()); // 同时更新时间

        var result = mongoTemplate.updateFirst(query, update, ChatSession.class);

        boolean success = result.getModifiedCount() > 0;

        if (success) {
            log.info("会话名称已更新，id={}, newName={}", id, sessionName);
        } else {
            log.warn("会话不存在或名称未改变，id={}", id);
        }
        return success;
    }

    /**
     * 获取用户最新的会话
     */
    public ChatSession getLatestSessionByUserId(String userId) {
        Optional<ChatSession> result = chatSessionRepository.findFirstByChatIdOrderByCreatedAtDesc(userId);
        return result.orElse(new ChatSession());
    }

    /**
     * 更新会话名称（带用户验证）
     * 
     * @param id          会话ID
     * @param userId      用户ID（防止越权）
     * @param sessionName 新的会话名称
     * @return 是否更新成功
     */
    public boolean updateSessionName(String id, String userId, String sessionName) {
        Query query = new Query(
                Criteria.where("_id").is(id)
                        .and("chat_id").is(userId) // 验证所有权
        );

        Update update = new Update()
                .set("session_name", sessionName)
                .set("updated_at", LocalDateTime.now());

        var result = mongoTemplate.updateFirst(query, update, ChatSession.class);

        boolean success = result.getModifiedCount() > 0;

        if (success) {
            log.info("会话名称已更新，id={}, userId={}, newName={}",
                    id, userId, sessionName);
        } else {
            log.warn("会话不存在或无权限，id={}, userId={}", id, userId);
        }
        return success;
    }

    /**
     * 根据 chatId 以及 sessionId 查询对应的会话
     */
    public ChatSession getSessionByChatIdAndSessionId(String chatId, String sessionId) {
        Optional<ChatSession> result = chatSessionRepository.findByIdAndChatId(sessionId, chatId);
        return result.orElse(new ChatSession());
    }

    /**
     * 根据 sessionId 以及 chatId 删除对应的会话
     * 
     * @return 删除的会话数量（0 表示未删除，1 表示删除成功）
     */
    public long deleteSession(String id, String chatId) {
        return chatSessionRepository.deleteByIdAndChatId(id, chatId);
    }

    /**
     * 更新会话状态
     */
    public boolean updateSessionActive(String id, String userId, Boolean isActive) {
        Query query = new Query(
                Criteria.where("_id").is(id)
                        .and("chat_id").is(userId));

        Update update = new Update()
                .set("is_active", isActive)
                .set("updated_at", LocalDateTime.now());

        var result = mongoTemplate.updateFirst(query, update, ChatSession.class);

        return result.getModifiedCount() > 0;
    }

    /**
     * 增加消息计数
     */
    public void incrementMessageCount(String sessionId) {
        Query query = new Query(Criteria.where("_id").is(sessionId));
        Update update = new Update()
                .inc("message_count", 1) // 原子递增
                .set("updated_at", LocalDateTime.now());

        mongoTemplate.updateFirst(query, update, ChatSession.class);
    }

    /**
     * 批量更新会话状态
     */
    public long batchUpdateActive(String userId, Boolean isActive) {
        Query query = new Query(Criteria.where("chat_id").is(userId));
        Update update = new Update()
                .set("is_active", isActive)
                .set("updated_at", LocalDateTime.now());

        var result = mongoTemplate.updateMulti(query, update, ChatSession.class);

        return result.getModifiedCount();
    }

    // ===== 其他CRUD方法 =====

    public ChatSession createSession(String userId, String sessionName) {
        ChatSession session = ChatSession.builder()
                .chatId(userId)
                .sessionName(sessionName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .messageCount(0)
                .build();

        return chatSessionRepository.save(session);
    }

    public List<ChatSession> getUserActiveSessions(String userId) {
        return chatSessionRepository.findByChatIdAndIsActive(userId, true);
    }
}