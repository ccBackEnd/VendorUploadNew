package com.application.main.Repositories;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.application.main.model.Invoice;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
	//Optional<Invoice> findBypoNumber(String poNumber);
	

	Page<Invoice> findAll(Pageable pageable);
	Invoice findBypoNumber(String poNumber);
	
	Optional<Invoice>findById(String id);

	List<Invoice> findByUsername(String username);


	List<Invoice> findByUsernameAndClaimedIsTrue(String username);

	//List<Invoice> findByClaimedAndUsername(Boolean claimed, String username);

	List<Invoice> findByClaimed(Boolean claimed);

	List<Invoice> findByType(String string);

//	List<Invoice> findByEicAndInvoice(String eic, boolean b);

	List<Invoice> findByClaimedAndUsername(Boolean claimed, String username);

//	List<Invoice> findByUsernameAndInvoice(String username, boolean b);

	List<Invoice> findByClaimedAndId(Boolean claimed, String id);

	List<Invoice> findByEicAndClaimed(String eic, boolean b);

	List<Invoice> findAllByTypeAndEic(String type, String eic);

	List<Invoice> findByUsernameAndClaimed(String username, boolean b);

	List<Invoice> findByClaimedAndEic(Boolean claimed, String eic);

	List<Invoice> findByEic(String eic);

	List<Invoice> findAllByTypeAndEicAndClaimed(String type, String eic, boolean b);

	List<Invoice> findByClaimedAndEicAndClaimedBy(Boolean claimed, String eic, String username);



	List<Invoice> findByTypeAndClaimed(String string, boolean b);

	List<Invoice> findByClaimedTrue();

	List<Invoice> findByClaimedFalse();

	long countByTypeAndClaimed(String string, boolean b);

	long countByTypeAndEicAndClaimed(String type, String eic, boolean b);

	Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

	List<Invoice> findByUsernameAndPoNumber(String username, String poNumber);

	List<Invoice> findByUsernameAndInvoiceNumber(String username, String invoiceNumber);

	List<Invoice> findBypoNumberStartingWith(String prefix);

	//List<Invoice> findByDeliveryPlantStartingWith(String deliveryPlant);

	//List<Invoice> findBypoNumberContains(String poNumber);

	List<Invoice> findBypoNumberContaining(String poNumber);

	List<Invoice> findByReceiver(String receiver);

	List<Invoice> findAllByTypeAndEicAndClaimedAndReceiver(String type, String eic, boolean b, String receiver);

	List<Invoice> findByTypeAndClaimedAndReceiver(String string, boolean b, String receiver);

	List<Invoice> findByTypeAndClaimedOrReceiver(String string, boolean b, String eic);

	//List<Invoice> findAllByTypeAndEicAndClaimedOrReceiver(String type, String eic, boolean b, String eic2);

	//List<Invoice> findByTypeAndClaimedOrReceiverAndReceiver(String string, boolean b, String receiver);

	//List<Invoice> findAllByTypeAndEicAndClaimedOrReceiverAndReceiver(String type, String eic, boolean b,
	//		String receiver);

	List<Invoice> findByClaimedAndClaimedByOrReceiver(Boolean claimed, String claimedBy, String receiver);

	Invoice findByPoNumber(String poNumber);

	
	List<Invoice> findByPoNumberContaining(String poNumber);

	List<Invoice> findAllByTypeAndEicAndClaimedOrReceievedBy(String type, String eic, boolean b, String receievedBy);

	List<Invoice> findAllByTypeAndEicAndClaimedOrReceievedByAndReceievedBy(String type, String eic, boolean b,
			String receievedBy);

	List<Invoice> findByTypeAndClaimedOrReceievedByAndReceievedBy(String string, boolean b, String receievedBy);

	List<Invoice> findAllByTypeAndEicAndClaimedAndReceievedBy(String type, String eic, boolean b, String receievedBy);

	List<Invoice> findAllByTypeAndEicAndClaimedOrReceiver(String type, String eic, boolean b, String eic2);

	List<Invoice> findByClaimedAndClaimedBy(Boolean claimed, String claimedBy);

	List<Invoice> findByRoleName(String roleName);

	List<Invoice> findByRoleNameAndTypeAndClaimed(String roleName, String string, boolean b);

	List<Invoice> findByTypeAndClaimed(String roleName, String string, boolean b);

	long countByUsernameAndClaimed(String username, boolean b);

	long countByUsername(String username);

	List<Invoice> findByUsernameAndStatus(String username, String string);

	long countByUsernameAndStatus(String username, String string);
	// In your InvoiceRepository interface

	Collection<? extends Invoice> DeliveryTimelinesContaining(String deliveryTimelines);

	Collection<? extends Invoice> findByInvoiceNumberContaining(String invoiceNumber);

	Collection<? extends Invoice> findByStatusContaining(String status);

	Collection<? extends Invoice> findByInvoiceAmount(Double invoiceAmount);

	Collection<? extends Invoice> findByMobileNumberContaining(String mobileNumber);

	Collection<? extends Invoice> findByDeliveryPlantContaining(String deliveryPlant);

	Collection<? extends Invoice> findByUsernameContaining(String username);

	Collection<? extends Invoice> findByRemarksContaining(String remarks);

	Collection<? extends Invoice> findByPaymentTypeContaining(String paymentType);

	Collection<? extends Invoice> findByReceiverContaining(String receiver);

	Collection<? extends Invoice> findByClaimedByContaining(String claimedBy);

	Collection<? extends Invoice> findByMsmeCategoryContaining(String msmeCategory);

	Collection<? extends Invoice> findByTypeContaining(String type);

	

	

	
	

	Collection<? extends Invoice> findByUsernameAndPoNumberContainingOrDeliveryTimelinesContainingOrInvoiceNumberContainingOrStatusContainingOrMobileNumberContainingOrDeliveryPlantContainingOrUsernameContainingOrRemarksContainingOrPaymentTypeContainingOrReceiverContainingOrClaimedByContainingOrTypeContainingOrMsmeCategoryContaining(
			String username, String searchItems, String searchItems2, String searchItems3, String searchItems4,
			String searchItems5, String searchItems6, String searchItems7, String searchItems8, String searchItems9,
			String searchItems10, String searchItems11, String searchItems12, String searchItems13,
			String searchItems14);

	Collection<? extends Invoice> findByUsernameAndPoNumberContainingOrUsernameAndDeliveryTimelinesContainingOrUsernameAndInvoiceNumberContainingOrUsernameAndStatusContainingOrUsernameAndMobileNumberContainingOrUsernameAndDeliveryPlantContainingOrUsernameAndUsernameContainingOrUsernameAndRemarksContainingOrUsernameAndPaymentTypeContainingOrUsernameAndReceiverContainingOrUsernameAndClaimedByContainingOrUsernameAndTypeContainingOrUsernameAndMsmeCategoryContaining(
			String username, String searchItems, String searchItems2, String searchItems3, String searchItems4,
			String searchItems5, String searchItems6, String searchItems7, String searchItems8, String searchItems9,
			String searchItems10, String searchItems11, String searchItems12, String searchItems13);




}
