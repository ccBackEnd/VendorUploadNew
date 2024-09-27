package com.application.main.Repositories.NotificationRepository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.model.NotificationModel.VendorPortalNotification;

public interface NotificationRepository extends MongoRepository<VendorPortalNotification, String> {

	List<VendorPortalNotification> findAllByRecieverusername(String recieverusername);

	List<VendorPortalNotification> findAllByRecieverusernameOrderByGeneratedAtDesc(String recieverusername);

}
