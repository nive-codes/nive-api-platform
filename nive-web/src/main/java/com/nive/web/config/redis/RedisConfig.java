package com.nive.web.config.redis;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private final Environment environment;

    public RedisConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public RedissonClient redissonClient() {
        String protocol = isProdProfile() ? "rediss" : "redis";
        String redisAddress = protocol + "://" + redisHost + ":" + redisPort;

        log.info("Initializing RedissonClient with address: {}", redisAddress);

        Config config = new Config();
        config.useSingleServer().setAddress(redisAddress);
        return Redisson.create(config);
    }

    private boolean isProdProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    @Bean
    public ProxyManager<String> proxyManager(RedissonClient redissonClient) {
        CommandAsyncExecutor executor = ((Redisson) redissonClient).getCommandExecutor();
        return RedissonBasedProxyManager.builderFor(executor).build();
    }
}