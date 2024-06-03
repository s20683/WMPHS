package com.s20683.plc;

public interface MokaOperation {
    public void handle(int id, int size, byte[] value);
}
