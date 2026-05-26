package be.vdab.gebruikersbeheer.derden.adapter.out.rest.codes;

import be.vdab.gebruikersbeheer.derden.core.domain.Code;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface WebServiceConverter {

	static Code convert(be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Code code) {
		Code codeObject = new Code();
		codeObject.setActief(
				be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Code.ActiefEnum.JA == code.getActief());
		codeObject.setWaarde(code.getWaarde());
		codeObject.setKortLabel(code.getLabels().isEmpty() ? "" : code.getLabels().getFirst().getKortLabel());
		codeObject.setLangLabel(code.getLabels().isEmpty() ? "" : code.getLabels().getFirst().getLangLabel());

		return codeObject;
	}

	static List<Code> convert(List<be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Code> listCode) {
		List<Code> listCodeObject = new ArrayList<>();

		if (listCode != null) {
			for (be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Code code : listCode) {
				listCodeObject.add(convert(code));
			}
		}

		return listCodeObject;
	}

	static List<String> filterAndGetCodeValues(List<Code> listCode, Predicate<Code> predicate) {
		if (listCode == null) {
			return new ArrayList<>();
		}

		return listCode.stream()
				.filter(predicate)
				.map(Code::getWaarde)
				.toList();
	}
}
