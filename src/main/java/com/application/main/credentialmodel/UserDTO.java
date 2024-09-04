package com.application.main.credentialmodel;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Userdetails")
public class UserDTO {
	
	@Id
	private String id;

	private String username;
	private String firstName;
	private String lastName;
	private String password;
	private Set<String> userroles;
	private Set<String> eicdepartments;
	public UserDTO(String username, String firstName, String lastName, String password, Set<String> userroles,
			Set<String> eicdepartments) {
		super();
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.userroles = userroles;
		this.eicdepartments = eicdepartments;
	}
	

	
	
}
