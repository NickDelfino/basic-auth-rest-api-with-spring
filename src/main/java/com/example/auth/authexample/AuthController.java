package com.example.auth.authexample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class AuthController {

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    public AuthController(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    @RequestMapping(path="/user/{userName}", method = RequestMethod.GET)
    public ResponseEntity<?> getUser(@PathVariable(value = "userName") String userName){

        System.out.println("Getting user: " + userName);

        if(!inMemoryUserDetailsManager.userExists(userName)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        UserDetails user = inMemoryUserDetailsManager.loadUserByUsername(userName);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public ResponseEntity createUser(@RequestBody RequestUser requestUser) {

        System.out.println("Creating user: " + requestUser.getUserName());

        if(inMemoryUserDetailsManager.userExists(requestUser.getUserName())) {
            return new ResponseEntity(null, HttpStatus.CONFLICT);
        }

        inMemoryUserDetailsManager.createUser(User.withUsername(requestUser.getUserName()).password(requestUser.getPassword()).authorities(new ArrayList<>()).build());

        return new ResponseEntity(null, HttpStatus.CREATED);
    }
}
