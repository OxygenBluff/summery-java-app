package com.example.demo.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.error.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	//oohh so that's how u pass it ... @ExceptionHandler then .class name
	public ResponseEntity<ErrorResponse> handleUserExistsException(UserAlreadyExistsException ex){
	var error=ErrorResponse.builder()
	.errorStatus(HttpStatus.CONFLICT.value()) // 409 ?
	.errorMessage(ex.getMessage())
	.errorTimeStamp(System.currentTimeMillis())
	.build();

	return new ResponseEntity<>(error,HttpStatus.CONFLICT);
}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException ex){
	var error=ErrorResponse.builder()
	.errorStatus(HttpStatus.CONFLICT.value()) // 401 = unauthorized
	.errorMessage(ex.getMessage())
	.errorTimeStamp(System.currentTimeMillis())
	.build();

	return new ResponseEntity<>(error,HttpStatus.CONFLICT);
}

@ExceptionHandler(TokenNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleTokenNotFoundException(TokenNotFoundException ex){
	var error=ErrorResponse.builder()
	.errorStatus(HttpStatus.CONFLICT.value()) // 401 (= unauthorized
	.errorMessage(ex.getMessage())
	.errorTimeStamp(System.currentTimeMillis())
	.build();

	return new ResponseEntity<>(error,HttpStatus.CONFLICT);
}

@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
	var error=ErrorResponse.builder()
	.errorStatus(HttpStatus.NOT_FOUND.value())
	.errorMessage(ex.getMessage())
	.errorTimeStamp(System.currentTimeMillis())
	.build();
	return new ResponseEntity<>(error,HttpStatus.NOT_FOUND);
}

//@Not Blank and @Valid !
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
	//why map ?
	//-> FUTURE PROOFING! what if MULTIPLE ERRORS with sneidng the DTO?
	//-> collect all 3 in a hashmap! -> String,String
	Map<String, String> errors = new HashMap<>();

	for (FieldError error: ex.getBindingResult().getFieldErrors()) {
		//field error = created auomatically if validation error!
		//just has rhe message container !
		//ex.getBindingResult = BOX OG ALL RESULTS OF THE CHECKS!!
		//-> FROM THAT BOX -> get just the error conatainers! ->.getFieldErrors
		errors.put(error.getField(),error.getDefaultMessage());
	}
	return ResponseEntity.badRequest().body(errors);
	//.badRequest -> 400 erorr message (bad data sent) -> helper function to create 400 envolope WOW
	//.body(erors) puts the map of erorrs into the envelope sent (ResponseEntity)
}

//wow new excpetion been a while
@ExceptionHandler(DuplicateResourceException.class)
public ResponseEntity<ErrorResponse>  handleDuplicateResource (RuntimeException e){
		var error = ErrorResponse.builder()
				.errorStatus(HttpStatus.CONFLICT.value())
				.errorMessage(e.getMessage())
				.errorTimeStamp(System.currentTimeMillis())
				.build();

		return new ResponseEntity<>(error,HttpStatus.CONFLICT);
	}




//catch eveuthing else 
@ExceptionHandler(RuntimeException.class) //put the entire class loool
public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
    var error = ErrorResponse.builder()
            .errorStatus(HttpStatus.BAD_REQUEST.value()) // 400
            .errorMessage(ex.getMessage())
            .errorTimeStamp(System.currentTimeMillis())
            .build();
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
}


}

