package sk.mkrajcovic.travelx.config;

import static lombok.AccessLevel.NONE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration properties for OpenAI integration.
 *
 * <p>
 * Maps values from the {@code openai.*} configuration namespace and provides
 * validated runtime configuration required to initialize the AI client.
 *
 * <p>
 * This class ensures that mandatory properties such as API key and model name
 * are present at application startup. If they are missing, the application will
 * fail fast via bean validation.
 *
 * <p>
 * If {@code baseUrl} is not explicitly configured, it defaults to OpenAI's
 * public API endpoint.
 */
@Configuration
@ConfigurationProperties("openai")
@Validated
@Getter @Setter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = NONE)
public class OpenAiProperties {

	private static final Logger LOG = LoggerFactory.getLogger(OpenAiProperties.class);

	private static final String OPEN_AI_API_URL = "https://api.openai.com/v1";

	private String baseUrl;

	@NotBlank(message = "openai.api-key configuration is missing! Cannot proceed without it.")
	private String apiKey;

	@NotBlank(message = "openai.model configuration is missing! Cannot proceed without it.")
	private String model;

	@NotBlank(message = "openai.embedding-model configuration is missing! RAG requires an embedding model. Cannot proceed without it.")
	private String embeddingModel;

	@PostConstruct
	private void initialize() {
		setDefaultBaseUrlIfMissing();
		LOG.info("OpenAI server/host is set to {}", baseUrl);
		LOG.info("OpenAI model: {}", model);
		LOG.info("OpenAI embedding model: {}", embeddingModel);
	}

	private void setDefaultBaseUrlIfMissing() {
		if (baseUrl == null) {
			baseUrl = OPEN_AI_API_URL;
			LOG.debug("No explicit OpenAI host defined, applying defaults.");
		}
	}
}
