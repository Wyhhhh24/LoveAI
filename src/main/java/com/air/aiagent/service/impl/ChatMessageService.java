package com.air.aiagent.service.impl;

import com.air.aiagent.mapper.repository.ChatMessageRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

}
