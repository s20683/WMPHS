package com.s20683.wmphs.plc;

import com.s20683.wmphs.plc.tools.TrackId;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Decision {
    private int decisionSlot;
    private int trackIdSlot;
    private TrackId trackId;
    private short decision;

    public Decision(int decisionSlot, int trackIdSlot) {
        this.decisionSlot = decisionSlot;
        this.trackIdSlot = trackIdSlot;
        this.trackId = new TrackId(0);
    }
}
