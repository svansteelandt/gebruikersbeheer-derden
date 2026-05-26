package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.ApiError;
import be.vdab.gebruikersbeheer.derden.exception.ApplicationException;
import be.vdab.gebruikersbeheer.derden.exception.NotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.PersonRoleAssignmentNotYetFinishedException;
import be.vdab.hermes.logstash.core.ProcessId;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

@RestControllerAdvice("be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui")
@RequiredArgsConstructor
@Slf4j
public class UiControllerExceptionHandler {
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNietGevondenException(NotFoundException e) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("NOT_FOUND", e.getMessage());
		logDebug(e, apiError);
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(OptimisticLockException.class)
	public ResponseEntity<ApiError> handleOptimisticLockingException(OptimisticLockException e) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("OPTIMISTIC_LOCKING_EXCEPTION", e.getMessage());
		logWarning(e, apiError);
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ApiError> handleApplicationException(ApplicationException ex) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("BAD_REQUEST", ex.getMessage());
		return ResponseEntity.badRequest()
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(PersonRoleAssignmentNotYetFinishedException.class)
	public ResponseEntity<ApiError> handleApplicationException(PersonRoleAssignmentNotYetFinishedException ex) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("NOT_YET_FULLY_PROCESSED", ex.getMessage());
		return ResponseEntity.badRequest()
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}



	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiError> handleBindException(BindException ex) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());

		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			apiError.addError("VALIDATION_FIELD_ERROR", error.getDefaultMessage(), error.getField(), error.getRejectedValue());
		}
		for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
			apiError.addError("VALIDATION_GLOBAL_ERROR", error.getDefaultMessage(), error.getObjectName(), "");
		}

		return ResponseEntity.badRequest()
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(DateTimeParseException.class)
	public ResponseEntity<ApiError> handleDateTimeParseException(DateTimeParseException ex) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("INVALID_DATE", ex.getMessage());
		return ResponseEntity.badRequest()
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
		//b.v. wanneer foutieve enum waarde als request parameter
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("METHOD_ARGUMENT_TYPE_MISMATCH", ex.getMessage());
		return ResponseEntity.badRequest()
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleMessageNotReadableException(HttpMessageNotReadableException ex) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("REQUEST_NOT_READABLE", ex.getMostSpecificCause().getMessage());
		return ResponseEntity.badRequest()
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
		return handleSecurityFout();
	}

	private static ResponseEntity<ApiError> handleSecurityFout() {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("SECURITY_ERROR", "Security fout"); //we geven niet meer details, want anders kan fout zeggen dat gebruiker niet gevonden is...
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}


	@ExceptionHandler(SecurityException.class)
	public ResponseEntity<ApiError> handleSecurityException(SecurityException e) {
		return handleSecurityFout();
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleInternalServerException(Exception e) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("INTERNAL_SERVER_ERROR", ""); //we geven niet meer details, wegens security
		logError(e, apiError);
		return ResponseEntity.internalServerError()
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("DB_CONSTRAINT_VIOLATION", ""); //we geven niet meer details, wegens security
		logError(e, apiError);
		return ResponseEntity.badRequest()
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(MissingRequestValueException.class)
	public ResponseEntity<ApiError> handleMissingRequestValueException(MissingRequestValueException e) {
		var apiError = new ApiError(ProcessId.getVdabProcessId());
		apiError.addError("MISSING_REQUEST_VALUE", e.getMessage());
		return ResponseEntity.badRequest()
				.contentType(MediaType.APPLICATION_JSON)
				.body(apiError);
	}

	@ExceptionHandler(ClientAbortException.class)
	public void handleBrokenPipe(ClientAbortException e) {
		log.debug("ClientAbortException on url: {}", e.getMessage());
	}

	private void logDebug(Exception e, ApiError apiError) {
		if (log.isDebugEnabled()) {
			log.debug(apiError.toString(), e);
		}
	}

	private void logError(Exception e, ApiError apiError) {
		if (log.isErrorEnabled()) {
			log.error(apiError.toString(), e);
		}
	}

	private void logWarning(Exception e, ApiError apiError) {
		if (log.isWarnEnabled()) {
			log.warn(apiError.toString(), e);
		}
	}

}
