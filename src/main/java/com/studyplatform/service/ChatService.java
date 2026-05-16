package com.studyplatform.service;

import com.studyplatform.dto.chat.ChatMessageResponse;
import com.studyplatform.dto.chat.SendMessageRequest;
import com.studyplatform.entity.ChatMessage;
import com.studyplatform.entity.StudyGroup;
import com.studyplatform.entity.User;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.ChatMessageRepository;
import com.studyplatform.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatRepository;
    private final StudyGroupRepository groupRepository;
    private final GroupService groupService;

    @Transactional
    public ChatMessageResponse send(User sender, SendMessageRequest request) {
        StudyGroup group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> ApiException.notFound("Group not found"));
        groupService.requireMembership(sender.getId(), request.getGroupId());

        ChatMessage message = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .content(request.getContent())
                .build();

        return toResponse(chatRepository.save(message));
    }

    public List<ChatMessageResponse> getHistory(UUID groupId, UUID userId, int limit) {
        groupService.requireMembership(userId, groupId);
        return chatRepository.findByGroupIdOrderBySentAtDesc(groupId, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ChatMessageResponse toResponse(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .groupId(msg.getGroup().getId())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getFullName())
                .content(msg.getContent())
                .sentAt(msg.getSentAt())
                .build();
    }
}
