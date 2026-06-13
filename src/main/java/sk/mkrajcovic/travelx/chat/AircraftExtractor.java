package sk.mkrajcovic.travelx.chat;

import sk.mkrajcovic.travelx.model.Aircraft;

/**
 * LangChain4j AI service contract for extracting {@link Aircraft} data from
 * unstructured text.
 *
 * <p>
 * Implementations are generated at runtime by
 * {@link dev.langchain4j.service.AiServices}. The underlying model is prompted
 * to parse the supplied text and map it to an {@link Aircraft} instance.
 */
public interface AircraftExtractor {

	/**
	 * Parses the given text and maps its content to an {@link Aircraft} object.
	 *
	 * @param text free-form text containing aircraft-related information
	 * @return an {@link Aircraft} populated with data extracted from {@code text}
	 */
	Aircraft extractAircraftFrom(String text);

}
