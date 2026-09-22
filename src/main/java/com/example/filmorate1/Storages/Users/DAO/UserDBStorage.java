package com.example.filmorate1.Storages.Users.DAO;

import com.example.filmorate1.BacisClasses.Film.FilmGenre;
import com.example.filmorate1.BacisClasses.User;
import com.example.filmorate1.Exceptions.UserExceptions.NonExistingUserException;
import com.example.filmorate1.Exceptions.UserExceptions.UserAlreadyExistsException;
import com.example.filmorate1.Storages.Users.Mappers.UserRowMapper;
import com.example.filmorate1.Storages.Users.UserStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("userDBStorage")
@Slf4j
public class UserDBStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<Integer, User> getUsersList() {// добавить вывод лайкнутых фильмов
        String sql = "SELECT * from users";

        List<User> usersList = new ArrayList<>();
        try{
            usersList= jdbcTemplate.query(sql, new UserRowMapper());
        }
        catch (Exception e)
        {
            log.error("Cannot get users: "+e.getMessage());
            throw new RuntimeException("Unable to retrieve users from database",e);
        }

        Map<Integer, Set<Integer>> friendsMap=getAllFriendsIds();

        Map<Integer, User> usersMap = new HashMap<>();
        Map<Integer,Set<Integer>> likedFilmsMap=getAllLikes();

        for (User user : usersList) {
            user.setFriendsList(friendsMap.getOrDefault(user.getId(),new HashSet<>()));
            user.setLikedFilmsList(likedFilmsMap.getOrDefault(user.getId(),new HashSet<>()));
            usersMap.put(user.getId(), user);
        }

        return usersMap;
    }

    private Map<Integer, Set<Integer>> getAllFriendsIds()
    {
        Map<Integer, Set<Integer>> friendsMap = new HashMap<>();
        String confirmedFriendsSql="SELECT id_user, id_friend from friend_users " +
                "WHERE status = 'CONFIRMED'";

        try{
            jdbcTemplate.query(confirmedFriendsSql, rs->{
                    int userId=rs.getInt("id_user");
                    int friendId=rs.getInt("id_friend");

                friendsMap.computeIfAbsent(userId, k->new HashSet<>()).add(friendId);
                friendsMap.computeIfAbsent(friendId, k->new HashSet<>()).add(userId);
            });
        }
        catch (Exception e)
        {
            log.error("Cannot get friends ids: "+e.getMessage());
            throw new RuntimeException("Unable to retrieve users from database",e);
        }

        String pendingFriendsSql="SELECT id_user, id_friend FROM friend_users " +
                "WHERE status = 'PENDING'";

        try{
            jdbcTemplate.query(pendingFriendsSql, rs->{
                int userId=rs.getInt("id_user");
                int friendId=rs.getInt("id_friend");

                friendsMap.computeIfAbsent(userId, k->new HashSet<>()).add(friendId);
            });
        }
        catch (Exception e)
        {
            log.error("Cannot get friends ids: "+e.getMessage());
            throw new RuntimeException("Unable to retrieve users from database",e);
        }
        return friendsMap;
    }

    @Override
    public void setUsersList(final Map<Integer, User> usersList) {}

    @Override
    public User getUserById(int id) throws NonExistingUserException  { // доделать выыод друзей

        if(!checkUserExists(id))
        {
            throw new NonExistingUserException(id);
        }

        String sql = "SELECT * from users where id=?";

        try{
            User user= jdbcTemplate.queryForObject(sql,new UserRowMapper(),id);
            user.setFriendsList(getUserFriendsListId(id));
            return user;
        }
        catch (Exception e)
        {
            log.error("Cannot get user by id: "+e.getMessage());
            throw new NonExistingUserException(id);
        }
    }

    private Set<Integer> getUserFriendsListId(int userId)
    {
        String sql= "SELECT users.id " +
                "FROM users WHERE users.id IN " +
                "(SELECT friend_users.id_friend FROM friend_users WHERE friend_users.id_user = ? " +
                "UNION " +
                "SELECT friend_users.id_user FROM friend_users WHERE friend_users.id_friend = ? AND friend_users.status='CONFIRMED')";

        try{
            List<Integer> friendsList= jdbcTemplate.queryForList(sql, Integer.class,userId,userId);
            Set<Integer> usersSet=new HashSet<>(friendsList);
            log.info("User "+userId+"has "+friendsList.size()+" friends");
            for(Integer i:usersSet)
            {
                log.info("Found friend with id="+i);
            }
            return usersSet;
        }
        catch(Exception e)
        {
            log.error("Cannot get user friends"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void removeUser(int userId) throws NonExistingUserException {

        if(!checkUserExists(userId))
        {
            throw new NonExistingUserException(userId);
        }
        String sql = "DELETE FROM users WHERE id=?";

        try{
            jdbcTemplate.update(sql,userId);
        }
        catch (Exception e){
            log.error("Cannot delete user by id: "+e.getMessage());
            throw new NonExistingUserException(userId);
        }
    }

    @Override
    public void addUser(User user) throws UserAlreadyExistsException {

        if(checkUserExists(user.getLogin(),user.getEmail())){
            throw new UserAlreadyExistsException(user.getLogin(),user.getEmail());
        }

        String sql = "INSERT INTO users (name, email, login, password, birthday_date) VALUES (?,?,?,?,?)";

        try{
            jdbcTemplate.update(sql,user.getName(),user.getEmail(),user.getLogin(),user.getPassword(),user.getBirthdayDate());
        }
        catch (Exception e){
            log.error("Cannot add user: "+e.getMessage());
            throw new NonExistingUserException(user.getLogin(),user.getEmail());
        }
    }

    @Override
    public void updateUser(int userId, User user) throws NonExistingUserException {

        if(!checkUserExists(userId)){
            throw new NonExistingUserException(userId);
        }

        String sql="UPDATE users SET name=?, email=?, login=?, password=?, birthday_date=? WHERE id=?";

        try{
            jdbcTemplate.update(sql,user.getName(),user.getEmail(),user.getLogin(),user.getPassword(),user.getBirthdayDate(),userId);
        }
        catch (Exception e){
            log.error("Cannot update user: "+e.getMessage());
            throw new NonExistingUserException(user.getLogin(),user.getEmail());
        }
    }

    private boolean checkUserExists(String login,String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE login=? AND email=?";

        try{
            int count = jdbcTemplate.queryForObject(sql,Integer.class,login,email);
            return count > 0;
        }
        catch(Exception e){
            log.error("Cannot check existing user: ",e);
            return false;
        }
    }
    private boolean checkUserExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id=?";

        try{
            int count = jdbcTemplate.queryForObject(sql,Integer.class,userId);
            return count > 0;
        }
        catch(Exception e){
            log.error("Cannot check existing user: ",e);
            return false;
        }
    }
    private Map<Integer, Set<Integer>> getAllLikes()
    {
        String sql = "SELECT id_user, id_film FROM film_likes";

        Map<Integer, Set<Integer>> likesMap = new HashMap<>();
        try{
            jdbcTemplate.query(sql, rs->{
                int userId=rs.getInt("id_user");
                int filmId=rs.getInt("id_film");

                likesMap.computeIfAbsent(userId, k->new HashSet<>()).add(filmId);
            });
        }
        catch (Exception e)
        {
            log.error("Cannot get likes: "+e.getMessage());
            throw new RuntimeException("Unable to retrieve users from database",e);
        }
        return likesMap;
    }


}
