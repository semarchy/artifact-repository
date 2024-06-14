package com.semarchy.khufu.artifactRepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.semarchy.khufu.artifactRepository")
public class ArtifactRepositoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtifactRepositoryApplication.class, args);
	}

}
