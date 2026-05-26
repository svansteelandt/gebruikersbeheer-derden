package be.vdab.gebruikersbeheer.derden.asserters;

import jakarta.validation.ConstraintViolation;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

public class ConstraintViolationAssert<T> extends AbstractAssert<ConstraintViolationAssert<T>, Set<ConstraintViolation<T>>> {

	public ConstraintViolationAssert(Set<ConstraintViolation<T>> tConstraintViolation) {
		super(tConstraintViolation, ConstraintViolationAssert.class);
	}

	public static <T> ConstraintViolationAssert<T> assertThat(Set<ConstraintViolation<T>> constraintViolations) {
		return new ConstraintViolationAssert<>(constraintViolations);
	}


	public ConstraintViolationAssert<T> hasNoViolations() {
		Assertions.assertThat(this.actual).isEmpty();
		return this;
	}

	public ConstraintViolationAssert<T> hasViolationForProperty(String property, Class<? extends Annotation> annotation) {
		this.actual.stream()
				.filter(c -> c.getPropertyPath().toString().equals(property))
				.filter(c -> c.getConstraintDescriptor().getAnnotation().annotationType().equals(annotation))
				.findAny()
				.orElseThrow(() -> new AssertionError("No constraint violation on " + property + " for annotation " + annotation));
		return this;
	}

	public ConstraintViolationAssert<T> hasViolationForProperty(String property) {
		this.actual.stream()
				.filter(c -> c.getPropertyPath().iterator().next().getName().equals(property))
				.findAny()
				.orElseThrow(() -> new AssertionError("No constraint violation on " + property));
		return this;
	}

	public ConstraintViolationAssert<T> hasNoViolationForProperty(String property) {
		Optional<ConstraintViolation<T>> constraintViolation = this.actual.stream()
				.filter(c -> c.getPropertyPath().iterator().next().getName().equals(property))
				.findAny();
		if (constraintViolation.isPresent()) {
			throw new AssertionError("Constraint violation on " + property);
		}
		return this;
	}


	public ConstraintViolationAssert<T> hasViolationForListElement(String listProperty, int index, Class<? extends Annotation> annotation) {
		return hasViolationForProperty(listProperty + "[" + index + "]" + ".<list element>", annotation);
	}


}

