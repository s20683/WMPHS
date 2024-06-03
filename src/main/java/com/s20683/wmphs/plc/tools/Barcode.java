package com.s20683.wmphs.plc.tools;

import lombok.Getter;

@Getter
public class Barcode {

    private final String code;
    private final String type;
    private Barcode(String code, String type) {
        if (code == null)
            throw new NullPointerException("Barcode is null");
        if (type == null)
            throw new NullPointerException("Barcode has not type");
        this.code = code;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Barcode{" +
                "code='" + code + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public static Barcode createFromCode128(String code) {
        if (code == null)
            throw new IllegalArgumentException("Null barcode string");
        if (code.length() < 3)
            throw new IllegalArgumentException("Code128 barcode string too short, len = " + code.length());
        if (code.startsWith("]C0"))
            throw new IllegalArgumentException("Illegal Code128 prefix “" + code);
        String type = "Code128";

        return new Barcode(code.substring(3), type);
    }
}
