package com.springcloud.demo.roomsmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RoomsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomsMicroserviceApplication.class, args);
	}

}
