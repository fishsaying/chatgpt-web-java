package com.hncboy.chatgpt.front.api.storage;

import cn.hutool.core.util.StrUtil;
import com.hncboy.chatgpt.base.domain.entity.ChatMessageDO;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author hncboy
 * @date 2023/3/25 22:52
 * ApiKey 数据库数据存储
 */
@Component
public class ApiKeyDatabaseDataStorage extends AbstractDatabaseDataStorage {

    @Override
    public void onFirstMessage(ChatMessageStorage chatMessageStorage) {
        // 第一条消息手动生成消息 id 和对话 id
        ChatMessageDO answerChatMessageDO = chatMessageStorage.getAnswerChatMessageDO();
        answerChatMessageDO.setMessageId(UUID.randomUUID().toString());
        answerChatMessageDO.setConversationId(UUID.randomUUID().toString());
    }

    @Override
    void onLastMessage(ChatMessageStorage chatMessageStorage) {
        populateMessageUsageToken(chatMessageStorage);
    }

    @Override
    void onErrorMessage(ChatMessageStorage chatMessageStorage) {
        populateMessageUsageToken(chatMessageStorage);
    }

    /**
     * 填充消息使用 Token 数量
     *
     * @param chatMessageStorage 聊天消息数据存储
     */
    private void populateMessageUsageToken(ChatMessageStorage chatMessageStorage) {
        // 获取模型
        ChatMessageDO questionChatMessageDO = chatMessageStorage.getQuestionChatMessageDO();
        String modelName = questionChatMessageDO.getModelName();

        // 获取问题消耗的 tokens
        int promptTokens = TikTokensUtil.tokens(modelName, questionChatMessageDO.getContent());

        // 获取回答消耗的 tokens
        ChatMessageDO answerChatMessageDO = chatMessageStorage.getAnswerChatMessageDO();
        String answerContent = answerChatMessageDO.getContent();
        int completionTokens = StrUtil.isEmpty(answerContent) ? 0 : TikTokensUtil.tokens(modelName, answerContent);

        // 填充使用情况
        int totalTokens = promptTokens + completionTokens;
        answerChatMessageDO.setPromptTokens(promptTokens);
        answerChatMessageDO.setCompletionTokens(completionTokens);
        answerChatMessageDO.setTotalTokens(totalTokens);

        questionChatMessageDO.setPromptTokens(promptTokens);
        questionChatMessageDO.setCompletionTokens(completionTokens);
        questionChatMessageDO.setTotalTokens(totalTokens);
    }
}
