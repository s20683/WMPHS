package com.s20683.plc;

import com.s20683.plc.tools.TrackId;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Decision {
    private int decisionValuePosition;
    private int trackIdSlot;
    private TrackId trackId;
    private short decision;

    public Decision(int decisionValuePosition, int trackIdSlot) {
        this.decisionValuePosition = decisionValuePosition;
        this.trackIdSlot = trackIdSlot;
        this.trackId = new TrackId(0);
    }
}
