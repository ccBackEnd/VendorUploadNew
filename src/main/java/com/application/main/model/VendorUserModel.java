package com.application.main.model;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.application.main.Repositories.VendorUserRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "User_details")
public class VendorUserModel {

	@Autowired
	VendorUserRepository vendorrepo;
	
	@Id
	private String id;

	private String username;
	private String firstName;
	private String lastName;
	private String displayUsername;
	private String vendoremail;
	private String password;
	private int accesslevel;
	@Default
	private Boolean enabled = false;
	private Set<String> userroles;
	private Set<String> eicdepartments;
	
	private String eic;
	private String otp;
	
	private String organizationName;
	private String adminOforganization;
	private String phoneNumber;
	private String panNumber;
	private List<String> gstDocument;
	private List<String> panDocument;
	private List<String> incorporationDocument;
	private boolean claimed;
	private String address;
	private String gstNo;

	public VendorUserModel(String username, String firstName, String lastName ,String password,String vendoremail,Set<String> Userroles, Set<String> Eicdepartments) {
		super();
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
//		this.enabled = enabled;
		this.password = password;
		this.vendoremail=vendoremail;
//		this.accesslevel = accesslevel;
		this.userroles = Userroles;
		this.eicdepartments=Eicdepartments;
		}
	private @UniqueElements String createUniqueRandomid(int count) {
		// TODO Auto-generated method stub
		String id = RandomStringUtils.randomAlphanumeric(20);
		if(vendorrepo.existsByVendoruserid(id)) {
			createUniqueRandomid(count++);
		}
		System.out.println(count);
		return id;
	}

	public VendorUserModel(String username, String firstName, String lastName ,String password,Set<String> Userroles, Set<String> Eicdepartments) {
		super();
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
//		this.enabled = enabled;
		this.password = password;
//		this.accesslevel = accesslevel;
		this.userroles = Userroles;
		this.eicdepartments=Eicdepartments;
		}

}
