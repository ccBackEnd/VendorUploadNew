package com.application.main.exception;


import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	private HttpStatus httpStatus;
	private String message;

	public CustomException(String message,HttpStatus httpStatus) {
		this.message=message;
		this.httpStatus=httpStatus;
		// TODO Auto-generated constructor stub
	}

	public CustomException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
}
