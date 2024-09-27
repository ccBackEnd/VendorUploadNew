package com.application.main.model.PaymentDetailsModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class TaxDetails {
	
	private String taxname;
	private double taxpercentage;
	private double taxamount;

}
