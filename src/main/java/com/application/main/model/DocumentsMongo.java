package com.application.main.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "MongoDocumentsVendorPortal")
public class DocumentsMongo {
	@Id
	private String id;
	private String url;
	private String username;
	private String filename;
	public DocumentsMongo() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getusername() {
		return username;
	}
	public void setusername(String username) {
		this.username = username;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	@Override
	public String toString() {
		return "DocumentsMongo [id=" + id + ", url=" + url + ", username=" + username + ", filename=" + filename + "]";
	}
	

}
