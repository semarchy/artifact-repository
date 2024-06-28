package com.semarchy.khufu.artifactRepository.connectorRepository.infrastructure.persistence.v1;

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
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.semarchy.khufu.artifactRepository.connectorRepository.ports.in.v1.ConnectorRepositoryController.ArtifactRequest;
import com.semarchy.khufu.artifactRepository.connectorRepository.domain.service.v1.*;

@Repository
public class NexusRepositoryImpl implements ArtifactService {

	private final Logger logger = LogManager.getLogger(getClass());
	private final List<String> ALLOWED_NEXUS_FILE_TYPES = Collections.unmodifiableList(Arrays.asList(ZIP_FILE_TYPE));

	private static final String GENERATION_BUNDLE = "generationBundle";
	private static final String RUNTIME_BUNDLE = "runtimeBundle";

	private static final String ZIP_FILE_TYPE = "zip";

	// Constant HashMap
	private static final Map<String, String> ARTIFACT_TYPE_MAP;

	static {
		ARTIFACT_TYPE_MAP = new HashMap<>();
		ARTIFACT_TYPE_MAP.put(GENERATION_BUNDLE, ZIP_FILE_TYPE);
		ARTIFACT_TYPE_MAP.put(RUNTIME_BUNDLE, ZIP_FILE_TYPE);
	}

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

