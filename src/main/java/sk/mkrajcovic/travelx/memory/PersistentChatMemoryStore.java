package sk.mkrajcovic.travelx.memory;

import static org.mapdb.Serializer.STRING;

import java.util.List;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

/**
 * Persistent implementation of {@link ChatMemoryStore} that stores conversation
 * history in a file-based MapDB database.
 *
 * <p>
 * This store serializes {@link ChatMessage} objects to JSON for persistence,
 * and deserializes them when loading. Each conversation is identified by a
 * unique {@code memoryId}, allowing multiple independent conversations to be
 * stored simultaneously.
 *
 * <p>
 * Messages are committed to disk on each update to ensure durability across
 * application restarts. This implementation is suitable for small- to
 * medium-sized chat histories where a simple file-based persistent store is
 * sufficient.
 */
public class PersistentChatMemoryStore implements ChatMemoryStore {

	private final DB database;
	private final Map<String, String> chatHistory;

	/**
	 * Initializes the persistent chat memory store.
	 *
	 * <p>
	 * Creates (or opens if it already exists) a file-based MapDB database named
	 * {@code chat-memory.db}, with a hash map named {@code messages} that maps
	 * memory IDs to serialized JSON chat histories.
	 */
	public PersistentChatMemoryStore() {
		database = DBMaker.fileDB("chat-memory.db").transactionEnable().make();
		chatHistory = database.hashMap("messages", STRING, STRING).createOrOpen();
	}

	/**
	 * Retrieves the chat history associated with a given memory ID.
	 *
	 * @param memoryId the unique identifier for the conversation (usually a user ID)
	 * @return a list of {@link ChatMessage} objects representing the conversation
	 *         history, or {@code null} if no messages exist for this memory ID
	 */
	@Override
	public List<ChatMessage> getMessages(Object memoryId) {
		String json = chatHistory.get((String) memoryId);
		return ChatMessageDeserializer.messagesFromJson(json);
	}

	/**
	 * Updates the stored messages for a given memory ID.
	 *
	 * <p>
	 * Messages are serialized to JSON and committed immediately to the persistent
	 * MapDB store.
	 *
	 * @param memoryId the unique identifier for the conversation
	 * @param messages the complete list of {@link ChatMessage} objects to persist
	 */
	@Override
	public void updateMessages(Object memoryId, List<ChatMessage> messages) {
		String json = ChatMessageSerializer.messagesToJson(messages);
		chatHistory.put((String) memoryId, json);
		database.commit();
	}

	/**
	 * Retrieves the chat history associated with a given memory ID.
	 *
	 * @param memoryId the unique identifier for the conversation (usually a user ID)
	 * @return a list of {@link ChatMessage} objects representing the conversation
	 *         history, or {@code null} if no messages exist for this memory ID
	 */
	@Override
	public void deleteMessages(Object memoryId) {
		chatHistory.remove((String) memoryId);
		database.commit();
	}
}
