package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.extern.view.PersonCommand;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@SessionAttributes(types = {PersonCommand.class})
public abstract class ExternAbstractController {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomBooleanEditor(true));
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
		binder.registerCustomEditor(Integer.class, null, new CustomNumberEditor(Integer.class, true));
		binder.registerCustomEditor(Double.class, null, new CustomNumberEditor(Double.class, true));
		binder.registerCustomEditor(Long.class, null, new CustomNumberEditor(Long.class, true));
	}
}