package com.HKJC.Exceptions;

public class HKJCException extends Exception {
    // error type 400: bad request
    // error type 500: internal server error
    public int type;

    public HKJCException(int type, String msg) {
        super(msg);
        this.type = type;
    }
}
