package be.vdab.gebruikersbeheer.derden.extension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class FlashMap {

	public static final String FLASH_MAP_ATTRIBUTE = FlashMap.class.getName();

	@SuppressWarnings("unchecked")
	public static void processSessionMessages(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			Map<String, Object> flash = (Map<String, Object>) session.getAttribute(FlashMap.FLASH_MAP_ATTRIBUTE);
			if (flash != null) {
				for (Map.Entry<String, ?> entry : flash.entrySet()) {
					Object currentValue = request.getAttribute(entry.getKey());
					if (currentValue == null) {
						request.setAttribute(entry.getKey(), entry.getValue());
					}					
				}
				session.removeAttribute(FlashMap.FLASH_MAP_ATTRIBUTE);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Message> getCurrent(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Map<String, Message> flash = (Map<String, Message>) session.getAttribute(FLASH_MAP_ATTRIBUTE);
		if (flash == null) {
			flash = new HashMap<>();
			session.setAttribute(FLASH_MAP_ATTRIBUTE, flash);
		}
		return flash;
	}

	private FlashMap() {

	}

	public static void put(String key, Message m) {
		getCurrent(getRequest(RequestContextHolder.currentRequestAttributes())).put(key, m);
	}

	public static void setInfoMessage(String message, String info) {
		put(message, new Message(message, MessageType.notificationInfo, info));
	}

	public static void setWarningMessage(String message, String warning) {
		put(message, new Message(message, MessageType.notificationWarning, warning));
	}

	public static void setErrorMessage(String message, String error) {
		put(message, new Message(message, MessageType.notificationErrorWarning, error));
	}

	public static void setSuccessMessage(String message, String success) {
		put(message, new Message(message, MessageType.notificationOK, success));
	}

	private static HttpServletRequest getRequest(RequestAttributes requestAttributes) {
		return ((ServletRequestAttributes) requestAttributes).getRequest();
	}

	public static final class Message implements Serializable {

		private final MessageType type;

		private final String text;
		private final String key;

		public Message(String message, MessageType type, String text) {
			this.type = type;
			this.text = text;
			this.key = message;
		}

		public MessageType getType() {
			return type;
		}

		public String getKey() {
			return key;
		}

		public String getText() {
			/*if (staticClearKey.equals(key)) {
				getRequest(RequestContextHolder.currentRequestAttributes()).removeAttribute(staticClearKey);
			}*/
			return text;
		}

		public String toString() {
			return type + ": " + text;
		}

	}

	public enum MessageType {
		notificationInfo, notificationOK, notificationWarning, notificationErrorWarning
	}
}
