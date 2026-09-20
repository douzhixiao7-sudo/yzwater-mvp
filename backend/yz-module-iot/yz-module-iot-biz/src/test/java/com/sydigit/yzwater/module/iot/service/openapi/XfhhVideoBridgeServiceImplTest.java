package com.sydigit.yzwater.module.iot.service.openapi;

import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.sydigit.yzwater.module.config.video.YzVideoHkProperties;
import com.sydigit.yzwater.module.iot.dal.mysql.openapi.XfhhVideoCameraMysqlMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.openapi.XfhhVideoEventMysqlMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.openapi.XfhhVideoRegionMysqlMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class XfhhVideoBridgeServiceImplTest {

    @Mock
    private XfhhVideoRegionMysqlMapper videoRegionMapper;
    @Mock
    private XfhhVideoCameraMysqlMapper videoCameraMapper;
    @Mock
    private XfhhVideoEventMysqlMapper videoEventMapper;

    @Test
    void syncRegions_shouldThrowWhenConfigIncomplete() {
        ArtemisConfig artemisConfig = new ArtemisConfig();
        artemisConfig.setHost("");
        artemisConfig.setAppKey("");
        artemisConfig.setAppSecret("");
        YzVideoHkProperties properties = new YzVideoHkProperties();
        properties.setEnabled(true);
        XfhhVideoBridgeServiceImpl service = new XfhhVideoBridgeServiceImpl(
                artemisConfig, properties, videoRegionMapper, videoCameraMapper, videoEventMapper);

        IllegalStateException exception = assertThrows(IllegalStateException.class, service::syncRegions);

        assertEquals("海康参数未配置完整，请检查配置 yz.video.hk.host/app-key/app-secret", exception.getMessage());
        verifyNoInteractions(videoRegionMapper, videoCameraMapper, videoEventMapper);
    }
}
