package com.s20683.plc;

import java.util.ArrayList;
import java.util.List;

public class PlcBase extends Moka7{

    private int slotsReadBase;
    private int slotsCount;
    private int readAreaLength;
    private int readBufLen;
    protected byte[] readBuf;
    private List<MokaCamera> cameras = new ArrayList<>();

    public PlcBase(int slotsReadBase, int slotsCount, int readAreaLength) {
        super();

        this.slotsReadBase = slotsReadBase;
        this.slotsCount = slotsCount;
        this.readAreaLength = readAreaLength;
    }
    @Override
    public void init() {
        this.readBufLen = getReadBufferLength();
        this.readBuf = new byte[this.readBufLen];
    }

    public void registerCamera(MokaCamera camera){
        cameras.add(camera);
    }
    @Override
    protected void onConnected() {
        super.onConnected();
    }

    @Override
    protected void onRunLoop() {
        super.onRunLoop();
        read();
        for (MokaCamera camera : cameras) {
            camera.handleCamera(this::readSpecificData, this::writeArea);
        }
    }
    public void readSpecificData(int base, int len, byte[] buffer) {
        if (base < 0 || len < 0 || base + len > readBuf.length) {
            throw new IllegalArgumentException("Invalid base or length");
        }
        System.arraycopy(readBuf, base, buffer, 0, len);
    }
    private void read() {
        readArea(slotsReadBase, readBufLen, readBuf);
    }
    protected int getReadBufferLength() {
        return (slotsCount+readAreaLength)*2 + (32 * cameras.size());
    }
}
