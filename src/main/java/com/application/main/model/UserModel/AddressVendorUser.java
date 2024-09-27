package com.application.main.model.UserModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressVendorUser {

	private String address;
	private String city;
	private String zipCode;
	private String state;
	private String country;
	private String landmark;

//	private String statecode;
}