	@Autowired
	public NexusRepositoryImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/////////////

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<String> getArtifactVersions(String artifactName, String artifactType) {

		try {
			String nexusArtifactType = getNexusArtifactType(artifactType);

			String url = String.format(
					"%s" + NexusContants.FIND_ARTIFACT_NEXUS_V1_API_URL + "?"
							+ NexusContants.MAVEN_REPOSITORY_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
							+ NexusContants.MAVEN_ARTIFACT_NAME_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
							+ NexusContants.MAVEN_EXTENSION_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s",
					nexusApiUrl, nexusRepositoryName, artifactName, nexusArtifactType);
			logger.info("NEXUS Artifact Search URL: {}", url);

			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, getEntity(), Map.class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				String errorMessage = String.format("An error occurred while retrieving %s artifact versions",
						artifactName);
				logger.error(errorMessage + ". Status code: " + response.getStatusCode());
				throw new RuntimeException(errorMessage);
			}

			List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody()
					.get(NexusContants.ITEMS_OUTPUT_FIELD_FOR_NEXUS_V1_API_URL);
			if (items == null) {
				logger.warn("No items found in response");
				return new ArrayList<>();
			}

			// read request's response and get versions
			List<String> versions = new ArrayList<>();
			for (Map<String, Object> item : items) {
				Map<String, String> mavenAttributeMap = (HashMap<String, String>) item
						.get(NexusContants.MAVEN2_OUTPUT_FIELD_FOR_NEXUS_V1_API_URL);
				String extension = mavenAttributeMap.get(NexusContants.EXTENSION_OUTPUT_FIELD_FOR_NEXUS_V1_API_URL);
				String version = mavenAttributeMap.get(NexusContants.VERSION_OUTPUT_FIELD_FOR_NEXUS_V1_API_URL);
				if (ALLOWED_NEXUS_FILE_TYPES.contains(extension)) {
					versions.add(version);
				}
			}
			return versions;

		} catch (Exception e) {
			String errorMessage = String.format("An error occurred while retrieving %s artifact versions",
					artifactName);
			logger.error(errorMessage, e);
			throw new RuntimeException(errorMessage);
		}

	}

	@Override
	public ResponseEntity<InputStreamResource> downloadArtifacts(List<ArtifactRequest> artifactRequestList)
			throws Exception {
		ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
		try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(zipOutputStream)) {
			for (ArtifactRequest artifactRequest : artifactRequestList) {
				ResponseEntity<InputStreamResource> responseEntity = downloadArtifact(artifactRequest.artifactName(),
						artifactRequest.artifactType(), artifactRequest.artifactVersion());
				InputStreamResource artifactStreamResource = responseEntity.getBody();
				if (artifactStreamResource != null) {
					try (InputStream artifactStream = artifactStreamResource.getInputStream()) {
						zos.putNextEntry(new java.util.zip.ZipEntry(artifactRequest.artifactName() + "-"
								+ artifactRequest.artifactVersion() + "." + artifactRequest.artifactType()));
						byte[] buffer = new byte[1024];
						int len;
						while ((len = artifactStream.read(buffer)) > -1) {
							zos.write(buffer, 0, len);
						}
						zos.closeEntry();
					}
				}
			}
		}

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipOutputStream.toByteArray());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=artifacts.zip");
		return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(byteArrayInputStream));
	}

	private ResponseEntity<InputStreamResource> downloadArtifact(String artifactName, String artifactType,
			String artifactVersion) {
		try {
			String nexusArtifactType = getNexusArtifactType(artifactType);

			String searchUrl = String.format(
					"%s" + NexusContants.DOWNLOAD_ARTIFACT_NEXUS_V1_API_URL + "?"
							+ NexusContants.MAVEN_REPOSITORY_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
							+ NexusContants.MAVEN_ARTIFACT_NAME_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
							+ NexusContants.MAVEN_EXTENSION_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s&"
							+ NexusContants.MAVEN_BASE_VERSION_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s",
					nexusApiUrl, nexusRepositoryName, artifactName, nexusArtifactType, artifactVersion);
			logger.info("NEXUS Artifact Download URL: {}", searchUrl);

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
					String errorMessage = String.format("Failed to download %s artifact", artifactName);
					logger.error(errorMessage);
					throw new ArtifactNotFoundException(errorMessage);
				}
			};

			ByteArrayInputStream artifactStream = restTemplate.execute(searchUrl, HttpMethod.GET, requestCallback,
					responseExtractor);
			if (artifactStream == null) {
				String errorMessage = String.format("{} artifact stream is null.", artifactName);
				;
				logger.error(errorMessage);
				throw new ArtifactNotFoundException(errorMessage);
			}

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Disposition",
					"attachment; filename=" + artifactName + "-" + artifactVersion + "." + artifactType);

			return ResponseEntity.ok().headers(responseHeaders).contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(new InputStreamResource(artifactStream));

		} catch (Exception e) {
			String errorMessage = String.format("An error occurred while downloading %s artifact versions",
					artifactName);
			logger.error(errorMessage, e);
			throw new RuntimeException(errorMessage);
		}
	}

	@Override
	public ResponseEntity<Object> uploadArtifact(InputStream artifactStream, String artifactName, String artifactType,
			String artifactVersion) throws Exception {
		try {
			String nexusArtifactType = getNexusArtifactType(artifactType);

			String url = String.format(
					"%s" + NexusContants.UPLOAD_ARTIFACT_NEXUS_V1_API_URL + "?"
							+ NexusContants.MAVEN_REPOSITORY_PARAMETER_FOR_NEXUS_V1_API_URL + "=%s",
					nexusApiUrl, nexusRepositoryName);
			logger.info("NEXUS Artifact Upload URL: {}", url);

			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(nexusUsername, nexusPassword);
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpHeaders fileHeaders = new HttpHeaders();
			fileHeaders.setContentDispositionFormData("maven2.asset1",
					artifactName + "-" + artifactVersion + "." + nexusArtifactType);

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
			body.add("maven2.asset1.extension", nexusArtifactType);

			// Add the file part
			ByteArrayResource byteArrayResource = new ByteArrayResource(byteArray) {
				@Override
				public String getFilename() {
					return artifactName + "-" + artifactVersion + "." + nexusArtifactType;
				}
			};
			body.add("maven2.asset1", byteArrayResource);
			// Create the request entity
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

			if (!response.getStatusCode().is2xxSuccessful()) {
				String errorMessage = String.format("An error occurred while uploading %s artifact", artifactName);
				logger.error(errorMessage + ". Status code: " + response.getStatusCode());
				throw new RuntimeException(errorMessage);
			}
		} catch (Exception e) {
			String errorMessage = String.format("An error occurred while uploading %s artifact", artifactName);
			logger.error(errorMessage, e);
			throw new RuntimeException(errorMessage);
		}
		return null;
	}

	private String getNexusArtifactType(String artifactType) throws Exception {
		String realArtifactType = ARTIFACT_TYPE_MAP.get(artifactType);
		if (null == realArtifactType) {
			throw new UnexistingArtifactTypeException("invalid artifact type");
		}
		return realArtifactType;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private HttpEntity<?> getEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(nexusUsername, nexusPassword);
		return new HttpEntity(headers);
	}

}