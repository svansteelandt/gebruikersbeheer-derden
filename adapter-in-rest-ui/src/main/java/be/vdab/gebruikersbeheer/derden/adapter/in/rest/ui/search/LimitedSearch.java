package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.search;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class LimitedSearch<T> {

	private final T searchCommand;
	private final BiConsumer<T, Integer> searchCommandLimitSetter;
	private int limit;

	public LimitedSearch(T searchCommand, BiConsumer<T, Integer> searchCommandLimitSetter) {
		this.searchCommand = searchCommand;
		this.searchCommandLimitSetter = searchCommandLimitSetter;
	}

	public LimitedSearch<T> setLimit(int limit) {
		this.limit = limit;
		return this;
	}

	private void applyHigherLimit() {
		searchCommandLimitSetter.accept(searchCommand, limit + 1);
	}


	public <R, S> LimitedSearchResult<R, S> execute(Function<T, R> executeSearch, Function<R, List<S>> listFromResult) {
		applyHigherLimit();
		R result = executeSearch.apply(searchCommand);
		List<S> resultList = listFromResult.apply(result);
		boolean hasMore = resultList.size() > limit;
		if (hasMore) {
			resultList = resultList.subList(0, limit);
		}
		return new LimitedSearchResult<>(result, resultList, hasMore);
	}
}
