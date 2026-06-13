package sk.mkrajcovic.travelx.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Flight {

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

}
