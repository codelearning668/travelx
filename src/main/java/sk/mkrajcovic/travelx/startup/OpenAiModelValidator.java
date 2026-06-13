package sk.mkrajcovic.travelx.startup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import dev.langchain4j.model.catalog.ModelCatalog;
import dev.langchain4j.model.catalog.ModelDescription;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import sk.mkrajcovic.travelx.config.OpenAiProperties;

/**
 * Startup validator that verifies the configured OpenAI chat and embedding
 * models are supported by the configured OpenAI-compatible API (e.g., OpenAI or
 * a LiteLLM proxy).
 *
 * <p>
 * During application startup, it retrieves the list of available models and
 * validates that:
 * <ul>
 * <li>the configured chat model exists in the remote model catalog,</li>
 * <li>the configured embedding model exists in the remote model catalog,
 * and</li>
 * <li>the configured embedding model can successfully serve requests through
 * the {@code /v1/embeddings} endpoint.</li>
 * </ul>
 *
 * <p>
 * The embedding endpoint probe is necessary because some providers expose
 * models through {@code /v1/models} that are not actually capable of generating
 * embeddings. A lightweight startup embedding request detects such
 * misconfigurations early and prevents failures during document ingestion or
 * vectorization workflows.
 *
 * <p>
 * The application fails fast when any validation check fails, providing
 * immediate feedback about invalid model configuration, unsupported models,
 * embedding capability mismatches, or connectivity issues with the configured
 * API endpoint.
 */
@Component
@RequiredArgsConstructor
public class OpenAiModelValidator implements ApplicationRunner {

	private static final Logger LOG = LoggerFactory.getLogger(OpenAiModelValidator.class);

	private final OpenAiProperties openAiProperties;
	private final ModelCatalog modelCatalog;
	private final OpenAiEmbeddingModel openAiEmbeddingModel;

	@Override
	public void run(ApplicationArguments args) {
		List<ModelDescription> remoteModels = modelCatalog.listModels();
		validateChatModel(remoteModels);
		validateEmbeddingModel(remoteModels);
	}

	private void validateChatModel(List<ModelDescription> remoteModels) {
		LOG.info("Validating OpenAI chat model '{}' against available models...", openAiProperties.getModel());
		if (remoteModels.stream().noneMatch(m -> openAiProperties.getModel().equals(m.name()))) {
			throw new IllegalStateException(
				"Configured OpenAI chat model is NOT supported by API: " + openAiProperties.getModel() +
				"; use one of " + extractModelNames(remoteModels)
			);
		}
		LOG.info("OpenAI chat model '{}' is valid and supported.", openAiProperties.getModel());
	}

	// this is not needed, the bean creation calls ingest(companyData) which tests the connection, that's why the firs log appears for embeddings
	private void validateEmbeddingModel(List<ModelDescription> remoteModels) {
		LOG.info("Validating OpenAI embedding model '{}' against available models...", openAiProperties.getEmbeddingModel());
		if (remoteModels.stream().noneMatch(m -> openAiProperties.getEmbeddingModel().equals(m.name()))) {
			throw new IllegalStateException(
				"Configured OpenAI embedding model is NOT supported by API: " + openAiProperties.getEmbeddingModel() +
				"; use one of " + extractModelNames(remoteModels)
			);
		}
		probeEmbeddingEndpoint();
		LOG.info("OpenAI embedding model '{}' is valid and supports embeddings.", openAiProperties.getEmbeddingModel());
	}

	/**
	 * Probes the {@code /v1/embeddings} endpoint with a minimal test input to confirm
	 * the configured model actually supports the embeddings API.
	 *
	 * <p>
	 * A model may appear in the {@code /v1/models} list but still fail embedding calls
	 * if it is a chat-only model. This probe catches that misconfiguration at startup
	 * rather than at runtime during document ingestion.
	 */
	private void probeEmbeddingEndpoint() {
		try {
			openAiEmbeddingModel.embed("startup probe");
			LOG.info("Embedding model '{}' confirmed working via /v1/embeddings.", openAiProperties.getEmbeddingModel());
		} catch (Exception e) {
			throw new IllegalStateException(
				"Embedding model '" + openAiProperties.getEmbeddingModel() +
				"' failed the /v1/embeddings probe at startup. " +
				"Ensure the configured model supports embeddings and the proxy is reachable.", e
			);
		}
	}

	private List<String> extractModelNames(List<ModelDescription> models) {
		return models.stream()
			.map(ModelDescription::name)
			.toList();
	}
}
