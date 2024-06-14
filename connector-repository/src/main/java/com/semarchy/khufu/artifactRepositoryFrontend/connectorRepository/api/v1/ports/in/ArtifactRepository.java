package com.semarchy.khufu.artifactRepositoryFrontend.connectorRepository.api.v1.ports.in;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

public interface ArtifactRepository {

	ResponseEntity<List<String>> getAllArtifacts(String artifactName, String artifactType);

	ResponseEntity<InputStreamResource> downloadArtifacts(String artifactName, String artifactType,
			String artifactVersion)  ;

}
