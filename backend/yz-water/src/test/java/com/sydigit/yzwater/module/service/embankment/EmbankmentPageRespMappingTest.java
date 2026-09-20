package com.sydigit.yzwater.module.service.embankment;

import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentPageRespVO;
import com.sydigit.yzwater.module.dal.dataobject.embankment.YzEmbankmentDO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmbankmentPageRespMappingTest {

    @Test
    void shouldExposeStartAndEndPointOnPageResp() throws Exception {
        EmbankmentService service = new EmbankmentService(null, null, null, null, null);
        YzEmbankmentDO item = new YzEmbankmentDO();
        item.setStartPoint("西闸口");
        item.setEndPoint("东闸口");

        EmbankmentPageRespVO resp = invokeBuildPageResp(service, item);

        assertEquals("西闸口", resp.getStartPoint());
        assertEquals("东闸口", resp.getEndPoint());
    }

    private EmbankmentPageRespVO invokeBuildPageResp(EmbankmentService service, YzEmbankmentDO item) throws Exception {
        Method method = EmbankmentService.class.getDeclaredMethod("buildPageResp",
                YzEmbankmentDO.class, java.util.Map.class, java.util.Map.class, java.util.Map.class, java.util.Map.class);
        method.setAccessible(true);
        return (EmbankmentPageRespVO) method.invoke(service, item,
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }
}
