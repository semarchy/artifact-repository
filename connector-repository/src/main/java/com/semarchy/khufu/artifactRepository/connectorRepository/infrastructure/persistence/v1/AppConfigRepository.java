package com.semarchy.khufu.artifactRepository.connectorRepository.infrastructure.persistence.v1;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import com.semarchy.khufu.artifactRepository.connectorRepository.domain.service.v1.*;
import org.springframework.context.annotation.*;

@Repository
@PropertySource("fonctionnal-properties.properties")
public class AppConfigRepository implements PropertiesService {

	private final List<String> types;

	@Autowired
	public AppConfigRepository(@Value("${app.artifacts.types}") List<String> types) {
		this.types = types;
	}

	public List<String> getArtifactTypes() {
		return types;
	}

}