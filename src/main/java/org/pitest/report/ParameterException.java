package org.pitest.report;

public class ParameterException extends Exception {

    private static final long serialVersionUID = 1L;
    public static final int INVALID_PARAMETER = 1;
    public static final int MISSING_PARAMETER = 2;
    int code = 0;

    public ParameterException(String text, int code) {
        super(text);
        this.code = code;
    }
}