package com.secondcommit.forum.controllers;

import com.secondcommit.forum.dto.PostDto;
import com.secondcommit.forum.repositories.ModuleRepository;
import com.secondcommit.forum.repositories.PostRepository;
import com.secondcommit.forum.security.payload.MessageResponse;
import com.secondcommit.forum.services.post.PostServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to manage the Post CRUD methods
 */
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostServiceImpl postService;
    private final PostRepository postRepository;
    private final ModuleRepository moduleRepository;

    public PostController(PostServiceImpl postService, PostRepository postRepository, ModuleRepository moduleRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.moduleRepository = moduleRepository;
    }

    /**
     * Method to create new post
     * @param postDto (title and content)
     * @param username (gets from the jwt token)
     * @return ResponseEntity (ok: post, bad request: messageResponse)
     */
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{moduleId}")
    @ApiOperation("Creates new post. Authentication required (USER)")
    public ResponseEntity<?> newPost(@PathVariable Long moduleId, PostDto postDto,
                                     @CurrentSecurityContext(expression="authentication?.name") String username) {

        //Validates module id
        if (!moduleRepository.existsById(moduleId))
            return ResponseEntity.badRequest().body(new MessageResponse("Wrong id"));

        //Validates post
        if (postDto.getContent() == null || postDto.getTitle() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Missing parameters"));

        //Validates length of MultipartFile[]
        if (postDto.getFiles() != null)
            if (postDto.getFiles().length > 5)
                return ResponseEntity.badRequest().body(new MessageResponse("Max 5 files are allowed"));

        return postService.addPost(moduleId, postDto, username);
    }

    /**
     * Method to get the post
     * @param id
     * @return ResponseEntity (ok: post, bad request: messageResponse)
     */
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/{id}")
    @ApiOperation("Gets post. Authentication required (USER)")
    public ResponseEntity<?> getPost(@PathVariable Long id,
                                     @CurrentSecurityContext(expression="authentication?.name") String username){

        //Validates post
        if (!postRepository.existsById(id))
            return ResponseEntity.badRequest().body(new MessageResponse("Wrong id"));

        return postService.getPost(id, username);
    }

    /**
     * Method to update post.
     * @param id
     * @param postDto
     * @param username (Gets from the jwt token)
     * @return ResponseEntity (ok: post, bad request: messageResponse)
     */
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/{id}")
    @ApiOperation("Updates the post. Authentication required (USER)")
    public ResponseEntity<?> updatePost(@PathVariable Long id, PostDto postDto,
                                        @CurrentSecurityContext(expression="authentication?.name") String username){

        //Validates Dto
        if (postDto.getTitle() == null && postDto.getContent() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Missing parameters"));

        //Validates id
        if (!postRepository.existsById(id))
            return ResponseEntity.badRequest().body(new MessageResponse("Wrong id"));

        if (postDto.getFiles() != null)
            if (postDto.getFiles().length > 5)
                return ResponseEntity.badRequest().body(new MessageResponse("Max 5 files are allowed"));

        return postService.updatePost(id, postDto, username);
    }

    /**
     * Method to delete post
     * @param id
     * @param username (Gets from the jwt token)
     * @return ResponseEntity (messageResponse)
     */
    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/{id}")
    @ApiOperation("Deletes the post. Authentication required (USER)")
    public ResponseEntity<?> deletePost(@PathVariable Long id,
                                        @CurrentSecurityContext(expression="authentication?.name") String username){

        //Validates id
        if (!postRepository.existsById(id))
            return ResponseEntity.badRequest().body(new MessageResponse("Wrong id"));

        return postService.deletePost(id, username);
    }

    /**
     * Method to add or remove like from post
     * @param id
     * @param username (Gets from the jwt token)
     * @return ResponseEntity(ok: totalLikes, bad request: messageResponse)
     */
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/like/{id}")
    @ApiOperation("Adds or removes like, depending on the previous state. Authentication required (USER)")
    public ResponseEntity<?> like(@PathVariable Long id,
                                  @CurrentSecurityContext(expression="authentication?.name") String username){

        //Validates id
        if (!postRepository.existsById(id))
            return ResponseEntity.badRequest().body(new MessageResponse("Wrong id"));

        return postService.like(id, username);
    }

    /**
     * Method to add or remove dislike from post
     * @param id
     * @param username (Gets from the jwt token)
     * @return ResponseEntity(ok: totalDislikes, bad request: messageResponse)
     */
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/dislike/{id}")
    @ApiOperation("Adds or removes dislike, depending on the previous state. Authentication required (USER)")
    public ResponseEntity<?> dislike(@PathVariable Long id,
                                    @CurrentSecurityContext(expression="authentication?.name") String username){

        //Validates id
        if (!postRepository.existsById(id))
            return ResponseEntity.badRequest().body(new MessageResponse("Wrong id"));

        return postService.dislike(id, username);
    }

    /**
     * Method to fix or unfix a post. ADMIN only
     * @param id
     * @return ResponseEntity (MessageResponse)
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/fix/{id}")
    @ApiOperation("Fix post. Authentication required (ADMIN)")
    public ResponseEntity<?> fix(@PathVariable Long id){

        //Validates id
        if (!postRepository.existsById(id))
            return ResponseEntity.badRequest().body(new MessageResponse("Wrong id"));

        return postService.fix(id);
    }

    /**
     * Method to make user follow post
     * @param id
     * @param username (Gets from the jwt token)
     * @return ResponseEntity (ok: post, bad request: messageResponse)
     */
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/follow/{id")
    @ApiOperation("User follows post. Authentication required (USER")
    public ResponseEntity<?> follow(@PathVariable Long id,
                                    @CurrentSecurityContext(expression="authentication?.name") String username){

        //Validates id
        if (!postRepository.existsById(id))
            return ResponseEntity.badRequest().body(new MessageResponse("Wrong id"));

        return postService.follow(id, username);
    }

    /**
     * Method to make user unfollow post
     * @param id
     * @param username (Gets from the jwt token)
     * @return ResponseEntity (ok: post, bad request: messageResponse)
     */
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/unfollow/{id")
    @ApiOperation("User unfollows post. Authentication required (USER")
    public ResponseEntity<?> unfollow(@PathVariable Long id,
                                    @CurrentSecurityContext(expression="authentication?.name") String username){

        //Validates id
        if (!postRepository.existsById(id))
            return ResponseEntity.badRequest().body(new MessageResponse("Wrong id"));

        return postService.unfollow(id, username);
    }
}
