FROM openjdk:17-jdk-alpine
COPY ./target/VendorPortalUpload-0.0.1-SNAPSHOT.jar vpupload.jar
CMD ["java","-jar","vpupload.jar"]