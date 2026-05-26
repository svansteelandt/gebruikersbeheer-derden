package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands;

import java.util.List;

public record WijsRollenToeAanGebruikerCommand(List<String> globalIdsVanRollen, String cvsRole) {
}
