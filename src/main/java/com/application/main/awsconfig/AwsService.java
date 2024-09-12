package com.application.main.awsconfig;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.application.main.Repositories.DocDetailsRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.LoginUserRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.credentialmodel.CipherEncDec;
import com.application.main.credentialmodel.DocDetails;
import com.application.main.model.Invoice;
import com.application.main.model.PoSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AwsService {

//	@Value("${bucket_name}")
	private String bucketName;
	@Value("${minio_url}")
	private String minioUrl;
	@Autowired
	private AWSClientConfigService s3client;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	@Autowired
	InvoiceRepository invoicerepository;
	
	@Autowired
	LoginUserRepository loginrepo;

	@Autowired
	PoSummaryRepository porepo;

	@Autowired
	DocDetailsRepository docDetailsRepository;
	
	KafkaTemplate<String, Invoice> ktemplate;

	public static final String ALGORITHM = "AES";
	public static final int KEY_SIZE = 256;

	public void createBucket(String token, String folderappend) {
		this.bucketName = "vendorportalfiles" + folderappend;
		try {
			AmazonS3 awsClient = s3client.awsClientConfiguration(token);
			if (!awsClient.doesBucketExistV2(bucketName))
				awsClient.createBucket(bucketName);
			System.out.println("----------BUCKET CREATED SUCCESSFULLY---------");
			System.out.println("bucket name : " + bucketName);
		} catch (AmazonS3Exception e) {
			System.out.println("CREATE BUCKET EXCEPTION");
			System.err.println(e.getErrorMessage());
		}
	}

	public String getUserNameFromToken(String token) {

		String tokenBody = token.split("\\.")[1];
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String payload = new String(decoder.decode(tokenBody));
		System.out.println("Getting Username from payload");
		return getFieldFromJson(payload, "preferred_username");
	}

	private String getFieldFromJson(String json, String fieldName) {
		String fieldValue = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(json);
			System.out.println(jsonNode.toPrettyString());
			fieldValue = jsonNode.get(fieldName).asText();
			System.out.println("------------------------------------");
			System.out.println(fieldName + " : " + fieldValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fieldValue;
	}

	public DocDetails uploadFile(String token, MultipartFile file, String id, String username)
			throws IOException, Exception {
		System.out.println("uploading file attached");
		System.out.println("Getting token from AWS Service : ");
		System.out.println("token is : " + token);
		String fileName = id.concat("?#" + file.getOriginalFilename());
		AmazonS3 awsClient = s3client.awsClientConfiguration(token);

		if (file == null || file.isEmpty()) {
			System.out.println("File is null");
			return new DocDetails();
		}
		if (awsClient.doesObjectExist(bucketName, fileName)) {
			System.err.println("FILE WITH " + file.getOriginalFilename() + " Already Exists !");
			System.err.println("-------------Replacing file without permission-------------");
			awsClient.deleteObject(bucketName, fileName);
		}

		System.out.println("UPLOADING FILES ................");

		PutObjectResult res = awsClient.putObject(bucketName, fileName, file.getInputStream(), new ObjectMetadata());
		String invoiceFileUrl = bucketName + "XCIDHK2788k99BBSEEL99" + fileName;

		SecretKey secretkey = generateSecretKey();
		invoiceFileUrl = new CipherEncDec().encrypt(invoiceFileUrl, secretkey);
		String EncodedSecretKey = Base64.getEncoder().encodeToString(secretkey.getEncoded());
		HashMap<String, Object> response = new HashMap<>();
		response.put("Response", res);
		response.put("fileName", file.getOriginalFilename());
		response.put("generatedURL", invoiceFileUrl);

		DocDetails newdoc = new DocDetails(fileName, id, invoiceFileUrl, EncodedSecretKey);

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

	public Map<String, Object> uploadMongoFile(String username, String msmecategory, String poNumber,
			String paymentType, String deliveryPlant, String invoiceDate, String invoiceNumber, String invoiceAmount,
			String mobileNumber, String email, String alternateMobileNumber, String alternateEmail, String remarks,
			String ses, boolean isagainstLC, boolean isGst, boolean isTredExchangePayment, String factoryunitnumber,
			boolean isMDCCPayment, String mdccnumber, String sellerGst, String buyerGst, String bankaccountno,
			DocDetails invoicedetails, List<DocDetails> suppDocNameList)

			throws IOException {

		Invoice invoice = new Invoice();
		invoice.setPoNumber(poNumber);
		invoice.setStatus("Sent");
		invoice.setInvoiceurl(invoicedetails.getUrl());
		invoice.setEic(porepo.findByPoNumber(poNumber).get().getEic());
		invoice.setDeliveryPlant(deliveryPlant);

		List<DocDetails> invoiceobjectaslist = new ArrayList<>();
		if (invoicedetails != null)
			invoiceobjectaslist.add(invoicedetails);
		invoice.setInvoiceFile(invoiceobjectaslist);
		invoice.setSupportingDocument(suppDocNameList);
		if (alternateMobileNumber != null && !alternateMobileNumber.isEmpty())
			invoice.setAlternateMobileNumber(alternateMobileNumber);

		if (alternateEmail != null && !alternateEmail.isEmpty())
			invoice.setAlternateEmail(alternateEmail);

		Set<String> remarksset = new HashSet<String>();
		remarksset.add(remarks);
		invoice.setRemarks(remarksset);
		invoice.setUsername(List.of(username));
		invoice.setInvoiceAmount(invoiceAmount);
		System.out.println("-------------------- before invoice date--------------");
		if (invoiceDate != null) {
			invoice.setInvoiceDate(LocalDate.parse(invoiceDate, formatter));
			System.err.println(invoice.getInvoiceDate());
		}

		invoice.setInvoiceNumber(invoiceNumber);
		invoice.setCurrentDateTime(LocalDateTime.now());
		System.err.println("-------------Invoice with : " + invoiceNumber + " Saved Successfully-------------------");
		List<String> usernamelist = invoice.getUsername();
		usernamelist.add(loginrepo.findByUserCode(invoice.getEic()).get().getUsername());
		
		Optional<PoSummary> po = porepo.findByPoNumber(poNumber);

		if (po.isPresent()) {
			invoice = invoicerepository.save(invoice);
			if (po.get().getInvoiceidlist() == null || po.get().getInvoiceidlist().isEmpty()) {
				Map<String, String> invoicemap = new HashMap<>();
				invoicemap.put(invoice.getId(), invoice.getInvoiceNumber());
				po.get().setNoOfInvoices(po.get().getNoOfInvoices() + 1);
				po.get().setInvoiceidlist(invoicemap);
			} else {
				po.get().getInvoiceidlist().put(invoice.getId(), invoice.getInvoiceNumber());
				po.get().setNoOfInvoices(po.get().getNoOfInvoices() + 1);
			}
			System.out.println("No. of Invoices in Referenced Po with poNumber : " + poNumber + " is : "
					+ po.get().getNoOfInvoices());

		} else
			return Map.of("Error Found , PO is Not found or Not accesible", HttpStatus.SC_CONFLICT);
//		else return new HashMap<>().put("Error Found", HttpStatus.SC_SERVICE_UNAVAILABLE);
//		
		porepo.save(po.get());
		System.out.println("Saving Invoice to database");
		System.out.println("Invoice with details :-> \n" + invoice.toString() + " is saved succesfully");

		Map<String, Object> responseData = new HashMap<>();
		System.err.println("---------------------------------");
		Set<String> remarksSet = new HashSet<>();
		remarksSet.addAll(remarksset);
		responseData.put("Remarks", remarksSet);
		responseData.put("InvoiceAmount", invoiceAmount);
		responseData.put("created_for_Username", username);
		responseData.put("deliveryPlant", deliveryPlant);
		responseData.put("InvoiceNumber", invoiceNumber);
		ktemplate.send("Invoiceinbox",invoice);
		
		return responseData;
	}
}
