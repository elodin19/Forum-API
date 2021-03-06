package com.secondcommit.forum.controllers;

import com.secondcommit.forum.dto.ActivateUserRequest;
import com.secondcommit.forum.dto.ForgotPassRequest;
import com.secondcommit.forum.dto.NewPassRequest;
import com.secondcommit.forum.dto.NewUserRequest;
import com.secondcommit.forum.entities.User;
import com.secondcommit.forum.repositories.UserRepository;
import com.secondcommit.forum.security.jwt.JwtTokenUtil;
import com.secondcommit.forum.security.payload.JwtResponse;
import com.secondcommit.forum.security.payload.LoginRequest;
import com.secondcommit.forum.security.payload.MessageResponse;
import com.secondcommit.forum.services.sparkpost.SparkPostServiceImpl;
import com.secondcommit.forum.services.authentication.AuthServiceImpl;
import com.secondcommit.forum.services.user.UserServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Controller to manage the authentications
 * When the authentication are ok it sends a JWT token as answer
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final SparkPostServiceImpl sparkPost;
    private final AuthServiceImpl authService;
    private final UserServiceImpl userService;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenUtil jwtTokenUtil,
                          UserRepository userRepository,
                          SparkPostServiceImpl sparkPost,
                          AuthServiceImpl authService,
                          UserServiceImpl userService){
        this.authManager = authManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.sparkPost = sparkPost;
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Method to login
     * @param loginRequest (username and password)
     * @return ResponseEntity (ok: jwt token, bad request: messageResponse)
     */
    @PostMapping("/login")
    @ApiOperation("Login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){

        try {
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

            //If the user isn't activated yet, the login won't work
            if (!userOpt.get().getIsActivated())
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("The user " + userOpt.get().getUsername() + " isn't validated yet"));

        } catch (NoSuchElementException e){

            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("The user " + loginRequest.getUsername() + " doesn't exist"));
        }

        //Authentication
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenUtil.generateJwtToken(authentication, loginRequest.isRememberMe());

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    /**
     * Method to create a new user
     * @param newUser
     * @return ResponseEntity
     */
    @PostMapping("/new-user")
    @ApiOperation("Creates new user")
    public ResponseEntity<?> newUser(NewUserRequest newUser){

        //Validates the DTO
        if (newUser.getUsername() != null &&
                newUser.getEmail() != null &&
                newUser.getPassword() != null)
            return userService.createUser(newUser, "USER");

        return ResponseEntity.badRequest()
                .body(new MessageResponse("Missing parameters"));
    }

    /**
     * Method to create a new user with ADMIN role
     * @param newUser
     * @return ResponseEntity
     */
    @PostMapping("/new-user-admin")
    public ResponseEntity<?> newUserAdmin(NewUserRequest newUser){

        //Validates the DTO
        if (newUser.getUsername() != null &&
                newUser.getEmail() != null &&
                newUser.getPassword() != null)
            return userService.createUser(newUser, "ADMIN");

        return ResponseEntity.badRequest()
                .body(new MessageResponse("Missing parameters"));
    }

    /**
     * Method to activate the user
     * * The user won't be able to log in before activating the account!
     *
     * @param activateUser
     * @return ResponseEntity
     */
    @PostMapping("/activate-user")
    @ApiOperation("Activates the new user")
    public ResponseEntity<?> activateUser(@RequestBody ActivateUserRequest activateUser){

        //Validates the DTO
        if (activateUser.getUsername() == null || activateUser.getActivationCode() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Missing parameters"));

        //Validates de user
        Optional<User> userOpt = userRepository.findByUsername(activateUser.getUsername());

        if (userOpt.isEmpty())
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("The user" + activateUser.getUsername() + " doesn't exist"));

        return userService.activateUser(userOpt.get(), activateUser.getActivationCode());
    }

    /**
     * Method to ask for a new password
     * @param forgotPass
     * @return ResponseEntity
     */
    @PostMapping("/forgot-pass")
    @ApiOperation("Asks for a new password")
    public ResponseEntity<?> forgotPass(@RequestBody ForgotPassRequest forgotPass){

        //Validates the DTO
        if (forgotPass.getEmail() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Missing parameters"));

        //Validates de user
        Optional<User> userOpt = userRepository.findByEmail(forgotPass.getEmail());

        if (userOpt.isEmpty())
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("The email " + forgotPass.getEmail() + " isn't registered"));

        return authService.askNewPass(userOpt.get());
    }

    /**
     * Method that sets a new password to the user
     * @param newPassRequest
     * @return
     */
    @PostMapping("/save-pass")
    @ApiOperation("Save new password")
    public ResponseEntity<?> setNewPass(@RequestBody NewPassRequest newPassRequest){

        //Validates the DTO
        if (newPassRequest.getUsername() == null ||
            newPassRequest.getNewPass() == null ||
            newPassRequest.getValidationCode() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Missing parameters"));

        //Validates de user
        Optional<User> userOpt = userRepository.findByUsername(newPassRequest.getUsername());

        if (userOpt.isEmpty())
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("The user" + newPassRequest.getUsername() + " doesn't exist"));

        return authService.setNewPass(userOpt.get(), newPassRequest.getNewPass(), newPassRequest.getValidationCode());
    }
}
