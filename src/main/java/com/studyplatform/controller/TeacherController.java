package com.studyplatform.controller;

import com.studyplatform.dto.teacher.GroupOverview;
import com.studyplatform.dto.teacher.StudentOverview;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/groups")
    public ResponseEntity<List<GroupOverview>> getMyGroups(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(teacherService.getMyGroups(principal.getUser()));
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<GroupOverview> getGroupDetail(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(teacherService.getGroupDetail(groupId, principal.getUser()));
    }

    @GetMapping("/groups/{groupId}/struggling")
    public ResponseEntity<List<StudentOverview>> identifyStruggling(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(teacherService.identifyStrugglingStudents(groupId, principal.getUser()));
    }
}
