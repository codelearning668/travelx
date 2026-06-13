package sk.mkrajcovic.travelx.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
public class Aircraft {

	private String manufacturer;
	private String model;
	private String maxDistance;

}
