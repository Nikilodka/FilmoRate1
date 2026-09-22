package com.example.filmorate1.Services.Users;

import com.example.filmorate1.BacisClasses.User;
import com.example.filmorate1.Exceptions.FriendsExceptions.*;
import com.example.filmorate1.Exceptions.UserExceptions.NonExistingUserException;
import com.example.filmorate1.Storages.Users.Mappers.UserRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Qualifier("userDBService")
@Slf4j
public class UserDBService implements UserService {

    JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDBService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String addFriend(int senderId, int receiverId) throws FriendshipRequestAlreadyExists,
            AlreadyFriendsException,
            NonExistingUserException,
            SelfFriendshipException
    {
        if(senderId==receiverId)
        {
            throw new SelfFriendshipException(senderId);
        }
        checkUserExists(senderId);
        checkUserExists(receiverId);

        if(!checkExistingFriendshipRequest(senderId, receiverId)
                && !checkExistingFriendshipRequest(receiverId, senderId)
                && !checkExistingFriendship(senderId, receiverId))
        {
            sendFriendshipRequest(senderId, receiverId);
            return "User with id " + senderId + " sent friendship request to user with id " + receiverId;
        }
        else if(checkExistingFriendshipRequest(senderId, receiverId))
        {
            throw new FriendshipRequestAlreadyExists(senderId, receiverId);
        }
        else
        {
            try{
                log.info("Confirming frienship...");
                confirmFriendship(senderId, receiverId);
                return "User with id " + senderId + " confirmed friendship with user with id " + receiverId;
            } catch (AlreadyFriendsException e) {
                throw e;
            }
        }

    }

    @Override
    public void removeFriend(int senderId, int receiverId) {

        checkUserExists(senderId);
        checkUserExists(receiverId);

        if(checkExistingFriendship(senderId, receiverId))
        {
            log.info("removing friendship...");
            removeFriendship(senderId, receiverId);
            log.info("User with id " + senderId + " removed friendship with user with id " + receiverId);
            return;
        }
        if (checkExistingFriendshipRequest(senderId, receiverId))
        {
            log.info("removing friedship request....");
            removeFriendshipRequest(senderId, receiverId);
            log.info("User with id " + senderId + " removed friendship request from user with id " + receiverId);
        }
    }

