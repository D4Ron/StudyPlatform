package com.studyplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyplatform.ai.AiService;
import com.studyplatform.dto.competition.*;
import com.studyplatform.entity.*;
import com.studyplatform.enums.Difficulty;
import com.studyplatform.enums.QuizType;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompetitionService {

    private final CompetitionRepository competitionRepo;
    private final CompetitionEntryRepository entryRepo;
    private final StudyGroupRepository groupRepo;
    private final GroupService groupService;
    private final AiService aiService;
    private final XpService xpService;
    private final ObjectMapper objectMapper;

    @Transactional
    public CompetitionResponse create(User user, CreateCompetitionRequest request) {
        StudyGroup group = null;
        if (request.getGroupId() != null) {
            group = groupRepo.findById(request.getGroupId())
                    .orElseThrow(() -> ApiException.notFound("Group not found"));
            groupService.requireMembership(user.getId(), request.getGroupId());
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw ApiException.badRequest("End time must be after start time");
        }

        // Generate quiz questions for the competition
        Difficulty diff = Difficulty.valueOf(request.getDifficulty().toUpperCase());
        JsonNode questionsJson = aiService.generateQuiz(
                request.getTopic(), diff, QuizType.STANDARD, request.getQuestionCount(), null);

        Competition comp = Competition.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .topic(request.getTopic())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .difficulty(request.getDifficulty())
                .questionCount(request.getQuestionCount())
                .createdBy(user)
                .group(group)
                .active(true)
                .questions(questionsJson.toString())
                .build();

        comp = competitionRepo.save(comp);
        log.info("Competition created: {} by {}", comp.getTitle(), user.getEmail());
        return toResponse(comp, user.getId());
    }

    @Transactional
    public void join(UUID competitionId, User user) {
        Competition comp = competitionRepo.findById(competitionId)
                .orElseThrow(() -> ApiException.notFound("Competition not found"));

        if (!comp.isActive()) throw ApiException.badRequest("This competition is no longer active");
        if (Instant.now().isAfter(comp.getEndTime())) throw ApiException.badRequest("This competition has ended");
        if (entryRepo.existsByCompetitionIdAndUserId(competitionId, user.getId()))
            throw ApiException.conflict("You have already joined this competition");

        if (comp.getGroup() != null)
            groupService.requireMembership(user.getId(), comp.getGroup().getId());

        CompetitionEntry entry = CompetitionEntry.builder()
                .competition(comp).user(user).completed(false).build();
        entryRepo.save(entry);
        log.info("User {} joined competition {}", user.getEmail(), comp.getTitle());
    }

    @Transactional
    public LeaderboardEntry submit(User user, SubmitCompetitionRequest request) {
        Competition comp = competitionRepo.findById(request.getCompetitionId())
                .orElseThrow(() -> ApiException.notFound("Competition not found"));

        if (!comp.isOngoing()) throw ApiException.badRequest("This competition is not currently active");

        CompetitionEntry entry = entryRepo.findByCompetitionIdAndUserId(comp.getId(), user.getId())
                .orElseThrow(() -> ApiException.badRequest("You haven't joined this competition"));

        if (entry.isCompleted()) throw ApiException.badRequest("You have already submitted your answers");

        // Score the answers
        JsonNode questionsNode;
        try {
            JsonNode root = objectMapper.readTree(comp.getQuestions());
            questionsNode = root.has("questions") ? root.get("questions") : root;
        } catch (Exception e) { throw ApiException.badRequest("Invalid competition data"); }

        int correct = 0, total = 0;
        for (var answer : request.getAnswers()) {
            if (answer.getQuestionIndex() >= questionsNode.size()) continue;
            JsonNode q = questionsNode.get(answer.getQuestionIndex());
            int points = q.has("points") ? q.get("points").asInt() : 10;
            total += points;
            String correctAns = q.has("correctAnswer") ? q.get("correctAnswer").asText() : "";
            if (correctAns.equalsIgnoreCase(answer.getSelectedAnswer())) correct++;
        }

        int earnedPoints = correct * (total > 0 ? total / questionsNode.size() : 10);
        entry.setScore(earnedPoints);
        entry.setTotalPoints(total);
        entry.setCorrectCount(correct);
        entry.setTotalQuestions(questionsNode.size());
        entry.setTimeTakenSeconds(request.getTimeTakenSeconds());
        entry.setCompleted(true);
        entry.setCompletedAt(Instant.now());
        entryRepo.save(entry);

        xpService.awardXp(user, null, 40, "COMPETITION_COMPLETED", comp.getId().toString());
        if (correct == questionsNode.size())
            xpService.awardXp(user, null, 20, "COMPETITION_PERFECT", comp.getId().toString());

        log.info("Competition submission: {} scored {}/{}", user.getEmail(), earnedPoints, total);
        return toLeaderboardEntry(entry, 0);
    }

    public List<CompetitionResponse> listUpcoming(UUID userId) {
        return competitionRepo.findUpcoming(Instant.now()).stream()
                .map(c -> toResponse(c, userId)).toList();
    }

    public List<CompetitionResponse> listOngoing(UUID userId) {
        return competitionRepo.findOngoing(Instant.now()).stream()
                .map(c -> toResponse(c, userId)).toList();
    }

    public List<CompetitionResponse> listByGroup(UUID groupId, UUID userId) {
        groupService.requireMembership(userId, groupId);
        return competitionRepo.findByGroupIdOrderByStartTimeDesc(groupId).stream()
                .map(c -> toResponse(c, userId)).toList();
    }

    public CompetitionResponse getById(UUID id, UUID userId) {
        Competition comp = competitionRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Competition not found"));
        CompetitionResponse resp = toResponse(comp, userId);
        // Include questions only if competition is ongoing and user has joined
        if (comp.isOngoing() && entryRepo.existsByCompetitionIdAndUserId(id, userId)) {
            try { resp.setQuestions(objectMapper.readTree(comp.getQuestions())); } catch (Exception ignored) {}
        }
        return resp;
    }

    public List<LeaderboardEntry> getLeaderboard(UUID competitionId) {
        AtomicInteger rank = new AtomicInteger(1);
        return entryRepo.findByCompetitionIdAndCompletedTrueOrderByScoreDesc(competitionId).stream()
                .map(e -> toLeaderboardEntry(e, rank.getAndIncrement()))
                .toList();
    }

    private CompetitionResponse toResponse(Competition c, UUID userId) {
        return CompetitionResponse.builder()
                .id(c.getId()).title(c.getTitle()).description(c.getDescription())
                .topic(c.getTopic()).difficulty(c.getDifficulty()).questionCount(c.getQuestionCount())
                .startTime(c.getStartTime()).endTime(c.getEndTime())
                .active(c.isActive()).ongoing(c.isOngoing())
                .participantCount(entryRepo.countByCompetitionId(c.getId()))
                .createdByName(c.getCreatedBy() != null ? c.getCreatedBy().getFullName() : null)
                .groupId(c.getGroup() != null ? c.getGroup().getId() : null)
                .groupName(c.getGroup() != null ? c.getGroup().getName() : null)
                .alreadyJoined(userId != null && entryRepo.existsByCompetitionIdAndUserId(c.getId(), userId))
                .createdAt(c.getCreatedAt()).build();
    }

    private LeaderboardEntry toLeaderboardEntry(CompetitionEntry e, int rank) {
        double pct = e.getTotalPoints() > 0 ? (e.getScore() * 100.0 / e.getTotalPoints()) : 0;
        return LeaderboardEntry.builder()
                .rank(rank).userId(e.getUser().getId())
                .firstName(e.getUser().getFirstName()).lastName(e.getUser().getLastName())
                .score(e.getScore()).totalPoints(e.getTotalPoints())
                .correctCount(e.getCorrectCount()).totalQuestions(e.getTotalQuestions())
                .percentageScore(Math.round(pct * 10.0) / 10.0)
                .timeTakenSeconds(e.getTimeTakenSeconds()).completed(e.isCompleted()).build();
    }
}
