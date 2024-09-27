package com.application.main.model.UserModel;

import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "VendorClientModel")
public class VendorClientModel {

	@Id
	private String id;
	@UniqueElements
	private String clientVendorId;
	private String clientVendorEmail;
	private String password;
//	private int accesslevel;
	@Default
	private Boolean enabled = false;
	
	private String companyName;
	@UniqueElements
	private String panNumber;
	@UniqueElements
	private String gstNumber;
	@UniqueElements
	private String tanno;
	@UniqueElements
	private String msmeno;
	private String industrytype;
	private AddressVendorUser registrationAddress;
	private AddressVendorUser warehouseAddress;
	private AddressVendorUser hoAddress;
	
	private Boolean isClient;
	private Boolean isVendor;
	

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	 public static class ContactPerson {
			private String contactpersonname;
			private String contactpersonmobilenumber;
			private String contactpersonemail;
			private String contactpersondesignation;
			
		}
		private ContactPerson contactpersondetails;
	
	public VendorClientModel(String organizationName, @UniqueElements String panNumber, @UniqueElements String gstNumber,
			@UniqueElements String tanno, @UniqueElements String msmeno, String industrytype,AddressVendorUser registrationAddress, Boolean isClient,
			ContactPerson contactpersondetails) {
		super();
		this.companyName = organizationName;
		this.panNumber = panNumber;
		this.gstNumber = gstNumber;
		this.tanno = tanno;
		this.msmeno = msmeno;
		this.registrationAddress = registrationAddress;
		this.industrytype = industrytype;
		this.isClient = isClient;
		this.contactpersondetails = contactpersondetails;
	}
	
	
	
	
}