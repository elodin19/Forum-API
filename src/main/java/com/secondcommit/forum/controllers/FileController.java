package com.secondcommit.forum.controllers;

import com.secondcommit.forum.entities.Subject;
import com.secondcommit.forum.repositories.SubjectRepository;
import com.secondcommit.forum.security.payload.MessageResponse;
import com.secondcommit.forum.services.subject.SubjectServiceImpl;
import com.secondcommit.forum.services.user.UserServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Controller to manage the upload of files
 * It's necessary to be authenticated
 * It's only used to send images individually. When updating a user, as an example, you can send the image in the
 *    userDto
 */
@RestController
@RequestMapping("/api/image")
public class FileController {

    private final UserServiceImpl userService;
    private final SubjectServiceImpl subjectService;
    private final SubjectRepository subjectRepository;

    public FileController(UserServiceImpl userService, SubjectServiceImpl subjectService,
                          SubjectRepository subjectRepository) {
        this.userService = userService;
        this.subjectService = subjectService;
        this.subjectRepository = subjectRepository;
    }

    /**
     * Method to upload the user avatar
     * @param username (gets from the jwt token)
     * @param avatar (MultipartFile)
     * @return ResponseEntity (ok: url, bad request: messageResponse)
     */
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/user")
    @ApiOperation("Uploads the user avatar")
    public ResponseEntity<?> uploadAvatarToUser(@CurrentSecurityContext(expression="authentication?.name") String username,
                                          MultipartFile avatar){

        //Validates avatar
        if (avatar == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Missing file"));

        return userService.addAvatar(username, avatar);
    }

    /**
     * Method to upload the subject avatar
     * @param id (gets from the url)
     * @param avatar (MultipartFile)
     * @return ResponseEntity (ok: url, bad request: messageResponse)
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/subject/{id}")
    @ApiOperation("Uploads the user avatar")
    public ResponseEntity<?> uploadAvatarToSubject(@PathVariable Long id, MultipartFile avatar){

        //Validates avatar
        if (avatar == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Missing file"));

        //Validates subject
        Optional<Subject> subjectOpt = subjectRepository.findById(id);

        if (subjectOpt.isEmpty())
            return ResponseEntity.badRequest().body(new MessageResponse("Bad ID"));

        return subjectService.addAvatarToSubject(subjectOpt.get(), avatar);
    }
}
