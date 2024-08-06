package com.application.main.exception;
import java.util.Date;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExceptionResponseObject {
	
	private String error;
	@Builder.Default
	private String serviceName="User Service";
	@JsonFormat(pattern = "dd MMM yyyy HH:mm:ss")
	private Date time;
	private HttpStatus httpStatus;
	private int statusCode;
}
