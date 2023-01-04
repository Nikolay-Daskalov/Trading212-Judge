package com.trading212.judge.web.controller;

import com.trading212.judge.model.binding.UserRegistrationBindingModel;
import com.trading212.judge.model.dto.UserDTO;
import com.trading212.judge.model.dto.UserRegistrationDTO;
import com.trading212.judge.service.user.UserService;
import com.trading212.judge.web.exception.UserExistException;
import com.trading212.judge.web.exception.UserRegistrationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.trading212.judge.web.controller.UserController.Routes;

@RestController
@RequestMapping(path = Routes.BASE)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping(path = Routes.REGISTER)
    public ResponseEntity<UserDTO> register(@RequestBody @Valid UserRegistrationBindingModel userRegistrationBindingModel,
                                            BindingResult bindingResult, HttpServletRequest httpServletRequest) {

        if (bindingResult.hasErrors() || !userRegistrationBindingModel.password().equals(userRegistrationBindingModel.confirmPassword())) {
            throw new UserRegistrationException("User credentials not valid!");
        }

        boolean exist = userService.isExist(userRegistrationBindingModel.username());

        if (exist) {
            throw new UserExistException("User with that username already exists!");
        }

        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO(
                userRegistrationBindingModel.username(),
                userRegistrationBindingModel.email(),
                userRegistrationBindingModel.password());

        UserDTO user = userService.register(userRegistrationDTO);

        if (user == null) {
            throw new UserRegistrationException("User registration failed!");
        }

        return ResponseEntity.created(URI.create(httpServletRequest.getContextPath())).body(user);
    }

    static class Routes {
        static final String BASE = "/api/users";

        static final String REGISTER = "/register";
    }
}