package com.s20683.wmphs.plc;


import com.s20683.wmphs.plc.tools.TrackId;

import java.util.ArrayList;
import java.util.List;


public class PlcBase extends Moka7{

    private int slotsReadBase;
    private int slotsCount;
    private int readAreaLength;
    private int writeAreaBase;
    private int writeAreaLen;
    private final byte[] slotsWriteBuf;
    private final MokaSlotClient[] slots;
    private final int readBufLen;
    protected final byte[] readBuf;
    protected final int[] extraReadWords;
    private List<CameraBase> cameras = new ArrayList<>();

    public PlcBase(int slotsReadBase, int slotsCount, int readAreaLength, int writeAreaBase, int writeAreaLen) {
        super();

        this.slotsReadBase = slotsReadBase;
        this.slotsCount = slotsCount;
        this.readAreaLength = readAreaLength;
        this.writeAreaBase = writeAreaBase;
        this.writeAreaLen = writeAreaLen;

        this.slots = new MokaSlotClient[slotsCount];
        this.slotsWriteBuf = new byte[2*slotsCount];
        this.readBufLen = getReadBufferLength();
        this.readBuf = new byte[this.readBufLen];
        extraReadWords = new int[readAreaLength];



    }

    public void registerCamera(CameraBase camera){
        cameras.add(camera);
    }
    @Override
    protected void onConnected() {
        super.onConnected();
        readArea(this.writeAreaBase, 2*this.slotsCount, this.slotsWriteBuf);
        for (int i=0; i < slotsCount; ++i) {
            MokaSlotClient slot = slots[i];
            if (slot == null)
                continue;
//            slot.onPlcConnected(new TrackId(slotsWriteBuf, 2*i));
        }
    }

    @Override
    protected void onRunLoop() {
        super.onRunLoop();
        read();
        for (CameraBase camera : cameras) {
            camera.handleCamera(this::readArea, this::writeArea);
        }
    }
    private final void read() {
        readArea(slotsReadBase, readBufLen, readBuf);
    }
    protected int getReadBufferLength() {
//        return (slotsCount+readAreaLength)*2;
        return 1024;
    }
}
