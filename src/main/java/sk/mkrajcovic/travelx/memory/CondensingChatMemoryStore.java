package sk.mkrajcovic.travelx.memory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CondensingChatMemoryStore implements ChatMemoryStore {

	private static final Logger LOG = LoggerFactory.getLogger(CondensingChatMemoryStore.class);

	private final ChatMemoryStore delegate;
	private final OpenAiChatModel summarizerModel;
	private final int threshold;

	@Override
	public List<ChatMessage> getMessages(Object memoryId) {
		LOG.info("GET MESSAGES {}", memoryId);
		return delegate.getMessages(memoryId);
	}

	// tu idem implementovat zmrstenie kontextu
	// treba si dat pozor, kedy framework vyskladava a posiela request a kedy vola tieto metody
	// chcem si nechat len poslednu user message

	// TODO: zvysit maximalny pocet sprav v historii na 20 a kondenzovat pri 18/19-tej
	//       (debuggnut kedy, nech sa nic nestrati vhladom na vyssie uvedny call stack + doplnanie system message frameworkom)

	// TODO: dorobit merge sumarov + nerobit sumar z uz urobeneho sumaru

	// NOTE: uchovavanie/posuvanie systemovej spravy, aby bola vzdy v historii uz robi framework
	//       ten ak ju odstrani, tak ju recreatne nanovo a doposle.. oplati sa to kotvenie spravit tu?
	@Override
	public void updateMessages(Object memoryId, List<ChatMessage> messages) {
		LOG.info("UPDATE MESSAGES {} size={}", memoryId, messages.size());
		if (messages == null || messages.size() < threshold) {
			delegate.updateMessages(memoryId, messages);
			return;
		}

		LOG.info("Condensing context...");
		int keepRecent = 1;
		int splitIndex = Math.max(0, messages.size() - keepRecent);

		List<ChatMessage> oldMessages = messages.subList(0, splitIndex);
		List<ChatMessage> recentMessages = messages.subList(splitIndex, messages.size());

		StringBuilder input = new StringBuilder();
		input.append("Summarize the conversation. Keep key facts, decisions, preferences.\n\n");

		for (ChatMessage msg : oldMessages) {
			input.append(msg.toString()).append("\n");
		}

		LOG.debug("Calling model to summarize context...");
		String summary = summarizerModel.chat(UserMessage.from(input.toString())).aiMessage().text();

		List<ChatMessage> condensed = new ArrayList<>();

		// FIXME: user message je vzdy frameworkom prepisana podla toho co je definovane v @SystemMessage na Assistant rozhrani
		//        sumarizovana konverzacia sa tym padom neposiela a prepisuje tou z konfiguracie
//		condensed.add(SystemMessage.from("Conversation summary so far:\n" + summary));
		// FIXME: nesimulovat AI odpoved ale pouzit user message, nech to vyzera, ze ja mu hovorim ten sumar
		//        ak by som zmenil model, tak to nemusi dobre pochopit..
		ChatMessage message = new AiMessage("Conversation summary so far:\n" + summary);
		condensed.add(message);

		condensed.addAll(recentMessages);

		delegate.updateMessages(memoryId, condensed);
		LOG.info("Context condensed with summary: {}", summary);
	}

	@Override
	public void deleteMessages(Object memoryId) {
		delegate.deleteMessages(memoryId);
	}
}