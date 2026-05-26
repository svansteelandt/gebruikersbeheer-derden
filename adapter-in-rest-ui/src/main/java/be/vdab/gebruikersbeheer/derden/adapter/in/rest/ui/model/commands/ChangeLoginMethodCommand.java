package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands;

import be.vdab.gebruikersbeheer.derden.domain.LoginMethod;

public record ChangeLoginMethodCommand(LoginMethod loginMethod) {
}
