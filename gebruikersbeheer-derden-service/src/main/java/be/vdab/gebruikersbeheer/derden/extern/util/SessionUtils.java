package be.vdab.gebruikersbeheer.derden.extern.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class SessionUtils {
	
	public static void setSession(HttpServletRequest request, String key, Object object) {
		HttpSession session= request.getSession();
		
		/*if (logger.isDebugEnabled()){
			logger.debug("setSession Object id : {} key: {} obj: ", session.getId(), key, object.getClass().getName());
		}*/
		
		session.setAttribute("be_optis_extern_" + key, object);
	}

	public static Object getSession(HttpServletRequest request, String key) {
		HttpSession session= request.getSession();
		Object obj= session.getAttribute("be_optis_extern_" + key);
		
		if (log.isDebugEnabled()){
			StringBuilder buf= new StringBuilder(75);
			buf.append("getSession id: ");
			buf.append(session.getId());
			buf.append(" key: ");
			buf.append(key);
			buf.append(" obj: ");
			
			if (obj != null){
				buf.append(obj.getClass().getName());
			}
		}
		
		return obj;
	}
}
