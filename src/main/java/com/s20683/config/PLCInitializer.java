package com.s20683.config;

import com.s20683.plc.CameraBase;
import com.s20683.plc.PlcBase;
import com.s20683.wmphs.carrier.CarrierService;
import com.s20683.wmphs.scheduler.SingleThreadScheduler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PLCInitializer {
    protected Logger logger = LoggerFactory.getLogger(PLCInitializer.class);

    @Value("${plc.host}")
    private String host;
    @Autowired
    private final SingleThreadScheduler scheduler;
    @Autowired
    private final CarrierService carrierService;
    public PLCInitializer(SingleThreadScheduler scheduler, CarrierService carrierService) {
        this.scheduler = scheduler;
        this.carrierService = carrierService;
    }
    private final short REJECT = 10;

    @PostConstruct
    public void init(){
        PlcBase plcBase = new PlcBase(0, 3, 2);
        plcBase.setAddress(host);
        plcBase.setdBNo(400);

        CameraBase SK10 = new CameraBase("SK10", 1,
                (camera)->{
                    logger.info("Proceeding barcode {}", camera.getBarcode());
                    carrierService.getCarrierDestination(camera.getTrackId(), camera.getBarcode().getCode(),
                            (result) ->{
                                if (!result.isSuccess()) {
                                    logger.info("Wrong result {} camera {} set Decision => REJECT[{}]", result.getException().getMessage(), camera, REJECT);
                                    camera.setTarget(REJECT);
                                    return;
                                }
                                logger.info("Camera {} set Decision => OK[{}]", camera, result.getValue().getTarget());
                                camera.setTarget(result.getValue().getTarget().shortValue());
                            });
                },
                (report) -> {
                    scheduler.submitTask(()->{
                        logger.info("Proceeding report {}", report);
                        carrierService.reportCarrier(report);
                    });
                });
        plcBase.registerCamera(SK10);

        plcBase.start();
    }
}
