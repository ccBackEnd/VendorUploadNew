package com.application.main.Service;

//@Service
//public class DSC_SERVICE {
//
//	Logger logApp = LoggerFactory.getLogger(DSC_SERVICE.class);
//	private MultipartFile file;
//
//	// Extracts signed content for verification
//	private byte[] getByteRangeData(ByteArrayInputStream bis, int[] byteRange) throws IOException {
//	logApp.info("Getting Byte Range");
//		bis.skip(byteRange[0]);
//		byte[] contentSigned = new byte[byteRange[1] + byteRange[3]];
//		bis.read(contentSigned, 0, byteRange[1]);
//		bis.skip(byteRange[2] - byteRange[1] - byteRange[0]);
//		bis.read(contentSigned, byteRange[1], byteRange[3]);
//		bis.reset();
//		return contentSigned;
//	}
//
//	// Main verification function
//	public boolean verify(MultipartFile file) throws IOException {
//		logApp.info("Verification Started...");
//		this.file = file;
//		try (PdfReader pdfReader = new PdfReader(file.getInputStream()); PdfDocument doc = new PdfDocument(pdfReader)) {
//			SignatureUtil signUtil = new SignatureUtil(doc);
//			List<String> signatureNames = signUtil.getSignatureNames();
//			if (signatureNames == null || signatureNames.isEmpty()) {
//				logApp.error("PDF is not digitally signed. , Please Sign it accordingly");
//				return false;
//			}
//			return digitalVerification(); // Proceed to detailed digital signature verification
//		} catch (Exception e) {
//			logApp.error("Error verifying PDF signature", e);
//			throw new ResponseStatusException(HttpStatusCode.valueOf(400),
//					"Error processing the PDF: " + e.getMessage());
//		}
//	}
//
//	// Verifies digital signature details
//	public boolean digitalVerification() throws Exception {
//		logApp.info("Digital Verification Started...");
//		try (PDDocument pdfDoc = PDDocument.load(file.getInputStream())) {
//			ByteArrayInputStream pdfBytes = new ByteArrayInputStream(file.getBytes());
//			for (PDSignature signature : pdfDoc.getSignatureDictionaries()) {
//				if (processSignature(signature, pdfBytes))
//					return true; // Process each signature
//			}
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
//		logApp.error("Verification Failed...");
//		return false;
//	}
//
//	// Subfilter validation logic: Ensures any valid subfilter passes
//	private boolean isValidSubFilter(String subFilter) {
//		logApp.info("Checking for Valid SubFilter...");
//		boolean verified = false;
//		 if(subFilter.contains("ETSI.CAdES.detached") || subFilter.contains("adbe.pkcs7.detached")) {
//			 logApp.info(subFilter + " : non verified Timestamp");
//			 verified = true;
//		 }
//		 if(subFilter.contains("ETSI.RFC3161")) {
//			 logApp.info(subFilter + " : verified Timestamp");
//			
//		 }
//		 return verified;
//		 
//	}
//
//	// Processes and validates each PDF signature
//	@SuppressWarnings("unchecked")
//	private boolean processSignature(PDSignature signature, ByteArrayInputStream pdfBytes) throws Exception {
//		byte[] contentToSigned = getByteRangeData(pdfBytes, signature.getByteRange());
//		String filter = signature.getFilter();
//		String subFilter = signature.getSubFilter();
//		String contactInfo = Optional.ofNullable(signature.getContactInfo()).orElse("N/A");
//		String reason = Optional.ofNullable(signature.getReason()).orElse("N/A");
//
//		// Log signature metadata
//		logApp.info("Processing Signature - Filter: {}, SubFilter: {}, Contact: {}, Reason: {}", filter, subFilter,
//				contactInfo, reason);
//
//		// Filter and subfilter validation
//		if (!filter.trim().equalsIgnoreCase("Adobe.PPKLite") || !isValidSubFilter(subFilter)) {
//			logApp.error("Cannot process PDF Signature {} with invalid filter or subFilter", signature.getName());
//			return false;
//		}
//		logApp.info("Valid SubFilters are Present for Level 3 DSC");
//		// Parse the PKCS#7 Data (CMS Signed Data)
//		CMSSignedData signedData = new CMSSignedData(signature.getContents());
//		SignerInformation signerInfo = signedData.getSignerInfos().getSigners().iterator().next();
//
//		// Extract Message Digest Attribute
//		Attribute digestAttribute = signerInfo.getSignedAttributes().get(PKCSObjectIdentifiers.pkcs_9_at_messageDigest);
//		String messageDigest = extractMessageDigest(digestAttribute, subFilter, signedData);
//
//		// Validate the Public Key & Signature Algorithm
//		PublicKey pubKey = extractPublicKey(signedData, signerInfo);
//		boolean isSignatureValid = validateSignature(pubKey, signerInfo, contentToSigned, messageDigest);
//
//		// Log the result
//		if (isSignatureValid) {
//			logApp.info("Signature ID {} is valid and the data integrity is OK.",
//					signerInfo.getSID().getSerialNumber().toString(16));
//			return true;
//		} else {
//			logApp.error("Signature ID {} is invalid, data integrity is compromised.",
//					signerInfo.getSID().getSerialNumber().toString(16));
//			return false;
//		}
//	}
//
//	// Extract Message Digest (from either ETSI timestamp or CMS signed data)
//	private String extractMessageDigest(Attribute digestAttribute, String subFilter, CMSSignedData signedData)
//			throws Exception {
//		logApp.info("Extracting Message Digest...");
//		if (subFilter.contains("ETSI.RFC3161") && subFilter.contains("ETSI.CAdES.detached")) {
//			TimeStampToken timeToken = new TimeStampToken(signedData);
//			return Base64.getEncoder().encodeToString(timeToken.getTimeStampInfo().getMessageImprintDigest());
//		} else {
//			return Base64.getEncoder()
//					.encodeToString(Hex.decode(digestAttribute.getAttributeValues()[0].toString().substring(1)));
//		}
//	}
//
//	// Extract public key from certificate data
//	private PublicKey extractPublicKey(CMSSignedData signedData, SignerInformation signerInfo) throws Exception {
//		logApp.info("Extracting Public Key...");
//		Collection<X509CertificateHolder> matches = signedData.getCertificates().getMatches(signerInfo.getSID());
//		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(
//				matches.iterator().next().getSubjectPublicKeyInfo().getEncoded());
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		return keyFactory.generatePublic(keySpec);
//	}
//
//	// Validate the signature using public key and the calculated digest
//	private boolean validateSignature(PublicKey pubKey, SignerInformation signerInfo, byte[] contentToSigned,
//			String messageDigest) throws Exception {
//		logApp.info("Validating Signature...");
//		MessageDigest digest = MessageDigest.getInstance(signerInfo.getDigestAlgOID());
//		String calculatedDigest = Base64.getEncoder().encodeToString(digest.digest(contentToSigned));
//
//		if (!messageDigest.equals(calculatedDigest)) {
//			logApp.error("Message Digest mismatch between CMS and PDF.");
//			return false;
//		}
//
//		Signature rsaSignature = Signature.getInstance(getSignatureAlgorithm(digest.getAlgorithm()));
//		if (rsaSignature == null) {
//			logApp.error("NULL ALGORITHM");
//			return false;
//		}
//		rsaSignature.initVerify(pubKey);
//		rsaSignature.update(signerInfo.getEncodedSignedAttributes());
//		return rsaSignature.verify(signerInfo.getSignature());
//	}
//
//	// Determine the signature algorithm based on the digest algorithm
//	private String getSignatureAlgorithm(String digestAlgorithm) {
//		logApp.info("Getting Signature Algorithm...");
//		switch (digestAlgorithm) {
//		case "2.16.840.1.101.3.4.2.1":
//			return "SHA256withRSA";
//		case "2.16.840.1.101.3.4.2.2":
//			return "SHA384withRSA";
//		case "2.16.840.1.101.3.4.2.3":
//			return "SHA512withRSA";
//		default:
//			return null; // Fallback to generic RSA if not identified
//		}
//	}
//}