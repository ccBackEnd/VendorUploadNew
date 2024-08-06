package com.application.main.model;

import java.util.Set;

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
@Document(collection = "VendorUser")
public class VendorUserModel {

	@Id
	private String id;

	private String username;
	private String firstName;
	private String lastName;
	@Default
	private Boolean enabled = false;
	private String password;
	private int accesslevel;
	private Set<String> userroles;
	private Set<String> eicdepartments;

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
