package com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.ports.in.ArtifactRepository;
import com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.ports.out.NexusRepository;
import com.semarchy.khufu.artifactRepository.infrastructure.persistence.NexusRepositoryImpl;
import com.semarchy.khufu.artifactRepository.shared.constants.ConnectorRepositoryContants;

@RestController
@RequestMapping("/api/connectorRepository/v1")
public class ConnectorRepositoryController implements ArtifactRepository {

	@Autowired
	private final NexusRepository nexusService;

	public ConnectorRepositoryController(NexusRepositoryImpl nexusService) {
		this.nexusService = nexusService;
	}

	@Override
	@GetMapping("/artifacts/{" + ConnectorRepositoryContants.ARTIFACT_NAME + "}/{"
			+ ConnectorRepositoryContants.ARTIFACT_TYPE + "}")
	public ResponseEntity<List<String>> getAllArtifacts(
			@PathVariable(ConnectorRepositoryContants.ARTIFACT_NAME) String artifactName,
			@PathVariable(ConnectorRepositoryContants.ARTIFACT_TYPE) String artifactType) {
		return nexusService.getArtifactVersions(artifactName, artifactType);
	}

	@Override
	@GetMapping("/artifacts/{" + ConnectorRepositoryContants.ARTIFACT_NAME + "}/{"
			+ ConnectorRepositoryContants.ARTIFACT_TYPE + "}/{" + ConnectorRepositoryContants.ARTIFACT_VERSION + "}")
	public ResponseEntity<InputStreamResource> downloadArtifacts(
			@PathVariable(ConnectorRepositoryContants.ARTIFACT_NAME) String artifactName,
			@PathVariable(ConnectorRepositoryContants.ARTIFACT_TYPE) String artifactType,
			@PathVariable(ConnectorRepositoryContants.ARTIFACT_VERSION) String artifactVersion) {
		return nexusService.downloadArtifact(artifactName, artifactType, artifactVersion);
	}

    @PostMapping("/artifacts/{" + ConnectorRepositoryContants.ARTIFACT_NAME + "}/{"
			+ ConnectorRepositoryContants.ARTIFACT_TYPE + "}/{" + ConnectorRepositoryContants.ARTIFACT_VERSION + "}")
    public ResponseEntity<Void> uploadArtifact(
    		@PathVariable(ConnectorRepositoryContants.ARTIFACT_NAME) String artifactName,
			@PathVariable(ConnectorRepositoryContants.ARTIFACT_TYPE) String artifactType,
			@PathVariable(ConnectorRepositoryContants.ARTIFACT_VERSION) String artifactVersion,
            @RequestParam("file") MultipartFile file) {

        try (InputStream inputStream = file.getInputStream()) {
        	nexusService.uploadArtifact(inputStream ,artifactName,artifactType, artifactVersion );
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
	
}
