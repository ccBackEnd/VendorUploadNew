package com.application.main.model;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
	private LocalDate forwardRevertDate;
	private String remarks;

}
