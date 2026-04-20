package com.studyplatform.service;

import com.studyplatform.dto.topic.CreateTopicRequest;
import com.studyplatform.dto.topic.TopicResponse;
import com.studyplatform.entity.Topic;
import com.studyplatform.entity.User;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    @Transactional
    public TopicResponse create(User user, CreateTopicRequest request) {
        if (topicRepository.existsByUserIdAndNameIgnoreCase(user.getId(), request.getName())) {
            throw ApiException.conflict("You already have a topic named '" + request.getName() + "'");
        }

        Topic topic = Topic.builder()
                .user(user)
                .name(request.getName().trim())
                .specificity(request.getSpecificity())
                .build();

        return toResponse(topicRepository.save(topic));
    }

    public List<TopicResponse> listByUser(UUID userId) {
        return topicRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public TopicResponse getById(UUID topicId, UUID userId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> ApiException.notFound("Topic not found"));

        if (!topic.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You do not own this topic");
        }
        return toResponse(topic);
    }

    @Transactional
    public void delete(UUID topicId, UUID userId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> ApiException.notFound("Topic not found"));

        if (!topic.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You do not own this topic");
        }
        topicRepository.delete(topic);
    }

    private TopicResponse toResponse(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .specificity(topic.getSpecificity())
                .createdAt(topic.getCreatedAt())
                .build();
    }
}
