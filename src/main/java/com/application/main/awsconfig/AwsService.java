package com.application.main.awsconfig;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.application.main.Repositories.DocDetailsRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.URLCredentialModel.CipherEncDec;
import com.application.main.URLCredentialModel.DocDetails;
import com.application.main.model.Invoice;
import com.application.main.model.PoSummary;

@Service
public class AwsService {

//	@Value("${bucket_name}")
	private String bucketName;
	@Value("${minio_url}")
	private String minioUrl;
	@Autowired
	private AWSClientConfigService s3client;
	private String token;

	@Autowired
	InvoiceRepository invoicerepository;

	@Autowired
	PoSummaryRepository porepo;
	
	@Autowired
	DocDetailsRepository docDetailsRepository;
	
	public static final String ALGORITHM = "AES";
	public static final int KEY_SIZE = 256;

	public void createBucket(String token, String username) {
		this.bucketName = "vendorportalfiles" + username;
		this.token = token;
		try {
			System.out.println("CREATE BUCKET ERROR");
			AmazonS3 awsClient = s3client.awsClientConfiguration(token);
			if (!awsClient.doesBucketExistV2(bucketName))
				awsClient.createBucket(bucketName);
		} catch (AmazonS3Exception e) {
			System.out.println("CREATE BUCKET EXCEPTION");
			System.err.println(e.getErrorMessage());
		}
	}

	public DocDetails uploadFile(MultipartFile file, String Invoiceid, String username) throws IOException,Exception {

		System.out.println("token from AWS Service : " + token);
		String fileName = Invoiceid.concat("?#" + file.getOriginalFilename());
		AmazonS3 awsClient = s3client.awsClientConfiguration(token);
		if (file == null || file.isEmpty())
			throw new ResponseStatusException(HttpStatus.SC_METHOD_FAILURE, "Null or Empty file not Accepted", null);
		if (awsClient.doesObjectExist(bucketName, fileName)) {
			System.err.println("FILE WITH " + file.getOriginalFilename() + " Already Exists !");
		}		
		PutObjectResult res = awsClient.putObject(bucketName, fileName, file.getInputStream(), new ObjectMetadata());
		String invoiceFileUrl = bucketName + "123" + fileName ;
		
		SecretKey secretkey = generateSecretKey();
		invoiceFileUrl = new CipherEncDec().encrypt(invoiceFileUrl, secretkey);
		String EncodedSecretKey = Base64.getEncoder().encodeToString(secretkey.getEncoded());
		HashMap<String, Object> response = new HashMap<>();
		response.put("Response", res);
		response.put("fileName", file.getOriginalFilename());
		response.put("generatedURL", invoiceFileUrl);
		
		DocDetails newdoc = new DocDetails(fileName, Invoiceid, invoiceFileUrl, EncodedSecretKey);
		
//		awsClient.getObject(bucketName, fileName);
		System.err.println("---------- Upload Response Object START ------------");
		for (Map.Entry<String, Object> ele : response.entrySet()) {
			System.out.println(ele.getKey() + " : " + ele.getValue().toString());
		}
		System.err.println("---------- Upload Response Object END ------------");
		docDetailsRepository.save(newdoc);
		return newdoc;

	}

