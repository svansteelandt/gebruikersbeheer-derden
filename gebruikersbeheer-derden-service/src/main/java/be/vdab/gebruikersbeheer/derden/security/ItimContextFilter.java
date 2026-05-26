package be.vdab.gebruikersbeheer.derden.security;

import be.vdab.gebruikersbeheer.derden.util.isim.support.ContextManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ItimContextFilter extends OncePerRequestFilter {

	private final ContextManager contextManager;

	public ItimContextFilter(ContextManager contextManager) {
		this.contextManager = contextManager;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			contextManager.init();
			filterChain.doFilter(request, response);
		} finally {
			contextManager.clearSessions();
		}
	}
}
