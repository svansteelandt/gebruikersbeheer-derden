package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = InszValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InszConstraint {
    String message() default "Invalid INSZ number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
