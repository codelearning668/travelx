package sk.mkrajcovic.travelx.controller.dto;

import lombok.Getter;
import sk.mkrajcovic.travelx.model.Aircraft;

/**
 * Read-only response DTO exposing {@link Aircraft} data to API clients.
 *
 * <p>
 * Constructed from an {@link Aircraft} domain object in the controller layer,
 * decoupling the internal model from the serialized API contract.
 */
@Getter
public class AircraftResponse {

	private String manufacturer;
	private String model;
	private String maxDistance;

	public AircraftResponse(Aircraft aircraft) {
		manufacturer = aircraft.getManufacturer();
		model = aircraft.getModel();
		maxDistance = aircraft.getMaxDistance();
	}

}
