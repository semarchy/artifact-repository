package com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.ports.out;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.application.ConnectorRepositoryController.ArtifactRequest;

public interface NexusRepository {

	List<String> getArtifactVersions(String artifactName, String artifactType)  throws Exception;

	ResponseEntity<InputStreamResource> downloadArtifact(String artifactName, String artifactType,
			String artifactVersion)  throws Exception;

	ResponseEntity<Object> uploadArtifact(InputStream artifactStream, String artifactName, String artifactType, String artifactVersion) throws Exception;

	ResponseEntity<InputStreamResource> downloadArtifacts(List<ArtifactRequest> artifactRequestList)  throws Exception;;

}
