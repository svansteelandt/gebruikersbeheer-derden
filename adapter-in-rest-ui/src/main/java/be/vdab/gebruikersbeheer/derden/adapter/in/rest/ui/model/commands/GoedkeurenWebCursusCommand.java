package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands;

import java.util.List;

public record GoedkeurenWebCursusCommand(String mlpRolCode, List<String> webCursusCodes) {
}
