package com.semarchy.khufu.artifactRepositoryFrontend.connectorRepository.api.v1.adapters.out;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.semarchy.khufu.artifactRepositoryFrontend.connectorRepository.api.v1.ports.out.NexusRepository;

@Service
public class NexusRepositoryImpl implements NexusRepository {

	private final Logger logger = LogManager.getLogger(getClass());
	private final List<String> ALLOWED_FILE_TYPES = Collections.unmodifiableList(Arrays.asList("zip", "js"));

	@Value("${nexus.api.url}")
	protected String nexusApiUrl;

	@Value("${nexus.username}")
	protected String nexusUsername;

	@Value("${nexus.password}")
	protected String nexusPassword;

	@Value("${nexus.repositoryName}")
	protected String nexusRepositoryName;

	@Autowired
	private final RestTemplate restTemplate;

	public NexusRepositoryImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/////////////

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ResponseEntity<List<String>> getArtifactVersions(String artifactName, String artifactType) {

		String url = String.format(
				"%s" + NexusContants.FIND_ARTIFACT_NEXUS_V1_API_URL + "?"
						+ NexusContants.MAVEN_REPOSITORY_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
						+ NexusContants.MAVEN_ARTIFACT_NAME_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
						+ NexusContants.MAVEN_EXTENSION_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s",
				nexusApiUrl, nexusRepositoryName, artifactName, artifactType);
		logger.info("Search URL: {}", url);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, getEntity(), Map.class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				logger.error("Failed to retrieve artifact versions. Status code: {}", response.getStatusCode());
				return ResponseEntity.status(response.getStatusCode()).build();
			}

			List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody()
					.get(NexusContants.ITEMS_OUTPUT_FIELD_FOR_NEXUS_V1_API_URL);
			if (items == null) {
				logger.warn("No items found in response");
				return ResponseEntity.ok().body(new ArrayList<>());
			}

			List<String> versions = new ArrayList<>();
			for (Map<String, Object> item : items) {
				Map<String, String> mavenAttributeMap = (HashMap<String, String>) item
						.get(NexusContants.MAVEN2_OUTPUT_FIELD_FOR_NEXUS_V1_API_URL);
				String extension = mavenAttributeMap.get(NexusContants.EXTENSION_OUTPUT_FIELD_FOR_NEXUS_V1_API_URL);
				String version = mavenAttributeMap.get(NexusContants.VERSION_OUTPUT_FIELD_FOR_NEXUS_V1_API_URL);
				if (ALLOWED_FILE_TYPES.contains(extension)) {
					versions.add(version);
				}
			}
			return ResponseEntity.ok().body(versions);

		} catch (Exception e) {
			logger.error("An error occurred while retrieving artifact versions", e);
			return ResponseEntity.status(500).build();
		}

	}

	@Override
	public ResponseEntity<InputStreamResource> downloadArtifact(String artifactName, String artifactType,
			String artifactVersion) {
		String searchUrl = String.format(
				"%s" + NexusContants.DOWNLOAD_ARTIFACT_NEXUS_V1_API_URL + "?"
						+ NexusContants.MAVEN_REPOSITORY_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
						+ NexusContants.MAVEN_ARTIFACT_NAME_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
						+ NexusContants.MAVEN_EXTENSION_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
						+ NexusContants.MAVEN_BASE_VERSION_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s",
				nexusApiUrl, nexusRepositoryName, artifactName, artifactType, artifactVersion);
		logger.info("Search URL: {}", searchUrl);

		try {

			RequestCallback requestCallback = request -> request.getHeaders().setBasicAuth(nexusUsername,
					nexusPassword);
			ResponseExtractor<ByteArrayInputStream> responseExtractor = response -> {
				if (response.getStatusCode().is2xxSuccessful()) {
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					byte[] data = new byte[1024];
					int nRead;
					try (InputStream inputStream = response.getBody()) {
						while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
							buffer.write(data, 0, nRead);
						}
						buffer.flush();
					}
					return new ByteArrayInputStream(buffer.toByteArray());
				} else {
					logger.error("Failed to download artifact. Status code: {}", response.getStatusCode());
					throw new RuntimeException("Failed to download artifact.");
				}
			};

			ByteArrayInputStream artifactStream = restTemplate.execute(searchUrl, HttpMethod.GET, requestCallback,
					responseExtractor);
			if (artifactStream == null) {
				logger.error("Artifact stream is null.");
				return ResponseEntity.notFound().build();
			}

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Disposition",
					"attachment; filename=" + artifactName + "-" + artifactVersion + "." + artifactType);

			return ResponseEntity.ok().headers(responseHeaders).contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(new InputStreamResource(artifactStream));

		} catch (Exception e) {
			logger.error("An error occurred while downloading the artifact", e);
			return ResponseEntity.status(500).build();
		}
	}

	@Override
	public void uploadArtifact(InputStream artifactStream, String artifactName, String artifactType,
			String artifactVersion) {
		String url = String.format(
				"%s" + NexusContants.UPLOAD_ARTIFACT_NEXUS_V1_API_URL + "?"
						+ NexusContants.MAVEN_REPOSITORY_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s",
				nexusApiUrl, nexusRepositoryName);
		logger.info("Upload URL: {}", url);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(nexusUsername, nexusPassword);
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpHeaders fileHeaders = new HttpHeaders();
			fileHeaders.setContentDispositionFormData("maven2.asset1", artifactName + "-" + artifactVersion + ".zip");


	        // Read the InputStream into a byte array
	        byte[] byteArray;
	        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
	            int nRead;
	            byte[] data = new byte[16384];
	            while ((nRead = artifactStream.read(data, 0, data.length)) != -1) {
	                buffer.write(data, 0, nRead);
	            }
	            byteArray = buffer.toByteArray();
	        } catch (IOException e) {
	            throw new RuntimeException("Failed to read input stream", e);
	        }

	        // Create the body as a LinkedMultiValueMap
	        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
	        body.add("maven2.groupId", "com.semarchy.khufu");
	        body.add("maven2.artifactId", artifactName);
	        body.add("maven2.version", artifactVersion);
	        body.add("maven2.asset1.extension", artifactType);

	        // Add the file part
	        ByteArrayResource byteArrayResource = new ByteArrayResource(byteArray) {
	            @Override
	            public String getFilename() {
	                return artifactName + "-" + artifactVersion + ".zip";
	            }
	        };
	        body.add("maven2.asset1", byteArrayResource);
			// Create the request entity
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException("Failed to upload artifact. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.error("An error occurred while uploading the artifact", e);
			throw e;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private HttpEntity<?> getEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(nexusUsername, nexusPassword);
		return new HttpEntity(headers);
	}

}