	public SecretKey generateSecretKey() throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		keyGenerator.init(KEY_SIZE);
		return keyGenerator.generateKey();
	}

	public Map<String, Object> uploadMongoFile(String eic, String roleName, String poNumber, String token,
			String alternateMobileNumber, String alternateEmail, Set<String> remarks, String invoiceAmount,
			String invoiceDate, String invoiceNumber, String username, String createdBy, String deliveryPlant,
			DocDetails invoicedetails, List<DocDetails> suppDocNameList, String status, String receievedBy)

			throws IOException {

		Invoice invoice = new Invoice();
		invoice.setPoNumber(poNumber);
		invoice.setStatus("Paid");
		invoice.setReceievedBy(eic);
		invoice.setCreatedBy(username);
		invoice.setInvoiceurl(invoicedetails.getUrl());

		if (roleName != null && roleName.length() > 0) {

			invoice.setRoleName(roleName);
		}
		System.out.println("---------------->" + roleName);
		// Update the "eic" field if the parameter is provided
		if (eic != null && !eic.isEmpty()) {
			invoice.setEic(eic);
		}
		if (deliveryPlant != null && !eic.isEmpty()) {
			invoice.setDeliveryPlant(deliveryPlant);
		}
		List<DocDetails> invoiceobjectaslist = new ArrayList<>();
		if (invoicedetails != null)
			invoiceobjectaslist.add(invoicedetails);
		invoice.setInvoiceFile(invoiceobjectaslist);
		invoice.setSupportingDocument(suppDocNameList);
		if (alternateMobileNumber != null && !alternateMobileNumber.isEmpty()) {
			invoice.setAlternateMobileNumber(alternateMobileNumber);
		}
		invoice.setAlternateMobileNumber(alternateMobileNumber);
		invoice.setAlternateEmail(alternateEmail);
		invoice.setRemarks(remarks);
		invoice.setUsername(username);
		invoice.setInvoiceAmount(invoiceAmount);
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
		if (invoiceDate != null) {
			invoice.setInvoiceDate(LocalDate.parse(invoiceDate, formatter));
			System.err.println(invoice.getInvoiceDate());
			// 9289114318
		}

		System.out.println("---------------->" + roleName);
		invoice.setInvoiceNumber(invoiceNumber);
		invoice.setCreatedBy(createdBy);
		invoice.setReceievedBy(receievedBy);
//		invoice.setCurrentDateTime(LocalDateTime.now());
//        if(isinvoice) invoiceur=
		// invoice.setInvoice(isinvoice);
		invoice = invoicerepository.save(invoice);
		System.err.println("-------------Object Saved -------------------");

		Optional<PoSummary> po = porepo.findByPoNumber(poNumber);

		if (po.isPresent()) {

			if (po.get().getInvoiceobject() == null || po.get().getInvoiceobject().isEmpty()) {
				List<Invoice> li = new ArrayList<>();
				li.add(invoice);
				po.get().setNoOfInvoices(po.get().getNoOfInvoices() + 1);
				po.get().setInvoiceobject(li);
			} else if (!po.get().getInvoiceobject().contains(invoice)) {
				po.get().getInvoiceobject().add(invoice);
				po.get().setNoOfInvoices(po.get().getNoOfInvoices() + 1);
				System.out.println(po.get().getNoOfInvoices());
			}

		} else
			return Map.of("Error Found", HttpStatus.SC_SERVICE_UNAVAILABLE);
//		else return new HashMap<>().put("Error Found", HttpStatus.SC_SERVICE_UNAVAILABLE);

//		
		porepo.save(po.get());

		System.out.println("Before saving invoice to database");
		System.out.println("S3ServiceSaveRepo" + invoice.toString());// saving
		System.out.println("After saving invoice to database");
		System.out.println("Invoice with details :-> \n" + invoice.toString() + " is saved succesfully");
		Map<String, Object> responseData = new HashMap<>();
		System.err.println("---------------------------------");

		responseData.put("alternateNumber", alternateMobileNumber);
		responseData.put("alternativeEmail", alternateEmail);
		responseData.put("Eic", eic);
		responseData.put("roleName", roleName);
//		
		Set<String> remarkslist = new HashSet<>();
		remarkslist.addAll(remarks);
		responseData.put("Remarks", remarkslist);
		responseData.put("InvoiceAmount", invoiceAmount);
		responseData.put("Username", username);
		responseData.put("createdBy", createdBy);
		responseData.put("receievedBy", receievedBy);
		responseData.put("deliveryPlant", deliveryPlant);
		responseData.put("InvoiceNumber", invoiceNumber);
		return responseData;
	}

	

//	public ResponseEntity<?> uploadCompliance(String token, MultipartFile file) {
//
//		try {
//			System.out.println(token);
//			AmazonS3 awsClient = s3client.awsClientConfiguration(token);
//			System.out.println("----------------------------");
//			
//			if (file == null || file.isEmpty())
//				throw new ResponseStatusException(HttpStatus.SC_METHOD_FAILURE, "Null or Empty file not Accepted", null);
//			ObjectMetadata obm = new ObjectMetadata();
//
//			String fileName = Invoiceid.concat("?#" + file.getOriginalFilename());
//			bucketName = bucketName.concat(username);
//			
//			
//			
//			String fileName = file.getOriginalFilename();
//			ObjectMetadata objm = new ObjectMetadata();
//			awsClient.putObject("compliance", fileName, file.getInputStream(), objm);
//			// String url = "minioUrl/compliance/" + fileName;
//			String url = minioUrl + "/" + bucketName + "/" + fileName;
//			return ResponseEntity.ok(url);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ResponseEntity.internalServerError().body("not uploaded");
//		}
//	}

}
