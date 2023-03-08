package com.HKJC.SolMessager;

import java.io.Serializable;

public class Response  implements Serializable{
    public Serializable data;
    public Integer code;
    public String message;

    public Response(Serializable data, Integer code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static Response success(Serializable data) {
        return new Response(data, 200, "OK");
    }

    public static Response error(Integer code, String message) {
        return new Response(null, code, message);
    }

    public String toString() {
        return "code:" + this.code + " message: " + this.message + " data:" + this.data;
    }
}
