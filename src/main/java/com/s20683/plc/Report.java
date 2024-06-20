package com.s20683.plc;

import com.s20683.plc.tools.TrackId;
import lombok.Data;

@Data
public class Report {
    private int reportValuePosition;
    private int reportTrackIdSlot;
    private TrackId reportTrackId;
    private short report;

    public Report(int reportValuePosition, int reportTrackIdSlot) {
        this.reportValuePosition = reportValuePosition;
        this.reportTrackIdSlot = reportTrackIdSlot;
        this.reportTrackId = new TrackId(0);
    }
}
