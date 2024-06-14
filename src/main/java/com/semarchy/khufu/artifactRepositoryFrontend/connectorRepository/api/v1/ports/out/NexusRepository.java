package com.semarchy.khufu.artifactRepositoryFrontend.connectorRepository.api.v1.ports.out;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

public interface NexusRepository {

	ResponseEntity<List<String>> getArtifactVersions(String artifactName, String artifactType);

	ResponseEntity<InputStreamResource> downloadArtifact(String artifactName, String artifactType,
			String artifactVersion);

	void uploadArtifact(InputStream artifactStream, String artifactName, String artifactType, String artifactVersion);

}
