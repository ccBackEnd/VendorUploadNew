package com.application.main.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user")
public class UserClass {
	@Id
	private String id;
	private boolean isAdmin = false;

	private String username;
	private String displayUsername;
	private String eic;
	private String type;
	private boolean FirstLogin;
	private String otp;
	private String KeycloakUserId;
	private boolean warehouse;
	private String organizationName;
	private String panNumber;
	private String adminOforganization;
	private String phoneNumber;
	private List<String> gstDocument;
	private List<String> panDocument;
	private List<String> incorporationDocument;
	private boolean claimed;
	private String email;
	private String address;
	private String password;
	private String newPassword;
	private String gstNo;

	public UserClass(String id, boolean isAdmin, String username, String displayUsername, String eic, String type,
			boolean firstLogin, String otp, String keycloakUserId, boolean warehouse, String organizationName,
			String panNumber, String adminOforganization, String phoneNumber, List<String> gstDocument,
			List<String> panDocument, List<String> incorporationDocument, String email, String password, String gstNo,
			String address, String newPassword) {
		super();
		this.id = id;
		this.isAdmin = isAdmin;
		this.username = username;
		this.displayUsername = displayUsername;
		this.eic = eic;
		this.type = type;
		FirstLogin = firstLogin;
		this.otp = otp;
		KeycloakUserId = keycloakUserId;
		this.warehouse = warehouse;
		this.organizationName = organizationName;
		this.panNumber = panNumber;
		this.adminOforganization = adminOforganization;
		this.phoneNumber = phoneNumber;
		this.gstDocument = gstDocument;
		this.panDocument = panDocument;
		this.incorporationDocument = incorporationDocument;
		this.email = email;
		this.password = password;
		this.gstNo = gstNo;
		this.address = address;
		this.newPassword = newPassword;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", isAdmin=" + isAdmin + ", username=" + username + ", displayUsername="
				+ displayUsername + ", eic=" + eic + ", type=" + type + ", FirstLogin=" + FirstLogin + ", otp=" + otp
				+ ", KeycloakUserId=" + KeycloakUserId + ", warehouse=" + warehouse + ", organizationName="
				+ organizationName + ", panNumber=" + panNumber + ", adminOforganization=" + adminOforganization
				+ ", phoneNumber=" + phoneNumber + ", gstDocument=" + gstDocument + ", panDocument=" + panDocument
				+ ", incorporationDocument=" + incorporationDocument + ", claimed=" + claimed + ", email=" + email
				+ ", password=" + password + ", gstNo=" + gstNo + ", address=" + address + ", newPassword="
				+ newPassword + "]";
	}

}
