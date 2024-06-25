package com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.ports.in;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.application.ConnectorRepositoryController.ArtifactRequest;

public interface ArtifactRepository {

	ResponseEntity<List<String>> getAllArtifacts(ArtifactRequest artifactRequest);

	ResponseEntity<InputStreamResource> downloadArtifact(ArtifactRequest artifactRequest);

	ResponseEntity<Object> uploadArtifact(String artifactName, String artifactType, String artifactVersion,
			MultipartFile file);

	ResponseEntity<InputStreamResource> downloadArtifacts(List<ArtifactRequest> artifactRequestList);


}
