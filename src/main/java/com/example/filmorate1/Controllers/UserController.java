package com.example.filmorate1.Controllers;

import com.example.filmorate1.BacisClasses.User;
import com.example.filmorate1.Exceptions.FriendsExceptions.*;
import com.example.filmorate1.Exceptions.UserExceptions.NonExistingUserException;
import com.example.filmorate1.Exceptions.UserExceptions.UserAlreadyExistsException;
import com.example.filmorate1.Services.Users.InMemoryUserService;
import com.example.filmorate1.Services.Users.UserDBService;
import com.example.filmorate1.Storages.Users.DAO.UserDBStorage;
import com.example.filmorate1.Storages.Users.InMemoryUserStorage;
import com.example.filmorate1.Storages.Users.UserStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Component
@RestController
@Slf4j
public class UserController {

    private UserStorage inMemoryUserStorage;
    private InMemoryUserService inMemoryUserService;

    private UserStorage userDBStorage;
    private UserDBService userDBService;

    private boolean userValidCache=false;
    private boolean friendsValidCache=false;

    @Autowired
    public UserController(InMemoryUserStorage inMemoryUserStorage, InMemoryUserService inMemoryUserService, UserDBStorage userDBStorage, UserDBService userDBService) {
        this.inMemoryUserStorage = inMemoryUserStorage;
        this.inMemoryUserService = inMemoryUserService;
        this.userDBStorage = userDBStorage;
        this.userDBService = userDBService;
    }


    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {//checked
        log.info("Получен запрос GetUsers");
        if(!userValidCache) {
            inMemoryUserStorage.setUsersList(userDBStorage.getUsersList());
            userValidCache=true;
        }

        return ResponseEntity.ok(inMemoryUserStorage.getUsersList());
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> putUser(@RequestBody  @Validated User user, // checked
                                        @PathVariable int userId) {

        log.info("Получен запрос PutUser");

        try{
            userDBStorage.updateUser(userId, user);
            log.info("Successfully updated user with ID={}",user.getId());

            userValidCache=false;

            return ResponseEntity.ok(user);
        }
        catch(NonExistingUserException e)
        {
            log.warn("Cannot update user: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/users")
    public ResponseEntity<User> postUser(@RequestBody  @Validated User user) {//checked

        log.info("Получен запрос PostUser");

        try{
            userDBStorage.addUser(user);
            log.info("Successfully added user with ID={}",user.getId());

            userValidCache=false;

            return ResponseEntity.ok(user);
        }
        catch(UserAlreadyExistsException e)
        {
            log.error("Cannot add user: "+e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(user);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) { //checked
        log.info("Starting DeleteUser");
        try{
            userDBStorage.removeUser(userId);
            log.info("Successfully deleted user with ID={}",userId);

            userValidCache=false;

            return ResponseEntity.ok("User has been successfully deleted");
        }
        catch(NonExistingUserException e)
        {
            log.warn("Cannot delete user: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User does not exist");
        }
    }

    @PutMapping("/users/{userId}/friends/{friendId}")
    public ResponseEntity<?> putUserFriend(@PathVariable int userId, @PathVariable int friendId) { //checked

        log.info("Starting PutUserFriend");
        try{
            String resultString =userDBService.addFriend(userId, friendId);
            friendsValidCache=false;
            log.info(resultString);
            return ResponseEntity.ok(resultString);
        }
        catch(FriendshipRequestAlreadyExists e)
        {
            log.error("Friendship request already exists: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        catch (NonExistingFriendException e)
        {
            log.error("Friendship request does not exist: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (AlreadyFriendsException e)
        {
            log.error("Users already friends: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        catch (NonExistingUserException e)
        {
            log.warn("Cannot update user: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (SelfFriendshipException e)
        {
            log.error("Cannot add yourself to friends: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/users/{userId}/friends/{friendId}")
    public ResponseEntity<?> deleteUserFriend(@PathVariable int userId, @PathVariable int friendId) {//checked

        log.info("Starting DeleteUserFriend");
        try{
            userDBService.removeFriend(userId, friendId);
            friendsValidCache=false;
            log.info("Users with ID={} and {} are not friends anymore", userId, friendId);
            return ResponseEntity.ok("Users with ID="+userId+" and " +friendId+ " are not friends anymore");
        }
        catch (NonExistingUserException e)
        {
            log.error("Cannot complete request: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch(NonExistingFriendException e)
        {
            log.error("Cannot complete request: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (NonExistingFriendshipRequestException e)
        {
            log.error("Cannot complete request: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {//checked
        log.info("Starting GetUserById");
        try{
            User user=null;
            if (userValidCache){
                user=inMemoryUserStorage.getUserById(id);
            }
            else{
                user=userDBStorage.getUserById(id);
            }
            log.info("Found user with ID={}",id);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        }
        catch (NonExistingUserException e)
        {
            log.info("Cannot update user: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/users/{id}/friends/mutual/{friendId}")
    public ResponseEntity<?> getUserFriendMutual(@PathVariable int id, @PathVariable int friendId) {//checked

        log.info("Starting GetUserFriendMutual");
        try{
            ArrayList<User> mutualFriendList=userDBService.getMutualFriends(id, friendId);
            log.info("Users with ID={} and {} have {} mutual friends", id, friendId,mutualFriendList.size());
            return ResponseEntity.status(HttpStatus.OK).body(mutualFriendList);
        }
        catch (NonExistingUserException e)
        {
            log.info("Cannot update user: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch(NonExistingFriendException e)
        {
            log.info("Cannot update user: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/users/{id}/friends")
    public ResponseEntity<?> getUserFriends(@PathVariable int id) {//checked
        log.info("Starting GetUserFriends");

        try{
            ArrayList<User> friendsList=userDBService.getUserFriendsList(id);
            log.info("Found {} friends for user with ID={}", friendsList.size(), id);
            return ResponseEntity.status(HttpStatus.OK).body(friendsList);
        }
        catch (NonExistingUserException e)
        {
            log.info("Cannot find user: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
