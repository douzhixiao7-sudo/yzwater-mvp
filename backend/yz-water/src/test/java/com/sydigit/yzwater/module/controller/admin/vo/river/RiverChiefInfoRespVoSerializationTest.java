package com.sydigit.yzwater.module.controller.admin.vo.river;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RiverChiefInfoRespVoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldSerializePageIdAsString() throws Exception {
        RiverChiefInfoPageRespVO vo = new RiverChiefInfoPageRespVO();
        vo.setId(1912876123987612345L);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(vo));

        assertTrue(json.get("id").isTextual(), "河长信息分页响应的 id 应序列化为字符串");
    }

    @Test
    void shouldSerializeDetailAndFacilityReferenceIdAsString() throws Exception {
        RiverChiefInfoFacilityRespVO facility = new RiverChiefInfoFacilityRespVO();
        facility.setReferenceId(1912876123987612345L);

        RiverChiefInfoDetailRespVO vo = new RiverChiefInfoDetailRespVO();
        vo.setId(1912876123987612345L);
        vo.setFacilities(List.of(facility));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(vo));

        assertTrue(json.get("id").isTextual(), "河长信息详情响应的 id 应序列化为字符串");
        assertTrue(json.get("facilities").get(0).get("referenceId").isTextual(),
                "河长信息详情中的关联设施 referenceId 应序列化为字符串");
    }
}
