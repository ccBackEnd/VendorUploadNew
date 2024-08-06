package com.application.main.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "VendorUser")
public class UserDTO {
	
	@Id
	private String id;
	
	private String  username;
	private String  firstName;
	private String  lastName;
	private Boolean enabled;
	private String  accesslevel;
	private String  password;
	private String  assignedrole;
	
	
	
	public UserDTO() {
		super();
	}

	public String getAccesslevel() {
		return accesslevel;
	}
	
	public void setAccesslevel(String access_level) {
		this.accesslevel = access_level;
	}
	public UserDTO( String username, String firstName, String lastName, Boolean enabled, String password,
			String assignedrole,String access_level) {
		super();
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.enabled = enabled;
		this.password = password;
		this.assignedrole = assignedrole;
		this.accesslevel = access_level;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getAssignedrole() {
		return assignedrole;
	}
	public void setAssignedrole(String assignedrole) {
		this.assignedrole = assignedrole;
	}
	@Override
	public String toString() {
		return "UserDTO [username=" + username + ", firstName=" + firstName + ", lastName=" + lastName + ", enabled="
				+ enabled + ", password=" + password + "]";
	}
	

	
	
}
