package com.studyplatform.controller;

import com.studyplatform.dto.gamification.BadgeResponse;
import com.studyplatform.dto.gamification.LevelResponse;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping("/level")
    public ResponseEntity<LevelResponse> getLevel(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(badgeService.getLevel(principal.getId()));
    }

    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponse>> getBadges(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(badgeService.getAllBadgesForUser(principal.getId()));
    }

    @PostMapping("/badges/check")
    public ResponseEntity<Map<String, String>> checkBadges(@CurrentUser UserPrincipal principal) {
        badgeService.checkAndAwardBadges(principal.getUser());
        return ResponseEntity.ok(Map.of("message", "Badge check complete"));
    }
}
