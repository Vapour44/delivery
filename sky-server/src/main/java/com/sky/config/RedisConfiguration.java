package com.sky.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate template = new RedisTemplate<>();
        template .setConnectionFactory(factory);

        //设置序列化器,将Java对象转化为直观的redis数据
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }
}
