package com.studyplatform.service;

import com.studyplatform.dto.note.CreateNoteRequest;
import com.studyplatform.dto.note.NoteResponse;
import com.studyplatform.dto.note.UpdateNoteRequest;
import com.studyplatform.entity.Note;
import com.studyplatform.entity.StudyGroup;
import com.studyplatform.entity.User;
import com.studyplatform.exception.ApiException;
import com.studyplatform.repository.NoteRepository;
import com.studyplatform.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final StudyGroupRepository groupRepository;
    private final GroupService groupService;

    @Transactional
    public NoteResponse create(User user, CreateNoteRequest request) {
        StudyGroup group = null;
        if (request.getGroupId() != null) {
            group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> ApiException.notFound("Group not found"));
            groupService.requireMembership(user.getId(), request.getGroupId());
        }

        Note note = Note.builder()
                .author(user)
                .group(group)
                .title(request.getTitle())
                .content(request.getContent())
                .sharedWithGroup(request.isSharedWithGroup())
                .build();

        return toResponse(noteRepository.save(note));
    }

    @Transactional
    public NoteResponse update(UUID noteId, UUID userId, UpdateNoteRequest request) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> ApiException.notFound("Note not found"));
        if (!note.getAuthor().getId().equals(userId)) {
            throw ApiException.forbidden("Only the author can edit this note");
        }

        if (request.getTitle() != null) note.setTitle(request.getTitle());
        if (request.getContent() != null) note.setContent(request.getContent());
        if (request.getSharedWithGroup() != null) note.setSharedWithGroup(request.getSharedWithGroup());

        return toResponse(noteRepository.save(note));
    }

    public List<NoteResponse> listMyNotes(UUID userId) {
        return noteRepository.findByAuthorIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    public List<NoteResponse> listGroupNotes(UUID groupId, UUID userId) {
        groupService.requireMembership(userId, groupId);
        return noteRepository.findByGroupIdAndSharedWithGroupTrueOrderByUpdatedAtDesc(groupId).stream()
                .map(this::toResponse).toList();
    }

    public NoteResponse getById(UUID noteId, UUID userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> ApiException.notFound("Note not found"));
        if (!note.getAuthor().getId().equals(userId) &&
                (note.getGroup() == null || !note.isSharedWithGroup())) {
            throw ApiException.forbidden("You do not have access to this note");
        }
        if (note.getGroup() != null && note.isSharedWithGroup()) {
            groupService.requireMembership(userId, note.getGroup().getId());
        }
        return toResponse(note);
    }

    @Transactional
    public void delete(UUID noteId, UUID userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> ApiException.notFound("Note not found"));
        if (!note.getAuthor().getId().equals(userId)) {
            throw ApiException.forbidden("Only the author can delete this note");
        }
        noteRepository.delete(note);
    }

    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .authorName(note.getAuthor().getFullName())
                .groupId(note.getGroup() != null ? note.getGroup().getId() : null)
                .groupName(note.getGroup() != null ? note.getGroup().getName() : null)
                .sharedWithGroup(note.isSharedWithGroup())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
