package com.semarchy.khufu.artifactRepository.connectorRepository.ports.out.v1;

import java.io.InputStream;
import java.util.*;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import com.semarchy.khufu.artifactRepository.connectorRepository.ports.in.v1.ConnectorRepositoryController.ArtifactRequest;

public interface NexusRepository {

	List<String> getArtifactVersions(String artifactName, String artifactType)  throws Exception;

	ResponseEntity<InputStreamResource> downloadArtifact(String artifactName, String artifactType,
			String artifactVersion)  throws Exception;

	ResponseEntity<Object> uploadArtifact(InputStream artifactStream, String artifactName, String artifactType, String artifactVersion) throws Exception;

	ResponseEntity<InputStreamResource> downloadArtifacts(List<ArtifactRequest> artifactRequestList)  throws Exception;

	Set<String> getArtifactTypes();

}
