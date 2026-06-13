package sk.mkrajcovic.travelx.chat;

import dev.langchain4j.service.SystemMessage;

/**
 * Contract for interacting with the configured AI assistant.
 *
 * <p>
 * At application startup, LangChain4j generates a runtime proxy using
 * {@code AiServices.builder(Assistant.class)} and registers it as a Spring
 * bean.
 *
 * <p>
 * The generated implementation is responsible for:
 * <ul>
 * 	<li>Sending chat requests to the configured language model.</li>
 * 	<li>Managing conversation memory when a {@code ChatMemory} is configured.</li>
 * 	<li>Applying any future tools, retrieval mechanisms, or AI service features configured through LangChain4j.</li>
 * </ul>
 *
 * <p>
 * Using an interface decouples application code from a specific AI provider
 * implementation and allows the underlying model, memory strategy, or agent
 * capabilities to be replaced without changing business services.
 */
public interface Assistant {

	static final String UNKNOWN_CONTEXT_REPLY = "I don't know."; 

	/*
	 * This is an example of prompt engineering - the process of designing,
	 * refining and optimizing text or visual instructions to guide an AI
	 * model to produce highly accurate, relevant and useful outputs.
	 * Minimize hallucinations.
	 */
	static final String TRAVEL_ASSISTANT_PROMPT =
		"You are a travel assistant.\n\n" +
		"Rules:\n" +
		"- Limit responses to known facts.\n" +
		"- If information is unavailable or you cannot access it, YOU MUST SAY EXACTLY THIS: \"" + UNKNOWN_CONTEXT_REPLY + "\".";

	/**
	 * Sends a user message to the configured language model and returns the
	 * generated response.
	 *
	 * <p>
	 * The assistant operates according to the {@link SystemMessage} instructions
	 * declared on this method and may use configured chat memory to maintain
	 * conversational context across multiple interactions.
	 *
	 * <i><p>
	 * The system message acts as the highest-priority instruction in the OpenAI
	 * chat hierarchy. It defines the assistant's role, behavioral constraints, and
	 * response style for the entire conversation.
	 *
	 * <p>
	 * Analogy in coding-agent systems (e.g., Roo Code or similar agent frameworks):
	 * this corresponds to global "rules" or "system prompts" that govern the
	 * agent's behavior across all tasks. It is NOT equivalent to tools/skills or
	 * execution modes, but rather to persistent behavioral policy and instruction
	 * set.</i>
	 *
	 * @param userRequest user's question, request, or message
	 * @return generated assistant response
	 */
	@SystemMessage(TRAVEL_ASSISTANT_PROMPT)
	public String chat(String userRequest);

}
