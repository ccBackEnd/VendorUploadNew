package com.application.main.model.PaymentDetailsModel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.application.main.model.UserModel.AddressVendorUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@Document(collection = "PoRecieverDetails")
public class RecieverDetails {
	@Id
	private String id;

	private String recievername;
    private String accountnumber;
    private String ifsccode;
    private String recieverEmail;
    private String recieverPhoneNumber;
    private String recieverpaymentType;
	private AddressVendorUser recieveraddress;
}
