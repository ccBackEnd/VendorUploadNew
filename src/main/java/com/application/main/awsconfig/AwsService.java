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
	private String token;

	@Autowired
	InvoiceRepository invoicerepository;

	@Autowired
	PoSummaryRepository porepo;

	@Autowired
	DocDetailsRepository docDetailsRepository;

	public static final String ALGORITHM = "AES";
	public static final int KEY_SIZE = 256;

	public void createBucket(String token, String folderappend) {
		this.bucketName = "vendorportalfiles" + folderappend;
		this.token = token;
		try {
			AmazonS3 awsClient = s3client.awsClientConfiguration(token);
			if (!awsClient.doesBucketExistV2(bucketName))
				awsClient.createBucket(bucketName);
		} catch (AmazonS3Exception e) {
			System.out.println("CREATE BUCKET EXCEPTION");
			System.err.println(e.getErrorMessage());
		}
	}

	public String getUserNameFromToken(String token) {

		String tokenBody = token.split("\\.")[1];
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String payload = new String(decoder.decode(tokenBody));
		System.out.println("payload" + payload);
		return getFieldFromJson(payload, "preferred_username");
	}

	private String getFieldFromJson(String json, String fieldName) {
		String fieldValue = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(json);
			fieldValue = jsonNode.get(fieldName).asText();
			System.out.println(fieldName + " : " + fieldValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fieldValue;
	}

	public DocDetails uploadFile(MultipartFile file, String Invoiceid, String username) throws IOException, Exception {

		System.out.println("token from AWS Service : " + token);
		
		String fileName = Invoiceid.concat("?#" + file.getOriginalFilename());
		
		AmazonS3 awsClient = s3client.awsClientConfiguration(token);
		if (file == null || file.isEmpty())
			throw new ResponseStatusException(HttpStatus.SC_METHOD_FAILURE, "Null or Empty file not Accepted", null);
		if (awsClient.doesObjectExist(bucketName, fileName)) {
			System.err.println("FILE WITH " + file.getOriginalFilename() + " Already Exists !");
		}
		PutObjectResult res = awsClient.putObject(bucketName, fileName, file.getInputStream(), new ObjectMetadata());
		String invoiceFileUrl = bucketName + "XCIDHK2788k99BBSEEL99" + fileName;

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

	public Map<String, Object> uploadMongoFile(String username , String msmecategory, String poNumber, String paymentType,
			String deliveryPlant, String invoiceDate, String invoiceNumber, String invoiceAmount, String mobileNumber,
			String email, String alternateMobileNumber, String alternateEmail, String remarks, String ses,
			boolean isagainstLC, boolean isGst, boolean isTredExchangePayment, String factoryunitnumber,
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
		
		Set<String> set = new HashSet<String>();
		set.add(remarks);
		invoice.setRemarks(set);
		invoice.setUsername(username);
		invoice.setInvoiceAmount(invoiceAmount);
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
		if (invoiceDate != null) {
			invoice.setInvoiceDate(LocalDate.parse(invoiceDate, formatter));
			System.err.println(invoice.getInvoiceDate());
		}

		invoice.setInvoiceNumber(invoiceNumber);
		invoice.setCurrentDateTime(LocalDateTime.now());
		System.err.println("-------------Invoice with : " + invoiceNumber + " Saved Successfully-------------------");

		Optional<PoSummary> po = porepo.findByPoNumber(poNumber);

		if (po.isPresent()) {
			if (po.get().getInvoiceobject() == null || po.get().getInvoiceobject().isEmpty()) {
				List<Invoice> li = new ArrayList<>();
				li.add(invoice);
				po.get().setNoOfInvoices(po.get().getNoOfInvoices() + 1);
				po.get().setInvoiceobject(li);
			} else {
				po.get().getInvoiceobject().add(invoice);
				po.get().setNoOfInvoices(po.get().getNoOfInvoices() + 1);
				System.out.println(po.get().getNoOfInvoices());
			}

		} else
			return Map.of("Error Found , PO is Not found or Not accesible", HttpStatus.SC_CONFLICT);
//		else return new HashMap<>().put("Error Found", HttpStatus.SC_SERVICE_UNAVAILABLE);
//		
		porepo.save(po.get());
		invoice = invoicerepository.save(invoice);

		System.out.println("Saving Invoice to database");
		System.out.println("Invoice with details :-> \n" + invoice.toString() + " is saved succesfully");
		
		Map<String, Object> responseData = new HashMap<>();
		System.err.println("---------------------------------");

		responseData.put("alternateNumber", alternateMobileNumber);
		responseData.put("alternativeEmail", alternateEmail);
		Set<String> remarkslist = new HashSet<>();
		remarkslist.addAll(set);
		responseData.put("Remarks", remarkslist);
		responseData.put("InvoiceAmount", invoiceAmount);
		responseData.put("Username", username);
		responseData.put("deliveryPlant", deliveryPlant);
		responseData.put("InvoiceNumber", invoiceNumber);
		return responseData;
	}
}
