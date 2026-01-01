package com.leesuchan.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.leesuchan.service",
    "com.leesuchan.account",
    "com.leesuchan.activity",
    "com.leesuchan.common",
    "com.leesuchan.infra"
})
@EnableJpaRepositories(basePackages = {
    "com.leesuchan.infra.database"
})
@EntityScan(basePackages = {
    "com.leesuchan.account.domain.model",
    "com.leesuchan.activity.domain.model"
})
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}
