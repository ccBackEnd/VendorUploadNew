package com.application.main.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "AWSDocumentdetails")
public class DocDetails {
	
	@Id
	private String id;
	private String docId;
	private String name;
	private String url;
	private String Base64Encodedsecretkey;
	
	
	public String getBase64Encodedsecretkey() {
		return Base64Encodedsecretkey;
	}
	public void setSecretkey(String secretkey) {
		this.Base64Encodedsecretkey = secretkey;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public DocDetails() {
		super();
	}	
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	@Override
	public String toString() {
		return "DocDetails [name=" + name + ", url=" + url + "]";
	}
	
	public DocDetails(String name,String documentid, String url,String secretkey) {
		super();
		this.name = name;
		this.docId = documentid;
		this.url = url;
		this.Base64Encodedsecretkey = secretkey;
	}
	
	
}

