package com.s20683.wmphs.plc;


import com.s20683.wmphs.plc.tools.TrackId;

interface MokaSlotClient {
    public TrackId proceedValue(TrackId trackId, int[] extraReads, byte[] extraWrites);
    public void onPlcConnected(TrackId trackId);
    default public void init(){ }
    public void unlock();
}
