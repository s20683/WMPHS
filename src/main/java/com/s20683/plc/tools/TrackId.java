package com.s20683.plc.tools;

import lombok.Data;

@Data
public class TrackId {
    private final short trackId;

    public TrackId(short trackId) {
        this.trackId = trackId;
    }
    public TrackId(int trackId) {
        this((short) trackId);
    }
}

