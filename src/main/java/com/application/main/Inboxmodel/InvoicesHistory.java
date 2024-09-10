package com.application.main.Inboxmodel;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class InvoicesHistory {
	
	@Id
	private String id;
	private String fileurl;
	private String status;
	private String invoiceNo;
	private String remarks;
	private String recievedFrom;
	private String sentto;
	
	
	public InvoicesHistory(String fileurl, String invoiceNo, String remarks,
			String recievedFrom, String sentto) {
		super();
		this.fileurl = fileurl;
		this.invoiceNo = invoiceNo;
		this.remarks = remarks;
		this.recievedFrom = recievedFrom;
		this.sentto = sentto;
	}
	
	

}
