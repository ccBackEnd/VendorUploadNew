package com.application.main.model;

public class DocDetails {
	
	private String docId;
	private String name;
	private String url;
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
	
	public DocDetails(String name,String documentid, String url) {
		super();
		this.name = name;
		this.docId = documentid;
		this.url = url;
	}
	
	
}

