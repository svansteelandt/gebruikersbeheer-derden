package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands;

import be.vdab.iam.validations.phone.annotation.E164;
import be.vdab.validatie.constraint.email.Email;
import be.vdab.validatie.constraint.gsm.Gsm;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import jakarta.validation.constraints.Size;


@Builder(toBuilder = true)
public record EditGebruikerCommand(
		@NotNull @Size(max = NAME_MAX_SIZE, message = "Voornaam mag maximaal {max} karakters bevatten") String firstName,
		@NotNull @Size(max = NAME_MAX_SIZE, message = "Achternaam mag maximaal {max} karakters bevatten") String lastName,
		@NotNull @Email String email,
		@NotNull @E164 String phone,
		@NotNull @Gsm @E164 String mobile,
		@NotNull boolean suspend,
		@Size(max = SUSPEND_OMSCHRIJVING_MAX_SIZE, message = "Reden voor blokkering max maximaal {max} karakters bevatten") String suspendOmschrijving
) {

	public static final int NAME_MAX_SIZE = 25;
	public static final int SUSPEND_OMSCHRIJVING_MAX_SIZE = 500;

	public EditGebruikerCommand {
		if (!suspend) {
			suspendOmschrijving = null;
		}
	}
}
