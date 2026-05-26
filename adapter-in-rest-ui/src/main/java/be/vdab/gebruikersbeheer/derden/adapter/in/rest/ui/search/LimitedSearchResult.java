package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.search;

import java.util.List;

public record LimitedSearchResult<R, T>(R result, List<T> limitedList, boolean tooManyResults) {
}




