package sk.mkrajcovic.travelx.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import sk.mkrajcovic.travelx.chat.AircraftExtractor;
import sk.mkrajcovic.travelx.chat.Assistant;
import sk.mkrajcovic.travelx.chat.FlightExtractor;
import sk.mkrajcovic.travelx.memory.CondensingChatMemoryStore;
import sk.mkrajcovic.travelx.memory.PersistentChatMemoryStore;

@Configuration
public class AiServicesConfiguration {

	/**
	 * Configures conversation memory for the AI assistant.
	 *
	 * <p>
	 * {@link MessageWindowChatMemory} maintains a sliding window of the most recent
	 * chat messages and automatically includes them in subsequent model requests.
	 * This enables the assistant to retain conversational context across multiple
	 * user interactions.
	 *
	 * <p>
	 * To prevent unbounded memory growth and excessive token consumption, only the
	 * latest 10 messages are retained. When the limit is exceeded, the oldest
	 * messages are discarded from the active conversation window.
	 *
	 * <p>
	 * Conversation history is persisted using {@link PersistentChatMemoryStore},
	 * allowing chat memory to survive application restarts and be restored when
	 * needed.
	 */
	@Bean
	MessageWindowChatMemory messageWindowChatMemory(OpenAiChatModel textOpenAiChatModel) {
		return MessageWindowChatMemory.builder()
			.maxMessages(10)
			.chatMemoryStore(new CondensingChatMemoryStore(
				new PersistentChatMemoryStore(),
				textOpenAiChatModel,
				8))
//			.alwaysKeepSystemMessageFirst(Boolean.TRUE) // aky to ma dopad na vytvaranie novej spravy a memory chatu?
			.build();
	}

	/**
	 * LangChain4j dynamically generates an implementation of the {@link Assistant}
	 * interface at runtime. The generated proxy acts as a high-level facade over
	 * the configured language model and conversation memory.
	 *
	 * <p>
	 * When {@link Assistant#chat(dev.langchain4j.model.chat.request.ChatRequest)}
	 * is invoked, the generated implementation:
	 * <ol>
	 * 	<li>Retrieves relevant conversation history from the configured {@link dev.langchain4j.memory.ChatMemory}.</li>
	 * 	<li>Combines memory and request messages into a model request.</li>
	 * 	<li>Sends the request to the configured OpenAI-compatible model.</li>
	 * 	<li>Stores the interaction back into chat memory.</li>
	 * 	<li>Returns the generated response text.</li>
	 * </ol>
	 *
	 * <p>
	 * This assistant relies only on the language model and chat memory. It does not
	 * perform retrieval from the company's knowledge base and is intended for
	 * general conversational interactions.
	 */
	@Bean
	Assistant generalChatAssistant(OpenAiChatModel textOpenAiChatModel) {
		return AiServices.builder(Assistant.class)
			.chatModel(textOpenAiChatModel)
			.chatMemory(messageWindowChatMemory(textOpenAiChatModel))
			.build();
	}

	/**
	 * LangChain4j dynamically generates an implementation of the {@link Assistant}
	 * interface at runtime. The generated proxy acts as a high-level facade over
	 * the configured language model, conversation memory, and company knowledge
	 * base.
	 *
	 * <p>
	 * When {@link Assistant#chat(dev.langchain4j.model.chat.request.ChatRequest)}
	 * is invoked, the generated implementation:
	 * <ol>
	 * 	<li>Retrieves relevant conversation history from the configured {@link dev.langchain4j.memory.ChatMemory}.</li>
	 * 	<li>Searches the embedding store for company-specific content relevant to the user's request.</li>
	 * 	<li>Combines retrieved content, memory, and request messages into a model request.</li>
	 * 	<li>Sends the augmented request to the configured OpenAI-compatible model.</li>
	 * 	<li>Stores the interaction back into chat memory.</li>
	 * 	<li>Returns the generated response text.</li>
	 * </ol>
	 *
	 * <p>
	 * This assistant uses retrieval-augmented generation (RAG) via an
	 * {@link dev.langchain4j.rag.content.retriever.ContentRetriever} backed by an
	 * embedding store containing company knowledge. It is intended for answering
	 * questions that require company-specific context in addition to the model's
	 * general knowledge.
	 */
	@Bean
	Assistant companyKnowledgeAssistant(OpenAiChatModel textOpenAiChatModel, OpenAiEmbeddingModel openAiEmbeddingModel) {
		return AiServices.builder(Assistant.class)
			.chatModel(textOpenAiChatModel)
			.chatMemory(messageWindowChatMemory(textOpenAiChatModel))
			.contentRetriever(
				EmbeddingStoreContentRetriever.builder()
					.embeddingStore(embeddingStore(openAiEmbeddingModel))
					.embeddingModel(openAiEmbeddingModel)
					.build())
			.build();
	}

	private InMemoryEmbeddingStore<TextSegment> embeddingStore(OpenAiEmbeddingModel embeddingModel) {
		var documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/static/", new TextDocumentParser());
		var embeddingStore = new InMemoryEmbeddingStore<TextSegment>();

		// no-op splitter: returns each document as a single TextSegment.
		// Required because EmbeddingStoreIngestor.builder().build() unconditionally
		// tries to load RecursiveDocumentSplitterFactory which depends on
		// HuggingFaceTokenCountEstimator from the ONNX jar (excluded from classpath).
		DocumentSplitter noOpSplitter = document ->
			List.of(TextSegment.from(document.text(), document.metadata()));

		EmbeddingStoreIngestor.builder()
			.documentSplitter(noOpSplitter)
			.embeddingModel(embeddingModel)
			.embeddingStore(embeddingStore)
			.build()
			.ingest(documents);

		return embeddingStore;
	}

	/**
	 * Creates a LangChain4j-generated implementation of {@link AircraftExtractor}.
	 *
	 * <p>
	 * The generated proxy parses free-text assistant responses and maps them to an
	 * {@link sk.mkrajcovic.travelx.model.Aircraft} instance using the configured
	 * chat model.
	 *
	 * <p>
	 * Registered as a singleton bean because {@code AiServices.create()} uses
	 * reflection to build a dynamic proxy - instantiating it once and reusing it
	 * across requests avoids unnecessary overhead per call. Extractors are
	 * stateless and thread-safe, so singleton scope is appropriate.
	 */
	@Bean
	AircraftExtractor aircraftExtractor(OpenAiChatModel structuredOpenAiChatModel) {
		return AiServices.create(AircraftExtractor.class, structuredOpenAiChatModel);
	}

	/**
	 * Creates a LangChain4j-generated implementation of {@link FlightExtractor}.
	 *
	 * <p>
	 * The generated proxy parses free-text assistant responses and maps them to a
	 * {@link sk.mkrajcovic.travelx.model.Flight} instance using the configured chat
	 * model.
	 *
	 * <p>
	 * Registered as a singleton bean because {@code AiServices.create()} uses
	 * reflection to build a dynamic proxy - instantiating it once and reusing it
	 * across requests avoids unnecessary overhead per call. Extractors are
	 * stateless and thread-safe, so singleton scope is appropriate.
	 */
	@Bean
	FlightExtractor flightExtractor(OpenAiChatModel structuredOpenAiChatModel) {
		return AiServices.create(FlightExtractor.class, structuredOpenAiChatModel);
	}

}
