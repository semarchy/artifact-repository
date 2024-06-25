package com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.application.test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.semarchy.khufu.artifactRepository.connectorRepository.api.v1.application.ConnectorRepositoryController;
import com.semarchy.khufu.artifactRepository.infrastructure.persistence.NexusRepositoryImpl;

@WebMvcTest(ConnectorRepositoryController.class)
public class ConnectorRepositoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private NexusRepositoryImpl nexusService;

	@InjectMocks
	private ConnectorRepositoryController connectorRepositoryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

	@Test
	public void getAllArtifacts_success() throws Exception {
		// Mock data
		// Create JSON payload
		String payload = """
				{
				    "artifactName": "core",
				    "artifactType": "generationBundle"
				}
				""";

		List<String> artifactVersions = Arrays.asList("1.0", "2.0");

		// Mock service method
		given(nexusService.getArtifactVersions(any(), any())).willReturn(artifactVersions);

		// Perform GET request
		try {
			ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
					.get(ConnectorRepositoryController.API_CONNECTOR_REPOSITORY_V1 + "/artifacts/versions")
					.contentType(MediaType.APPLICATION_JSON).content(payload));
			
			System.out.println(resultActions.toString());
			
			resultActions.andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$[0]").value("1.0")).andExpect(jsonPath("$[1]").value("2.0"));

		} catch (Exception e) {
			fail(e);
		}
	}

}