package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import java.util.List;

public record PropertiesDto(String createMlpGebruikerUrl, List<CvsCodeDto> cvsRollen, List<WebCursusCodeDto> webCursussen) {
}
