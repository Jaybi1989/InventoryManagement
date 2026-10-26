package ch.jacqueline.inventory.controller;

import ch.jacqueline.inventory.config.CurrentUser;
import ch.jacqueline.inventory.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/User")
    public User user(@CurrentUser User currentUser) {
        return currentUser;
    }
}