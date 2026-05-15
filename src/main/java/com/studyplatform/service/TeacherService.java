package com.studyplatform.service;

import com.studyplatform.dto.teacher.GroupOverview;
import com.studyplatform.dto.teacher.StudentOverview;
import com.studyplatform.entity.GroupMember;
import com.studyplatform.entity.User;
import com.studyplatform.enums.AccountType;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final GroupMemberRepository memberRepository;
    private final StudyGroupRepository groupRepository;
    private final XpLogRepository xpLogRepository;
    private final StudyGuideRepository guideRepository;
    private final QuizAttemptRepository attemptRepository;
    private final BadgeService badgeService;

    public void requireTeacher(User user) {
        if (user.getAccountType() != AccountType.TEACHER) {
            throw ApiException.forbidden("This endpoint is restricted to teachers");
        }
    }

    public List<GroupOverview> getMyGroups(User teacher) {
        requireTeacher(teacher);
        return memberRepository.findByUserId(teacher.getId()).stream()
                .map(gm -> buildGroupOverview(gm.getGroup().getId()))
                .toList();
    }

    public GroupOverview getGroupDetail(UUID groupId, User teacher) {
        requireTeacher(teacher);
        memberRepository.findByUserIdAndGroupId(teacher.getId(), groupId)
                .orElseThrow(() -> ApiException.forbidden("You are not a member of this group"));
        return buildGroupOverview(groupId);
    }

    public List<StudentOverview> identifyStrugglingStudents(UUID groupId, User teacher) {
        requireTeacher(teacher);
        memberRepository.findByUserIdAndGroupId(teacher.getId(), groupId)
                .orElseThrow(() -> ApiException.forbidden("You are not a member of this group"));

        return memberRepository.findByGroupId(groupId).stream()
                .map(gm -> buildStudentOverview(gm.getUser()))
                .filter(s -> {
                    if (s.getAverageQuizScore() != null && s.getAverageQuizScore() < 60) return true;
                    if (s.getTotalXp() < 50 && s.getGuidesCompleted() == 0) return true;
                    return false;
                })
                .toList();
    }

    private GroupOverview buildGroupOverview(UUID groupId) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> ApiException.notFound("Group not found"));
        var members = memberRepository.findByGroupId(groupId);

        List<StudentOverview> students = members.stream()
                .map(gm -> buildStudentOverview(gm.getUser()))
                .toList();

        int totalXp = students.stream().mapToInt(StudentOverview::getTotalXp).sum();
        Double avgScore = students.stream()
                .filter(s -> s.getAverageQuizScore() != null)
                .mapToDouble(StudentOverview::getAverageQuizScore)
                .average().orElse(0.0);

        return GroupOverview.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .memberCount(members.size())
                .totalGroupXp(totalXp)
                .averageQuizScore(Math.round(avgScore * 10.0) / 10.0)
                .students(students)
                .createdAt(group.getCreatedAt())
                .build();
    }

    private StudentOverview buildStudentOverview(User user) {
        int xp = xpLogRepository.getTotalXpByUserId(user.getId());
        long guides = guideRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size();
        long quizzes = attemptRepository.countByUserId(user.getId());
        Double avgScore = attemptRepository.findAverageScoreByUserId(user.getId());
        int level = badgeService.getLevel(user.getId()).getLevel();

        String status = "ACTIVE";
        if (avgScore != null && avgScore < 60) status = "STRUGGLING";
        else if (xp == 0 && guides == 0) status = "INACTIVE";

        return StudentOverview.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .totalXp(xp)
                .level(level)
                .guidesCompleted((int) guides)
                .quizzesTaken((int) quizzes)
                .averageQuizScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : null)
                .status(status)
                .build();
    }
}