    @Override
    public ArrayList<User> getUserFriendsList(int userId)
    {
        String sql= "SELECT users.id, " +
                "users.name, " +
                "users.email, " +
                "users.login, " +
                "users.password, " +
                "users.birthday_date FROM users WHERE users.id IN " +
                "(SELECT friend_users.id_friend FROM friend_users WHERE friend_users.id_user = ? " +
                "UNION " +
                "SELECT friend_users.id_user FROM friend_users WHERE (friend_users.id_friend = ? AND friend_users.status='CONFIRMED'))" ;

        try{
            List<User> friendsList= jdbcTemplate.query(sql,new UserRowMapper(),userId,userId);
            log.info("User "+userId+"has "+friendsList.size()+" friends");
            for(User user:friendsList)
            {
                log.info("Found friend with id="+user.getId());
            }
            return new ArrayList<>(friendsList);
        }
        catch(Exception e)
        {
            log.error("Cannot get user friends"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ArrayList<User> getMutualFriends(int userId1, int userId2){

        List<User> friendsList1= getUserFriendsList(userId1);
        List<User> friendsList2= getUserFriendsList(userId2);

        List<User> intersection = new ArrayList<>(friendsList1);
        intersection.retainAll(friendsList2);

        return new ArrayList<>(intersection);

    }
    private void sendFriendshipRequest(int senderId, int receiverId) throws FriendshipRequestAlreadyExists {

        if(checkExistingFriendshipRequest(senderId, receiverId)) {
            throw new FriendshipRequestAlreadyExists(senderId, receiverId);
        }

        String sql = "INSERT INTO friend_users (id_user,id_friend,status) VALUES (?, ?, 'PENDING')";

        try{
            jdbcTemplate.update(sql, senderId, receiverId);
            log.info("User " + senderId + " sent friend request to " + receiverId);
        }
        catch(Exception e){
            log.error("Cannot send friend request: "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public void confirmFriendship(int senderId, int receiverId) throws AlreadyFriendsException {
        String checkPendingRequestSql="SELECT COUNT(*) FROM friend_users WHERE " +
                "friend_users.id_user = ? AND friend_users.id_friend = ? AND friend_users.status='PENDING'";

        int count = jdbcTemplate.queryForObject(checkPendingRequestSql,Integer.class, receiverId,senderId);

        if(count==0) {
            throw new AlreadyFriendsException(senderId, receiverId); // исправить обработку запроса на дружбу
        }

        String sql = "UPDATE friend_users SET status = 'CONFIRMED' WHERE " +
                "(id_user = ? AND id_friend = ?)";
        try {
            jdbcTemplate.update(sql, receiverId, senderId);
            log.info("User " + receiverId + " confirmed friendship with user " + senderId);
        }
        catch (DataAccessException e) {
            log.error("Unable to confirm friendship: "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private boolean checkExistingFriendshipRequest(int userId, int friendId){
        log.info("UserId="+userId+" friendId="+friendId);
        String sql="SELECT COUNT(*) FROM friend_users WHERE (id_user = ? AND id_friend = ?) " +
                "AND status='PENDING'";

        try{
            int count = jdbcTemplate.queryForObject(sql,Integer.class,userId,friendId);
            log.info("found "+count+" entries");
            if(count>0){
                return true;
            }
            else {
                return false;
            }
        }
        catch(Exception e){
            log.error("Cannot check existing friend: "+e.getMessage());
            return false;
        }
    }
    private boolean checkExistingFriendship(int userId, int friendId){
        String sql="SELECT COUNT(*) FROM friend_users WHERE ((id_user = ? AND id_friend = ?) " +
                "OR (id_user = ? AND id_friend = ?)) " +
                "AND status='CONFIRMED'";

        try{
            int count = jdbcTemplate.queryForObject(sql,Integer.class,userId,friendId,friendId,userId);
            log.info("Found "+count+" entries");
            if(count==0){
                return false;
            }
            else {
                return true;
            }
        }
        catch(Exception e){
            log.error("Cannot check existing friend: "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    private void removeFriendship(int senderId, int receiverId) {
        if(!checkExistingFriendship(senderId, receiverId)) {
            throw new NonExistingFriendException(senderId, receiverId);
        }

        String sql = "DELETE FROM friend_users WHERE (id_user = ? AND id_friend = ?) " +
                "OR (id_user = ? AND id_friend = ?)";

        try {
            jdbcTemplate.update(sql, senderId, receiverId, receiverId,senderId);
            log.info("User " + senderId + " removed friendship with user " + receiverId);
        }
        catch (DataAccessException e) {
            log.error("Unable to remove friendship: "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        String restoreRequestSql="INSERT INTO friend_users (id_user, id_friend, status) VALUES (?, ?, 'PENDING')";

        try{
            jdbcTemplate.update(restoreRequestSql,receiverId,senderId);
            log.info("Current status: user with id "+receiverId+" has a request to user with id "+senderId);
        }
        catch(Exception e){
            log.error("Cannot restore friendship request: "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    private void removeFriendshipRequest(int senderId, int receiverId) {
        log.info("removing friendship request with user "+senderId+" and id "+receiverId);
        if(!checkExistingFriendshipRequest(senderId, receiverId)) {
            throw new NonExistingFriendshipRequestException(senderId, receiverId);
        }

        String sql="DELETE FROM friend_users WHERE id_user = ? AND id_friend = ?";

        try{
            jdbcTemplate.update(sql, senderId, receiverId);
            log.info("User " + senderId + " removed friendship with user " + receiverId);
        }
        catch(Exception e){
            log.error("Cannot remove friendship request: "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    private boolean checkUserExists(int userId){
        String sql="SELECT COUNT(*) FROM users WHERE id = ?";

        try{
            int count = jdbcTemplate.queryForObject(sql,Integer.class,userId);
            if(count>0){
                return true;
            }
            else {
                throw new NonExistingUserException(userId);
            }
        }
        catch(DataAccessException e){
            log.error("Unable to check user: "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getFriendStatus(int userId, int friendId){
        String sql = "SELECT friend_users.status FROM friend_users WHERE " +
                "(friend_users.id_user = ? AND friend_users.id_friend = ?) " +
                "OR (friend_users.id_user = ? AND friend_users.id_friend = ?)";
        try{
            String status = jdbcTemplate.queryForObject(sql,String.class,userId,friendId,friendId,userId);
            return status;
        }
        catch(Exception e){
            log.error("Cannot get friend status"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
