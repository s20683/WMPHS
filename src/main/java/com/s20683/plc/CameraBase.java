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
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private int db_id;
    private String name;
    private TrackId trackId;
    private Barcode barcode;
    private Consumer<CameraBase> proceedBarcode;
    private Consumer<Report> proceedReport;
    private Decision decision;
    private Report report;
    private boolean initProceedBarcode = false;
    private boolean initProceedReport = false;

    public CameraBase(String name, int db_id, Consumer<CameraBase> proceedBarcode, Consumer<Report> proceedReport) {
        this.name = name;
        this.db_id = db_id;
        this.proceedBarcode = proceedBarcode;
        this.proceedReport = proceedReport;
        this.trackId = new TrackId(0);
        this.decision = new Decision(48, 44);
        this.report = new Report(6, 4);
        this.logger = LoggerFactory.getLogger(getClass() + ":" + this.name);
    }

    public void setTrackId(TrackId trackId, boolean proceeding){
        this.trackId = trackId;
        if (proceeding)
            return;
        if (!initProceedBarcode)
            this.initProceedBarcode = true;
        else
            proceedBarcode.accept(this);
    }
    public void setTarget(short target){
        decision.setDecision(target);
    }

    public void handleCamera(MokaOperation readArea, MokaOperation writeArea){
        byte[] buffer33 = new byte[100];
        readArea.handle(0, 100, buffer33);
        logger.info("Receive buffer {}", Arrays.toString(buffer33));
        byte[] trackIdRow = new byte[2];
        readArea.handle(db_id * 2, 2, trackIdRow);
        short trackIdValue = ByteBuffer.wrap(trackIdRow).getShort();
        if (this.trackId.getTrackId() != trackIdValue) {
            logger.info("New TrackId for {} : {} => {}", this.name, this.trackId, trackIdValue);
            byte[] barcodeRow = new byte[32];
            readArea.handle(10 + 32 * (this.db_id - 1), 32, barcodeRow);
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
        logger.info("Received decision REQ TID {} last TID {}", trackIdValue, this.decision.getTrackId().getTrackId());

        if (trackIdValue != this.decision.getTrackId().getTrackId()) {
            byte[] decision = new byte[2];
            byte[] trackId = new byte[2];
            short decisionValue = this.decision.getDecision();
            short decisionTrackIdValue = this.trackId.getTrackId();
            ByteBuffer buffer = ByteBuffer.wrap(decision);
            buffer.putShort(decisionValue);
            ByteBuffer bufferTID = ByteBuffer.wrap(trackId);
            bufferTID.putShort(decisionTrackIdValue);
            logger.warn("Setting decision {} for trackId {}", decisionValue, trackIdValue);
            this.decision.setDecision(decisionValue);
            this.decision.setTrackId(new TrackId(trackIdValue));
            writeArea.handle(this.decision.getDecisionSlot(), 2, decision);
            writeArea.handle(this.decision.getTrackIdSlot(), 2, trackId);
        }
        byte[] currentReportTrackIdRow = new byte[2];
        byte[] receivedValue = new byte[2];
        readArea.handle(this.report.getReportTrackIdSlot(), 2, currentReportTrackIdRow);
        readArea.handle(this.report.getReportSlot(), 2, receivedValue);
        short currentReportTrackId = ByteBuffer.wrap(currentReportTrackIdRow).getShort();
        logger.info("Received TID {} last TID {}", currentReportTrackId, this.report.getReportTrackId().getTrackId());
        if (currentReportTrackId != this.report.getReportTrackId().getTrackId()) {
            short reportValue = ByteBuffer.wrap(receivedValue).getShort();
            logger.warn("Setting report {} for trackId {}", reportValue, currentReportTrackId);
            this.report.setReport(reportValue);
            this.report.setReportTrackId(new TrackId(currentReportTrackId));
            if (!initProceedReport)
                this.initProceedReport = true;
            else
                proceedReport.accept(this.report);
        }
    }
}
