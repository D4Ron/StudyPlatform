package com.studyplatform.controller;

import com.studyplatform.dto.note.CreateNoteRequest;
import com.studyplatform.dto.note.NoteResponse;
import com.studyplatform.dto.note.UpdateNoteRequest;
import com.studyplatform.security.CurrentUser;
import com.studyplatform.security.UserPrincipal;
import com.studyplatform.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteResponse> create(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody CreateNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noteService.create(principal.getUser(), request));
    }

    @PatchMapping("/{noteId}")
    public ResponseEntity<NoteResponse> update(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID noteId,
            @RequestBody UpdateNoteRequest request) {
        return ResponseEntity.ok(noteService.update(noteId, principal.getId(), request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<NoteResponse>> listMine(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(noteService.listMyNotes(principal.getId()));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<NoteResponse>> listGroupNotes(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(noteService.listGroupNotes(groupId, principal.getId()));
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteResponse> getById(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID noteId) {
        return ResponseEntity.ok(noteService.getById(noteId, principal.getId()));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(
            @CurrentUser UserPrincipal principal,
            @PathVariable UUID noteId) {
        noteService.delete(noteId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
