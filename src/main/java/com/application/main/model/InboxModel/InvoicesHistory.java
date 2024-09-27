package com.application.main.model.InboxModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class InvoicesHistory {
	
	private String fileName;
	private String fileurl;
	private String status;
	private String invoiceNo;
	private String remarks;
	
	
	public InvoicesHistory(String fileName , String fileurl, String invoiceNo, String remarks) {
		super();
		this.fileName = fileName;
		this.fileurl = fileurl;
		this.invoiceNo = invoiceNo;
		this.remarks = remarks;
	}
	
	

}
