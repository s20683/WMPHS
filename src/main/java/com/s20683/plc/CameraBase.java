package com.s20683.plc;


import com.s20683.plc.tools.Barcode;
import com.s20683.plc.tools.TrackId;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

@Data
public class CameraBase implements MokaCamera{
    protected Logger logger = LoggerFactory.getLogger(getClass() + ":SK");

    private int db_id;
    private String name;
    private TrackId trackId;
    private Barcode barcode;
    private Consumer<CameraBase> consumer;
    private Decision decision;
    private Report report;

    public CameraBase(String name, int db_id, Consumer<CameraBase> consumer) {
        this.name = name;
        this.db_id = db_id;
        this.consumer = consumer;
        this.trackId = new TrackId(0);
        this.decision = new Decision(50 + db_id, 50 + db_id + 2);
        this.report = new Report(100 + db_id, 100 + db_id + 2);
        this.logger = LoggerFactory.getLogger(getClass() + ":SK" + this.name);
    }

    public void setTrackId(TrackId trackId, boolean proceeding){
        this.trackId = trackId;
        if (proceeding)
            return;
        consumer.accept(this);
    }
    public void setTarget(short target){
        decision.setDecision(target);
        decision.setTrackId(this.trackId);
    }

    public void handleCamera(MokaOperation readArea, MokaOperation writeArea){
        byte[] trackIdRow = new byte[2];
        readArea.handle((this.db_id - 1) * 2, 2, trackIdRow);
        short trackIdValue = ByteBuffer.wrap(trackIdRow).getShort();
        if (this.trackId.getTrackId() != trackIdValue) {
            logger.info("New TrackId for {} : {} => {}", this.name, this.trackId, trackIdValue);
            byte[] barcodeRow = new byte[32];
            readArea.handle(350 + 32 * (this.db_id - 10), 32, barcodeRow);
            String barcodeString = StringUtils.substringBefore(new String(barcodeRow), "\0");
            try {
                Barcode barcode = Barcode.createFromCode128(barcodeString);
                logger.info("{}: {} → {}.proceedBarcode({}) → {}", this.name, Arrays.toString(barcodeRow), barcodeString, barcode, barcode.getCode());
                setBarcode(barcode);
                setTrackId(new TrackId(trackIdValue), false);
            } catch (IllegalArgumentException e) {
                logger.warn("Exception while proceeding barcode", e);
                setTrackId(new TrackId(trackIdValue), true);
            }
        }
        byte[] currentDecisionTrackIdRow = new byte[2];
        readArea.handle(this.decision.getTrackIdSlot(), 2, currentDecisionTrackIdRow);
        short currentDecisionTrackId = ByteBuffer.wrap(currentDecisionTrackIdRow).getShort();
        if (currentDecisionTrackId != this.decision.getTrackId().getTrackId()) {
            byte[] decision = new byte[2];
            byte[] trackId = new byte[2];
            short decisionValue = this.decision.getDecision();
            short decisionTrackIdValue = this.trackId.getTrackId();
            logger.warn("Setting decision {} for trackId {}", decisionValue, trackIdValue);
            ByteBuffer buffer = ByteBuffer.wrap(decision);
            buffer.putShort(decisionValue);
            ByteBuffer bufferTID = ByteBuffer.wrap(trackId);
            bufferTID.putShort(decisionTrackIdValue);
            writeArea.handle(this.decision.getDecisionSlot(), 2, decision);
            writeArea.handle(this.decision.getTrackIdSlot(), 2, trackId);
        }
        byte[] currentReportTrackIdRow = new byte[2];
        readArea.handle(this.report.getReportSlot(), 2, currentReportTrackIdRow);
        short currentReportTrackId = ByteBuffer.wrap(currentReportTrackIdRow).getShort();
        logger.info("{} : {}", currentReportTrackId, this.report.getReportTrackId().getTrackId());
        if (currentReportTrackId != this.report.getReportTrackId().getTrackId()) {
            byte[] report = new byte[2];
            byte[] newReportTrackId = new byte[2];
            short reportValue = this.report.getReport();
            short reportTrackIdValue = this.report.getReportTrackId().getTrackId();
            logger.warn("Setting report {} for trackId {}", reportValue, reportTrackIdValue);
            ByteBuffer buffer = ByteBuffer.wrap(report);
            buffer.putShort(reportValue);
            ByteBuffer bufferTID = ByteBuffer.wrap(newReportTrackId);
            bufferTID.putShort(reportTrackIdValue);
            writeArea.handle(this.report.getReportSlot(), 2, report);
            writeArea.handle(this.report.getReportTrackIdSlot(), 2, newReportTrackId);
        }
    }
}
