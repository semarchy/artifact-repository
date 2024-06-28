package com.semarchy.khufu.artifactRepository.connectorRepository.ports.in.v1;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.semarchy.khufu.artifactRepository.connectorRepository.domain.service.v1.ArtifactService;
import com.semarchy.khufu.artifactRepository.connectorRepository.domain.service.v1.PropertiesService;
import com.semarchy.khufu.artifactRepository.connectorRepository.ports.in.v1.ConnectorRepositoryController.ArtifactRequest;

@Service
public class ConnectorRepositoryService {

	private final ArtifactService artifactService;
	private final PropertiesService propertiesService;

	public ConnectorRepositoryService(ArtifactService artifactBusinessService, PropertiesService propertiesService) {
		this.artifactService = artifactBusinessService;
		this.propertiesService = propertiesService;
	}

	public List<String> getArtifactVersions(String artifactName, String artifactType) {
		return artifactService.getArtifactVersions(artifactName, artifactType);
	}

	public ResponseEntity<Object> uploadArtifact(InputStream inputStream, String artifactName, String artifactType,
			String artifactVersion) throws Exception {
		return artifactService.uploadArtifact(inputStream, artifactName, artifactType, artifactVersion);
	}

	public ResponseEntity<InputStreamResource> downloadArtifacts(List<ArtifactRequest> artifactRequestList)
			throws Exception {
		return artifactService.downloadArtifacts(artifactRequestList);
	}

	public List<String> getArtifactTypes() {
		return propertiesService.getArtifactTypes();
	}
}