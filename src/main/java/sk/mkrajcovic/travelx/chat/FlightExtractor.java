package sk.mkrajcovic.travelx.chat;

import sk.mkrajcovic.travelx.model.Flight;

/**
 * LangChain4j AI service contract for extracting {@link Flight} data from
 * unstructured text.
 *
 * <p>
 * Implementations are generated at runtime by
 * {@link dev.langchain4j.service.AiServices}. The underlying model is prompted
 * to parse the supplied text and map it to a {@link Flight} instance.
 */
public interface FlightExtractor {

	/**
	 * Parses the given text and maps its content to a {@link Flight} object.
	 *
	 * @param text free-form text containing flight-related information
	 * @return a {@link Flight} populated with data extracted from {@code text}
	 */
	public Flight extractFlightFrom(String text);

}
