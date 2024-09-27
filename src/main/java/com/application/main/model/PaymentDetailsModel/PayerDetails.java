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
@Document(collection = "PayerDetails")
public class PayerDetails {
	@Id
	private String id;

	private String payerName;
	private String accountnumber;
	private String accounttype;
	private String ifsccode;
	private String payerEmail;
	private String payerPhoneNumber;
	private String payerpaymentType;
	private AddressVendorUser payeraddress;

}
