package com.bilibili.domain.exception;

public class ConditionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String code;

    public ConditionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ConditionException(String message) {
        super(message);
        this.code = "500";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
