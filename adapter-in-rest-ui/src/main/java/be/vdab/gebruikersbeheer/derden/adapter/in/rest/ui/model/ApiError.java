package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiError {

    private String id;
    private List<ErrorMessage> errors;

    public ApiError(String id) {
        this.id = id;
        this.errors = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public List<ErrorMessage> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void addError(String key, String message) {
        errors.add(new ErrorMessage(key, message));
    }

    public void addError(String key, String message, String field, Object rejectedValue) {
        errors.add(new ErrorMessage(key, message, field, rejectedValue));
    }

    @Override
    public String toString() {
        return "Api error: " + id + ". Errors: " + errors;
    }

}

