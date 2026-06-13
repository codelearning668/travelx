package sk.mkrajcovic.travelx.controller;

import static lombok.AccessLevel.PACKAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sk.mkrajcovic.travelx.controller.dto.AircraftResponse;
import sk.mkrajcovic.travelx.controller.dto.ChatRequest;
import sk.mkrajcovic.travelx.controller.dto.FlightResponse;
import sk.mkrajcovic.travelx.service.ChatService;

/**
 * REST controller exposing a simple chat endpoint backed by an
 * OpenAI-compatible LLM API.
 *
 * <p>
 * This controller represents the entry point of the chat flow in the
 * application.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(access = PACKAGE)
class ChatController {

	private final ChatService chatService;

	@PostMapping(path = "/chat", produces = APPLICATION_JSON_VALUE)
	String chat(@RequestBody @Valid ChatRequest chatRequest) {
		return chatService.chat(chatRequest.getUserMessage());
	}

	@PostMapping(path = "/aircraft-chat", produces = APPLICATION_JSON_VALUE)
	AircraftResponse aircraftChat(@RequestBody @Valid ChatRequest chatRequest) {
		var aircraft = chatService.aircraftChat(chatRequest.getUserMessage());
		return new AircraftResponse(aircraft);
	}

	@PostMapping(path = "/flight-chat", produces = APPLICATION_JSON_VALUE)
	FlightResponse flightChat(@RequestBody @Valid ChatRequest chatRequest) {
		var flight = chatService.flightChat(chatRequest.getUserMessage());
		return new FlightResponse(flight);
	}
}
