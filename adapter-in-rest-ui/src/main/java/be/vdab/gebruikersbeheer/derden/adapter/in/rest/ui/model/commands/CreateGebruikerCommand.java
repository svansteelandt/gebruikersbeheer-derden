package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands;

import be.vdab.iam.validations.insz.Insz;
import be.vdab.iam.validations.phone.annotation.E164;
import be.vdab.validatie.constraint.email.Email;
import be.vdab.validatie.constraint.gsm.Gsm;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record CreateGebruikerCommand(
		@NotNull String firstName,
		@NotNull String lastName,
		@Insz String rrnr,
		@NotNull @Email String email,
		@NotNull @E164 String phone,
		@NotNull @Gsm @E164 String mobile,
		@NotNull List<String> roleGlobalIds,
		@NotNull boolean hasNoRrnr
) {

}
