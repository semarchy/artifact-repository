package com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.application;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.ports.in.ArtifactRepository;
import com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.ports.out.NexusRepository;
import com.semarchy.khufu.artifactRepository.infrastructure.persistence.ArtifactNotFoundException;
import com.semarchy.khufu.artifactRepository.infrastructure.persistence.NexusRepositoryImpl;

@RestController
@RequestMapping(ConnectorRepositoryController.API_CONNECTOR_REPOSITORY_V1)
public class ConnectorRepositoryController implements ArtifactRepository {
	
	public static final String API_CONNECTOR_REPOSITORY_V1 = "/api/connectorRepository/v1";
	
	@Autowired
	private final NexusRepository nexusService;

	public ConnectorRepositoryController(NexusRepositoryImpl nexusService) {
		this.nexusService = nexusService;
	}

	@Override
	@GetMapping("/artifacts/versions")
	public ResponseEntity<List<String>> getAllArtifacts(@RequestBody ArtifactRequest artifactRequest) {
		try {
			List<String> versions = nexusService.getArtifactVersions(artifactRequest.artifactName(),
					artifactRequest.artifactType());
			return ResponseEntity.ok().body(versions);
		} catch (ArtifactNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@Override
	@GetMapping("/artifacts/download")
	public ResponseEntity<InputStreamResource> downloadArtifact(@RequestBody ArtifactRequest artifactRequest) {
		try {
			return nexusService.downloadArtifact(artifactRequest.artifactName(), artifactRequest.artifactType(),
					artifactRequest.artifactVersion());
		} catch (ArtifactNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@Override
	@GetMapping("/artifacts/downloadByRequirements")
	public ResponseEntity<InputStreamResource> downloadArtifacts(@RequestBody List<ArtifactRequest> artifactRequestList) {
		try {
			return nexusService.downloadArtifacts(artifactRequestList);
		} catch (ArtifactNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	
	
	@Override
	@PostMapping("/artifacts/upload")
	public ResponseEntity<Object> uploadArtifact(@RequestParam String artifactName, @RequestParam String artifactType,
			@RequestParam String artifactVersion, @RequestParam("file") MultipartFile file) {

		try (InputStream inputStream = file.getInputStream()) {
			return nexusService.uploadArtifact(inputStream, artifactName, artifactType, artifactVersion);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	public record ArtifactRequest(String artifactName, String artifactType, String artifactVersion) {
	}

}
