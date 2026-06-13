package sk.mkrajcovic.travelx.config;

import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;
import static java.lang.Boolean.TRUE;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.catalog.ModelCatalog;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiModelCatalog;
import lombok.RequiredArgsConstructor;

/**
 * Spring configuration for OpenAI-compatible model access.
 *
 * <p>
 * This configuration defines the beans required to interact with OpenAI models
 * via LangChain4j.
 */
@Configuration
@RequiredArgsConstructor
public class OpenAiClientConfiguration {

	private final OpenAiProperties openAiProperties;

	/**
	 * Provides a catalog of available OpenAI models that can be queried or selected
	 * at runtime. Useful for retrieving metadata about models or dynamically
	 * switching models if needed.
	 */
	@Bean
	ModelCatalog modelCatalog() {
		return OpenAiModelCatalog.builder()
			.baseUrl(openAiProperties.getBaseUrl())
			.apiKey(openAiProperties.getApiKey())
			.logRequests(TRUE)
			.logResponses(TRUE)
			.build();
	}

	/**
	 * This bean provides the primary interface for sending chat messages and
	 * receiving text-based responses from the configured model.
	 */
	@Bean
	OpenAiChatModel textOpenAiChatModel() {
		return defaultOpenAiChatModelBuilder().build();
	}

	/**
	 * Responses are returned as JSON and validated against a strict JSON schema,
	 * making this model suitable for extraction and other structured-output use
	 * cases.
	 */
	@Bean
	OpenAiChatModel structuredOpenAiChatModel() {
		return defaultOpenAiChatModelBuilder()
			.responseFormat(JSON)
			.strictJsonSchema(TRUE)
			.build();
	}

	private OpenAiChatModelBuilder defaultOpenAiChatModelBuilder() {
		return OpenAiChatModel.builder()
			.baseUrl(openAiProperties.getBaseUrl())
			.apiKey(openAiProperties.getApiKey())
			.modelName(openAiProperties.getModel())
			.logRequests(TRUE)
			.logResponses(TRUE);
	}

	/**
	 * Creates an OpenAI-compatible embedding model for RAG ingestion and retrieval.
	 *
	 * <p>
	 * Uses the remote embedding endpoint (pure HTTP) instead of the default local
	 * ONNX model ({@code langchain4j-embeddings-bge-small-en-v15-q}) which requires
	 * {@code libtokenizers.so} compiled against GLIBC 2.32+. This avoids the
	 * {@link UnsatisfiedLinkError} on systems with GLIBC less than 2.32.
	 *
	 * <p>
	 * The same model instance MUST be used for both document ingestion and query
	 * retrieval - mixing models produces incompatible vector spaces and breaks
	 * semantic search.
	 */
	@Bean
	OpenAiEmbeddingModel openAiEmbeddingModel() {
		return OpenAiEmbeddingModel.builder()
			.baseUrl(openAiProperties.getBaseUrl())
			.apiKey(openAiProperties.getApiKey())
			.modelName(openAiProperties.getEmbeddingModel())
			.logRequests(TRUE)
			.logResponses(TRUE)
			.build();
	}

}
