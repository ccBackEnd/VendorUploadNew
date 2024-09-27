package com.application.main.model.UserModel;

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
@Document(collection = "User_details")
public class UserDetails {

	@Id
	private String id;

	private String username;
	private String firstName;
	private String lastName;
	private String userCode;
	private String email;
	private String password;
	@Default
	private boolean isEic = true;
	private Set<String> userRoles;
	private Set<String> eicDepartments;

	public UserDetails(String username, String firstName, String lastName, String password, Set<String> userroles,
			Set<String> eicdepartments) {
		super();
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.userRoles = userroles;
		this.eicDepartments = eicdepartments;
	}

}
