package com.application.main.NotificationService;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface VPNotificationRepository extends MongoRepository<VendorPortalNotification, String>{
	
	List<VendorPortalNotification> findAllByRecieverusername(String recieverusername);
	List<VendorPortalNotification> findAllByRecieverusernameAndGeneratedAtOrderByDesc(String recieverusername);

}
