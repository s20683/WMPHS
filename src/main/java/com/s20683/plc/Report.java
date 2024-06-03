package com.s20683.plc;

import com.s20683.plc.tools.TrackId;
import lombok.Data;

@Data
public class Report {
    private int reportSlot;
    private int reportTrackIdSlot;
    private TrackId reportTrackId;
    private short report;

    public Report(int reportSlot, int reportTrackIdSlot) {
        this.reportSlot = reportSlot;
        this.reportTrackIdSlot = reportTrackIdSlot;
        this.reportTrackId = new TrackId(0);
    }
}
