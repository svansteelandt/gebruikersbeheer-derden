package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

    @NotNull
    private String key;
    @NotNull
    private String message;

    private String field;
    private Object parameters;

    @JsonIgnore
    private boolean hasRejectedParameter;

    public ErrorMessage(String key, String message) {
        this.key = key;
        this.message = message;
        this.hasRejectedParameter = false;

    }

    public ErrorMessage(String key, String message, String field, Object parameters) {
        this.key = key;
        this.message = message;
        this.field = field;
        this.parameters = parameters;
        this.hasRejectedParameter = true;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }

    public Object getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        var errorMessage = "";
        if (key != null) {
            errorMessage += "key: \"" + key + "\", ";
        }
        if (message != null) {
            errorMessage += "message: \"" + message + "\", ";
        }
        if (field != null) {
            errorMessage += "field: \"" + field + "\", ";
        }
        if (hasRejectedParameter) {
            errorMessage += "Rejected: \"" + parameters + "\"";
        }

        if (errorMessage.endsWith(", ")) {
            errorMessage = errorMessage.substring(0, errorMessage.length() - 2);
        }
        return "{ " + errorMessage + " }";
    }
}
