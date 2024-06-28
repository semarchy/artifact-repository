package com.semarchy.khufu.artifactRepository.connectorRepository.domain.service.v1;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import com.semarchy.khufu.artifactRepository.connectorRepository.ports.in.v1.ConnectorRepositoryController.ArtifactRequest;

public interface ArtifactService {

	List<String> getArtifactVersions(String artifactName, String artifactName2);

	ResponseEntity<Object> uploadArtifact(InputStream inputStream, String artifactName, String artifactType,
			String artifactVersion) throws Exception;

	ResponseEntity<InputStreamResource> downloadArtifacts(List<ArtifactRequest> artifactRequestList) throws Exception;

 
}
