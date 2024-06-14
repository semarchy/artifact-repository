package com.semarchy.khufu.artifactRepository.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.ports.out.NexusRepository;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConnectorRepositoryControllerTest {

	@Autowired
    private MockMvc mockMvc;

    @Mock
    private NexusRepository nexusService;

    @Value("${nexus.api.url}")
    private String nexusUrl;

    @Test
    public void getAllArtifacts_success() throws Exception {
        // Mock data
        String artifactName = "testArtifact";
        String artifactType = "jar";
        List<String> artifactVersions = Arrays.asList("1.0", "2.0");

        // Mock service method
        given(nexusService.getArtifactVersions(any(), any()))
                .willReturn(ResponseEntity.ok(artifactVersions));

        // Perform GET request
        mockMvc.perform(MockMvcRequestBuilders.get("/artifacts/{artifactName}/{artifactType}", artifactName, artifactType)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("1.0"))
                .andExpect(jsonPath("$[1]").value("2.0"));
    }

    @Test
    public void downloadArtifact_success() throws Exception {
        // Mock data
        String artifactName = "testArtifact";
        String artifactType = "jar";
        String artifactVersion = "1.0";

        // Mock InputStreamResource
        byte[] contentBytes = "test data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(contentBytes);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        // Mock service method
        given(nexusService.downloadArtifact(any(), any(), any()))
                .willReturn(ResponseEntity.ok().body(inputStreamResource));

        // Perform GET request
        mockMvc.perform(MockMvcRequestBuilders.get("/artifacts/{artifactName}/{artifactType}/{artifactVersion}", artifactName, artifactType, artifactVersion)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        // Additional assertions can be added based on your actual response handling
    }

    @Test
    public void uploadArtifact_success() throws Exception {
        // Mock data
        String artifactName = "testArtifact";
        String artifactType = "jar";
        String artifactVersion = "1.0";

        MockMultipartFile file = new MockMultipartFile("file", "testFile.jar", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test data".getBytes());

        // Perform POST request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/artifacts/{artifactName}/{artifactType}/{artifactVersion}", artifactName, artifactType, artifactVersion)
                .file(file))
                .andExpect(status().isOk());
        // Additional assertions can be added based on your actual response handling
    }
	
}