package com.application.main.NotificationService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificationModel {

	private String message;
	private String title;
	private String senderid;
	private String userid;
	@Default
	private boolean read = false;
}
