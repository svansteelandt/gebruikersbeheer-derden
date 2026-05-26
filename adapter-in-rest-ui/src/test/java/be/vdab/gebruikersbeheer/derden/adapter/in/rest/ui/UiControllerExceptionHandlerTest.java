package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.ApiError;
import be.vdab.gebruikersbeheer.derden.exception.ApplicationException;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UiControllerExceptionHandlerTest {
	static final String MESSAGE = "test";
	UiControllerExceptionHandler defaultExceptionHandler;

	@Mock
	MethodParameter methodParameter;
	@Mock
	BindingResult bindingResult;

	@BeforeEach
	void setUp() {
		defaultExceptionHandler = new UiControllerExceptionHandler();
	}

	@Test
	void creeerApiError() {
		var apiError = new ApiError("A");
		apiError.addError("A", "A");
		apiError.addError("A", "A", "F", "R");

		assertThat(apiError.getErrors()).hasSize(2);
	}

	@Test
	void handleNietGevondenException() {
		var exception = new PersonNotFoundException(MESSAGE);

		var apiError = defaultExceptionHandler.handleNietGevondenException(exception);

		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("NOT_FOUND");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEqualTo(MESSAGE);
	}

	@Test
	void handleApplicationException() {
		var exception = new ApplicationException("test");

		var apiError = defaultExceptionHandler.handleApplicationException(exception);

		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("BAD_REQUEST");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEqualTo(exception.getMessage());
	}

	@Test
	void handleOptimisticLockingException() {
		var exception = new OptimisticLockException(MESSAGE);

		var apiError = defaultExceptionHandler.handleOptimisticLockingException(exception);

		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("OPTIMISTIC_LOCKING_EXCEPTION");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEqualTo(MESSAGE);
	}

	@Test
	void handleDateTimeParseException() {
		var exception = new DateTimeParseException("test", "test", 1);

		var apiError = defaultExceptionHandler.handleDateTimeParseException(exception);

		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("INVALID_DATE");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEqualTo(MESSAGE);
	}

	@Test
	void handleMethodArgumentMismatchException() {
		var ex = Mockito.mock(MethodArgumentTypeMismatchException.class);
		when(ex.getMessage()).thenReturn(MESSAGE);

		var apiError = defaultExceptionHandler.handleMethodArgumentTypeMismatchException(ex);

		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("METHOD_ARGUMENT_TYPE_MISMATCH");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEqualTo(MESSAGE);
	}

	@Test
	void handleMessageNotReadableException() {
		var exception = new HttpMessageNotReadableException(MESSAGE, new MockHttpInputMessage(new byte[]{}));

		var apiError = defaultExceptionHandler.handleMessageNotReadableException(exception);

		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("REQUEST_NOT_READABLE");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEqualTo(MESSAGE);
	}

	@Test
	void handleBindingException() {
		var methodArgumentNotValidException = new MethodArgumentNotValidException(methodParameter, bindingResult);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("object", "field", "rejected", false, new String[]{"code"}, new Object[]{"x"}, "msg")));
		when(bindingResult.getGlobalErrors()).thenReturn(List.of(new ObjectError("obj", "defaultmsg")));

		var expectedApiError = new ApiError("id");
		expectedApiError.addError("VALIDATION_FIELD_ERROR", "msg", "field", "rejected");
		expectedApiError.addError("VALIDATION_GLOBAL_ERROR", "defaultmsg", "obj", "");

		var actualApiError = defaultExceptionHandler.handleBindException(methodArgumentNotValidException);

		assertThat(actualApiError.getBody())
				.usingRecursiveComparison()
				.ignoringFields("id")
				.isEqualTo(expectedApiError);
	}

	@Test
	void handleAccessDeniedException() {
		var exception = new AccessDeniedException(MESSAGE);

		var apiError = defaultExceptionHandler.handleAccessDeniedException(exception);

		assertSecurityFout(apiError);
	}

	private static void assertSecurityFout(ResponseEntity<ApiError> apiError) {
		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("SECURITY_ERROR");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEqualTo("Security fout");
	}

	@Test
	void handleSecurityException() {
		var exception = new SecurityException(MESSAGE);

		var apiError = defaultExceptionHandler.handleSecurityException(exception);

		assertSecurityFout(apiError);
	}

	@Test
	void handleInternalServerException() {
		var exception = new RuntimeException(MESSAGE);

		var apiError = defaultExceptionHandler.handleInternalServerException(exception);

		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("INTERNAL_SERVER_ERROR");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEmpty();
	}

	@Test
	void handleDataIntegrityViolationException() {
		var exception = new DataIntegrityViolationException(MESSAGE);

		var apiError = defaultExceptionHandler.handleDataIntegrityViolationException(exception);

		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("DB_CONSTRAINT_VIOLATION");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEmpty();
	}

	@Test
	void handleMissingRequestValueException() {
		var exception = new MissingRequestValueException(MESSAGE);

		var apiError = defaultExceptionHandler.handleMissingRequestValueException(exception);

		assertThat(apiError.getBody()).isNotNull();
		assertThat(apiError.getBody().getErrors().getFirst().getKey()).isEqualTo("MISSING_REQUEST_VALUE");
		assertThat(apiError.getBody().getErrors().getFirst().getMessage()).isEqualTo(MESSAGE);
	}

	@Test
	void handleBrokenPipe() {
		var exception = new ClientAbortException(MESSAGE);
		assertThatNoException().isThrownBy(() -> defaultExceptionHandler.handleBrokenPipe(exception));
	}
}
