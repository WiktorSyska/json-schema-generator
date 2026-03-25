package com.example.jsonschemagenerator.loader;


public class JsonLoadException extends Exception {
    public JsonLoadException(String message) {super(message);}
    public JsonLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}