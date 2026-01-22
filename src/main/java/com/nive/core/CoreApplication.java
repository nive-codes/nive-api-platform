package com.nive.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.nive")	//이렇게 해야 com.nive 멀티 모듈의 web의 security config의 설정 적용
@EnableJpaRepositories(basePackages = "com.nive.domain") //domain 위치 지정(멀티모듈)
@EntityScan(basePackages = "com.nive.domain") //entity 위치 지정(멀티 모듈)
public class CoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreApplication.class, args);
	}

}
