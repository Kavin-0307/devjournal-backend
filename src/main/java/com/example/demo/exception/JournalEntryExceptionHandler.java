package com.example.demo.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
 import org.springframework.web.HttpMediaTypeNotAcceptableException;

 //It serves as a global exception handler for all controllers in your application. It centralizes the error handling logic and ensures consistent and meaningful error responsess
 @ControllerAdvice
public class JournalEntryExceptionHandler extends ResponseEntityExceptionHandler {
	Logger logger=LoggerFactory.getLogger(JournalEntryExceptionHandler.class);//SLF4J is used to log the details of exceptions that occur

	@ExceptionHandler({
		IllegalArgumentException.class,
		IllegalStateException.class
	
	})
	protected ResponseEntity<Object> handleConflict(RuntimeException ex,WebRequest request)
	{
		//it is a custom handler for specific runtime exceptions that often indicate a resource conflict or invalid sate in application logic
		
		logger.error("An error message");
		String bodyOfResponse="This should be application specific";//placeholder indicating custom error message
		return handleExceptionInternal(ex,bodyOfResponse,new HttpHeaders(),HttpStatus.CONFLICT,request);
		
	}
	@Override
	//@Override protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(...): This is called when the client requests a response format (via the Accept header) that the server cannot produce (e.g., requesting application/pdf from an API that only returns application/json).
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,HttpHeaders headers,HttpStatusCode status,WebRequest request){
		return handleExceptionInternal(ex,"Media not acceptable for this endpoint",headers,status,request);
	}

}
