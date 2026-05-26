package be.vdab.gebruikersbeheer.derden.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FilterUtils {

	private String regex= "\\.(js|png|css)$";
	private Pattern p = null;

	@PostConstruct
	private void init(){
		this.p= Pattern.compile(regex);
	}

	public boolean ignoreFilter(String uri){
		Matcher m = p.matcher(uri);

		return m.find();
	}
}
