package sk.mkrajcovic.travelx.controller.dto;

import lombok.Getter;
import sk.mkrajcovic.travelx.model.Flight;

@Getter
public class FlightResponse {

	private String aircraft;
	private String dateOfDeparture;
	private String fromCity;
	private String toCity;
	private String departureAirport;
	private String arrivalAirport;
	private String flightNumber;
	private String departureTime;
	private String arrivalTime;
	private String status;

	public FlightResponse(Flight flight) {
		aircraft = flight.getAircraft();
		dateOfDeparture = flight.getDateOfDeparture();
		fromCity = flight.getFromCity();
		toCity = flight.getFromCity();
		departureAirport = flight.getDepartureAirport();
		arrivalAirport = flight.getArrivalAirport();
		flightNumber = flight.getFlightNumber();
		departureTime = flight.getDepartureTime();
		arrivalTime = flight.getArrivalTime();
		status = flight.getStatus();
	}

}
