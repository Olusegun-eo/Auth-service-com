package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.RedisUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisUserDao {

    public static final String HASH_KEY = "User";
    @SuppressWarnings("rawtypes")
	@Autowired
    private RedisTemplate template;

    @SuppressWarnings("unchecked")
	public RedisUser save(RedisUser redisUser){
        template.opsForHash().put(HASH_KEY,redisUser.getId(),redisUser);
        return redisUser;
    }

    @SuppressWarnings("unchecked")
	public List<RedisUser> findAll(){
        return template.opsForHash().values(HASH_KEY);
    }

    @SuppressWarnings("unchecked")
	public RedisUser findUserById(int id){
        return (RedisUser) template.opsForHash().get(HASH_KEY,id);
    }


    @SuppressWarnings("unchecked")
	public String deleteUser(int id){
        template.opsForHash().delete(HASH_KEY,id);
        return "user deleted !!";
    }

}
