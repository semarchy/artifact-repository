package com.semarchy.khufu.artifactRepository.connectorRepository.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication(scanBasePackages = "com.semarchy.khufu.artifactRepository.connectorRepository")
public class ArtifactRepositoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtifactRepositoryApplication.class, args);
	}

}
