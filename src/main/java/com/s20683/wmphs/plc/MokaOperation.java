package com.s20683.wmphs.plc;

public interface MokaOperation {
    public void handle(int id, int size, byte[] value);
}
