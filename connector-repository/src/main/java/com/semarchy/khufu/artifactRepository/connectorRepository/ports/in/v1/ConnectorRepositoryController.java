package com.semarchy.khufu.artifactRepository.connectorRepository.ports.in.v1;

import java.io.InputStream;
import java.util.*;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.semarchy.khufu.artifactRepository.connectorRepository.infrastructure.persistence.v1.ArtifactNotFoundException;

@RestController
@RequestMapping(ConnectorRepositoryController.API_CONNECTOR_REPOSITORY_V1)
public class ConnectorRepositoryController {

	public static final String API_CONNECTOR_REPOSITORY_V1 = "/api/v1";

	private final ConnectorRepositoryService connectorRepositoryService;

	public ConnectorRepositoryController(ConnectorRepositoryService connectorRepositoryService) {
		this.connectorRepositoryService = connectorRepositoryService;
	}

	@GetMapping("/artifacts/types")
	public ResponseEntity<List<String>> getArtifactTypes() {
		try {
			List<String> types = connectorRepositoryService.getArtifactTypes();
			return ResponseEntity.ok().body(types);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/artifacts/versions")
	public ResponseEntity<List<String>> getAllArtifacts(@RequestBody ArtifactRequest artifactRequest) {
		try {
			List<String> versions = connectorRepositoryService.getArtifactVersions(artifactRequest.artifactName(),
					artifactRequest.artifactType());
			return ResponseEntity.ok().body(versions);
		} catch (ArtifactNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/artifacts")
	public ResponseEntity<InputStreamResource> downloadArtifacts(
			@RequestBody List<ArtifactRequest> artifactRequestList) {
		try {
			return connectorRepositoryService.downloadArtifacts(artifactRequestList);
		} catch (ArtifactNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/artifacts")
	public ResponseEntity<Object> uploadArtifact(@RequestParam String artifactName, @RequestParam String artifactType,
			@RequestParam String artifactVersion, @RequestParam("file") MultipartFile file) {
		try (InputStream inputStream = file.getInputStream()) {
			return connectorRepositoryService.uploadArtifact(inputStream, artifactName, artifactType, artifactVersion);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	public record ArtifactRequest(String artifactName, String artifactType, String artifactVersion) {
	}

}
