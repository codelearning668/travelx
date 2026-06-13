package sk.mkrajcovic.travelx.service;

import org.springframework.stereotype.Service;

import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import sk.mkrajcovic.travelx.chat.AircraftExtractor;
import sk.mkrajcovic.travelx.chat.Assistant;
import sk.mkrajcovic.travelx.chat.FlightExtractor;
import sk.mkrajcovic.travelx.model.Aircraft;
import sk.mkrajcovic.travelx.model.Flight;

/**
 * Service layer for AI-powered chat interactions.
 *
 * <p>
 * Delegates conversational queries to the {@link Assistant} and, where
 * structured output is required, uses {@link AiServices}-backed extractors to
 * parse the assistant's free-text response into a typed domain object.
 */
@Service
@RequiredArgsConstructor
public class ChatService {

	private final Assistant generalChatAssistant;
	private final Assistant companyKnowledgeAssistant;
	private final AircraftExtractor aircraftExtractor;
	private final FlightExtractor flightExtractor;

	/**
	 * Sends a message to the AI assistant and returns its raw text reply.
	 *
	 * @param userMessage the user's input message
	 * @return the assistant's response as a plain string
	 */
	public String chat(String userMessage) {
		return generalChatAssistant.chat(userMessage);
	}

	/**
	 * Sends a message to the AI assistant and extracts an {@link Aircraft} from the reply.
	 *
	 * @param userMessage the user's input message describing or requesting aircraft information
	 * @return an {@link Aircraft} populated from the assistant's response, or an
	 *         empty {@link Aircraft} if the assistant had no relevant context
	 */
	public Aircraft aircraftChat(String userMessage) {
		String response = companyKnowledgeAssistant.chat(userMessage);
		if (isOutOfContext(response)) {
			return new Aircraft();
		}
		return aircraftExtractor.extractAircraftFrom(response);
	}

	/**
	 * Sends a message to the AI assistant and extracts a {@link Flight} from the reply.
	 *
	 * @param userMessage the user's input message describing or requesting flight information
	 * @return a {@link Flight} populated from the assistant's response, or an empty
	 *         {@link Flight} if the assistant had no relevant context
	 */
	public Flight flightChat(String userMessage) {
		String response = companyKnowledgeAssistant.chat(userMessage);
		if (isOutOfContext(response)) {
			return new Flight();
		}
		return flightExtractor.extractFlightFrom(response);
	}

	private boolean isOutOfContext(String response) {
		return response.contains(Assistant.UNKNOWN_CONTEXT_REPLY);
	}
}
