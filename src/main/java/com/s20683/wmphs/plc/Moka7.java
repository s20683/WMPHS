package com.s20683.wmphs.plc;

import Moka7.S7;
import Moka7.S7Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Moka7 implements Runnable{

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected S7Client connection;
    private String address;
    //todo co to rack
    private int rack = 0;
    //todo slot1
    private int slot = 1;
    private int dBNo = 12;
    private boolean connectionOk = false;

    protected String threadName = "PLC";

    public Moka7() {
        this.connection = new S7Client();
    }


    private boolean connect() {
        int connectionStatus = this.connection.ConnectTo(this.address, this.rack, this.slot);

        switch (connectionStatus) {
            case 0-> {
                logger.info("PLC connection success!");
                this.connectionOk = true;
                onConnected();
                return true;
            }
            default -> {
                logger.info("Error while connection to PLC, error status {}", S7Client.ErrorText(connectionStatus));
                return false;
            }
        }
    }
    public void writeArea(int base, int len, byte[] buffer) {
        int status = connection.WriteArea(S7.S7AreaDB, dBNo, base, len, buffer);
        if (status != 0)
            throw new IllegalStateException("PLC error on writeArea " + S7Client.ErrorText(status));
    }

    public void readArea(int base, int len, byte[] buffer) {
        int status = connection.ReadArea(S7.S7AreaDB, dBNo, base, len, buffer);
        if (status != 0)
            throw new IllegalStateException("PLC error on writeArea " + S7Client.ErrorText(status));
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setdBNo(int dBNo) {
        this.dBNo = dBNo;
    }

    protected void onConnected(){

    }
    protected void onRunLoop(){}
    public static int getWord(byte[] src, int offset) {
        return ((src[offset] & 0xff) << 8) | (src[offset+1] & 0xff);
    }

    @Override
    public void run() {
        logger.info("PLC Thread started!");
        try {
            while (true) {
                //todo dodac oczekiwanie na inicializacje
                while (true) {
                    try {
                        while (! connect())
                            Thread.sleep(5000);
                        while(connectionOk) {
                            onRunLoop();
                            Thread.sleep(20);
                        }

                    } catch (IllegalStateException e) {
                        logger.warn("",e);
                        if (connectionOk) {
                            connectionOk = false;
                            connection.Disconnect();
                        }
                        Thread.sleep(5000);
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.warn("",e);
        }
    }
    public void start() {
        logger.info("Starting PLC thread {}", threadName);
        (new Thread(this, threadName)).start();
    }

}
