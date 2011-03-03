package edu.washington.cs.knowitall.commonlib.regex;

public class RegexException extends RuntimeException {
    private static final long serialVersionUID = -3534531866062810681L;
    
    public RegexException(String message, Exception e) {
        super(message, e);
    }
}