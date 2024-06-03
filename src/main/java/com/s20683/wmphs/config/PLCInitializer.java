package com.s20683.wmphs.config;

import com.s20683.wmphs.plc.CameraBase;
import com.s20683.wmphs.plc.PlcBase;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PLCInitializer {
    protected Logger logger = LoggerFactory.getLogger(PLCInitializer.class);


    @PostConstruct
    public void init(){
        PlcBase plcBase = new PlcBase(0, 128, 47, 2048, 194);
        plcBase.setAddress("127.0.0.1");
        plcBase.setdBNo(400);

        CameraBase SK10 = new CameraBase("SK10", 10, (camera)->{
            logger.info("Proceeding barcode {}", camera.getBarcode());
            Random random = new Random();
            short randomShort = (short) (random.nextInt(44) + 1);
            camera.setTarget(randomShort);
        });
        plcBase.registerCamera(SK10);

        plcBase.start();
    }
}
