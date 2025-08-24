package com.loadmapguide_backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.loadmapguide_backend.domain.*.repository")
@EnableJpaAuditing
public class JpaConfig {
}