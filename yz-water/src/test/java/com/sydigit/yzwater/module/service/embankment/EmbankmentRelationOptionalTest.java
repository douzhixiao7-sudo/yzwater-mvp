package com.sydigit.yzwater.module.service.embankment;

import com.sydigit.yzwater.module.dal.dataobject.embankment.YzEmbankmentDO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNull;

class EmbankmentRelationOptionalTest {

    @Test
    void shouldAllowEmptyRiverRelation() throws Exception {
        EmbankmentService service = new EmbankmentService(null, null, null, null, null);
        YzEmbankmentDO embankment = new YzEmbankmentDO();

        Method method = EmbankmentService.class.getDeclaredMethod("fillRiverRelation",
                YzEmbankmentDO.class, Long.class, Long.class);
        method.setAccessible(true);
        method.invoke(service, embankment, null, null);

        assertNull(embankment.getRiverChannelId());
        assertNull(embankment.getRiverSectionId());
    }
}
