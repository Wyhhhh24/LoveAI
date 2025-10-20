package com.air.aiagent.service.impl;

import com.air.aiagent.domain.entity.ChatMessage;
import com.air.aiagent.mapper.repository.ChatMessageRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author WyH524
 * @since 2025/10/16 11:30
 */
@Slf4j
@Service
public class ChatMessageService {

    @Resource
    private ChatMessageRepository chatMessageRepository;

    /**
     * 根据 sessionId 删除该 session 中所有的对话
     * 
     * @return 删除的消息数量
     */
    public long deleteBySessionId(String sessionId) {
        return chatMessageRepository.deleteBySessionId(sessionId);
    }


    /**
     * 保存消息到数据库
     */
    public void save(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }


    /**
     * 获取历史上下文，可排除最新的几条信息
     * sessionId 会话 Id
     * limit 最多返回多少条历史记录
     * excludeLatest 要排除的最新的几条记录
     */
    public List<ChatMessage> findHistoryExcludingLatest(String sessionId,int limit, int excludeLatest){
        return chatMessageRepository
                .findHistoryExcludingLatest(sessionId, limit, excludeLatest);
    }
}
