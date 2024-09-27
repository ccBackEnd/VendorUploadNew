package com.application.main.model.NotificationModel;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "VP_Notification")
public class VendorPortalNotification {

	@Id
	private String id;
	
	private String recieverusername;
	private LocalDateTime generatedAt;
	private String subject;
	private String filename;
	private String senderusername;
	private String message;
	private String status;
	private String priority;
	
	
}
