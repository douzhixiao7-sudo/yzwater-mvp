package com.sydigit.yzwater.module.iot.service.statistics;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringGateSummaryRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusListRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringGateDetailRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringPressValueRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringRuntimeStatRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStationHostStatusRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStreamWaterListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStreamWaterRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.service.device.property.IotDevicePropertyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IotScreenStatisticsServiceImplTest {

    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Shanghai");

    @Mock
    private DictDataCommonApi dictDataCommonApi;
    @Mock
    private IotDeviceMapper deviceMapper;
    @Mock
    private IotDevicePropertyService devicePropertyService;

    @InjectMocks
    private com.sydigit.yzwater.module.iot.service.statistics.IotScreenStatisticsServiceImpl service;

    @Test
    void getStreamWaterList_shouldKeepExactFiveMinuteBoundary() {
        IotDeviceDO device = new IotDeviceDO();
        device.setId(1001L);
        device.setStationId("320902102");
        device.setDeviceName("device-water-level-01");

        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("320902102");
        station.setLabel("陈集镇");

        IotDevicePropertyRespVO history = new IotDevicePropertyRespVO();
        history.setValue("2.15");
        history.setUpdateTime(toEpochMilli(LocalDateTime.of(2026, 4, 10, 10, 15, 6)));

        when(deviceMapper.selectListByStationAndTypeAndNames(eq("320902102"), eq(6), isNull()))
                .thenReturn(List.of(device));
        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(devicePropertyService.getHistoryDevicePropertyList(any())).thenReturn(List.of(history));

        IotScreenStreamWaterListReqVO reqVO = new IotScreenStreamWaterListReqVO();
        reqVO.setStationId("320902102");

        List<IotScreenStreamWaterRespVO> result = service.getStreamWaterList(reqVO);

        assertEquals(1, result.size());
        assertEquals("2026-04-10 10:15:00", result.get(0).getTime());
    }

    @Test
    void getStreamWaterList_shouldRoundMinuteDownToNearestFiveMinuteBoundary() {
        IotDeviceDO device = new IotDeviceDO();
        device.setId(1001L);
        device.setStationId("320902102");
        device.setDeviceName("device-water-level-01");

        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("320902102");
        station.setLabel("陈集镇");

        IotDevicePropertyRespVO history = new IotDevicePropertyRespVO();
        history.setValue("2.15");
        history.setUpdateTime(toEpochMilli(LocalDateTime.of(2026, 4, 14, 13, 52, 18)));

        when(deviceMapper.selectListByStationAndTypeAndNames(eq("320902102"), eq(6), isNull()))
                .thenReturn(List.of(device));
        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(devicePropertyService.getHistoryDevicePropertyList(any())).thenReturn(List.of(history));

        IotScreenStreamWaterListReqVO reqVO = new IotScreenStreamWaterListReqVO();
        reqVO.setStationId("320902102");

        List<IotScreenStreamWaterRespVO> result = service.getStreamWaterList(reqVO);

        assertEquals(1, result.size());
        assertEquals("2026-04-14 13:50:00", result.get(0).getTime());
    }

    @Test
    void getStreamWaterList_shouldReturnItemsInAscendingTimeOrder() {
        IotDeviceDO device = new IotDeviceDO();
        device.setId(1001L);
        device.setStationId("320902102");
        device.setDeviceName("device-water-level-01");

        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("320902102");
        station.setLabel("陈集镇");

        IotDevicePropertyRespVO laterHistory = new IotDevicePropertyRespVO();
        laterHistory.setValue("2.30");
        laterHistory.setUpdateTime(toEpochMilli(LocalDateTime.of(2026, 4, 17, 10, 20, 8)));

        IotDevicePropertyRespVO earlierHistory = new IotDevicePropertyRespVO();
        earlierHistory.setValue("2.15");
        earlierHistory.setUpdateTime(toEpochMilli(LocalDateTime.of(2026, 4, 17, 10, 15, 6)));

        when(deviceMapper.selectListByStationAndTypeAndNames(eq("320902102"), eq(6), isNull()))
                .thenReturn(List.of(device));
        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(devicePropertyService.getHistoryDevicePropertyList(any()))
                .thenReturn(List.of(laterHistory, earlierHistory));

        IotScreenStreamWaterListReqVO reqVO = new IotScreenStreamWaterListReqVO();
        reqVO.setStationId("320902102");

        List<IotScreenStreamWaterRespVO> result = service.getStreamWaterList(reqVO);

        assertEquals(2, result.size());
        assertEquals("2026-04-17 10:15:00", result.get(0).getTime());
        assertEquals("2026-04-17 10:20:00", result.get(1).getTime());
    }

    @Test
    void getStreamWaterList_shouldUseSwPvForShiErWeiStationAndKeepWaterLevelField() {
        IotDeviceDO device = new IotDeviceDO();
        device.setId(3001L);
        device.setStationId("0");
        device.setDeviceName("device-water-level-00");

        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("0");
        station.setLabel("十二圩");

        IotDevicePropertyRespVO history = new IotDevicePropertyRespVO();
        history.setValue("1.236");
        history.setUpdateTime(toEpochMilli(LocalDateTime.of(2026, 4, 21, 9, 16, 9)));

        when(deviceMapper.selectListByStationAndTypeAndNames(eq("0"), eq(6), isNull()))
                .thenReturn(List.of(device));
        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(devicePropertyService.getHistoryDevicePropertyList(any())).thenReturn(List.of(history));

        IotScreenStreamWaterListReqVO reqVO = new IotScreenStreamWaterListReqVO();
        reqVO.setStationId("0");

        List<IotScreenStreamWaterRespVO> result = service.getStreamWaterList(reqVO);

        assertEquals(1, result.size());
        assertEquals("2026-04-21 09:15:00", result.get(0).getTime());
        assertEquals("1.24", String.valueOf(result.get(0).getWaterLevel()));
        verify(devicePropertyService).getHistoryDevicePropertyList(argThat(historyReqVO ->
                historyReqVO != null
                        && Long.valueOf(3001L).equals(historyReqVO.getDeviceId())
                        && "sw_pv".equals(historyReqVO.getIdentifier())));
    }

    @Test
    void getStreamWaterList_shouldUseSwPvForLegacyShiErWeiStationKey() {
        IotDeviceDO device = new IotDeviceDO();
        device.setId(3002L);
        device.setStationId("3");
        device.setDeviceName("device-water-level-03");

        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("3");
        station.setLabel("十二圩-旧键");

        IotDevicePropertyRespVO history = new IotDevicePropertyRespVO();
        history.setValue("2.5");
        history.setUpdateTime(toEpochMilli(LocalDateTime.of(2026, 4, 21, 9, 16, 9)));

        when(deviceMapper.selectListByStationAndTypeAndNames(eq("3"), eq(6), isNull()))
                .thenReturn(List.of(device));
        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(devicePropertyService.getHistoryDevicePropertyList(any())).thenReturn(List.of(history));

        IotScreenStreamWaterListReqVO reqVO = new IotScreenStreamWaterListReqVO();
        reqVO.setStationId("3");

        List<IotScreenStreamWaterRespVO> result = service.getStreamWaterList(reqVO);

        assertEquals(1, result.size());
        verify(devicePropertyService).getHistoryDevicePropertyList(argThat(historyReqVO ->
                historyReqVO != null
                        && Long.valueOf(3002L).equals(historyReqVO.getDeviceId())
                        && "sw_pv".equals(historyReqVO.getIdentifier())));
    }

    @Test
    void getStationHostStatusList_shouldUseRunIdentifierForShiErWeiStation() {
        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("0");
        station.setLabel("十二圩");
        station.setStatus(0);

        IotDeviceDO runningDevice = new IotDeviceDO();
        runningDevice.setId(3101L);
        runningDevice.setStationId("0");
        runningDevice.setDeviceName("host-running");

        IotDeviceDO stoppedDevice = new IotDeviceDO();
        stoppedDevice.setId(3102L);
        stoppedDevice.setStationId("0");
        stoppedDevice.setDeviceName("host-stopped");

        IotDevicePropertyDO runningProperty = new IotDevicePropertyDO();
        runningProperty.setValue("1");

        IotDevicePropertyDO stoppedProperty = new IotDevicePropertyDO();
        stoppedProperty.setValue("0");

        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(deviceMapper.selectListByStationAndTypeAndNames(eq("0"), eq(3), isNull()))
                .thenReturn(List.of(runningDevice, stoppedDevice));
        when(devicePropertyService.getLatestDeviceProperties(3101L))
                .thenReturn(Map.of("run", runningProperty));
        when(devicePropertyService.getLatestDeviceProperties(3102L))
                .thenReturn(Map.of("run", stoppedProperty));

        List<IotScreenStationHostStatusRespVO> result = service.getStationHostStatusList();

        assertEquals(1, result.size());
        assertEquals("0", result.get(0).getStationId());
        assertEquals(1L, result.get(0).getRunningCount());
        assertEquals(1L, result.get(0).getStopCount());
    }

    @Test
    void getEngineeringDeviceStatusList_shouldReturnPForStationThreeMainUnit() {
        IotDeviceDO mainUnit = new IotDeviceDO();
        mainUnit.setId(3201L);
        mainUnit.setStationId("3");
        mainUnit.setDeviceName("3#机组");

        IotDevicePropertyDO activePowerProperty = new IotDevicePropertyDO();
        activePowerProperty.setValue("18.6");

        when(deviceMapper.selectListByStationAndTypeAndNames(eq("3"), eq(3), isNull()))
                .thenReturn(List.of(mainUnit));
        when(devicePropertyService.getLatestDeviceProperties(3201L))
                .thenReturn(Map.of("p", activePowerProperty));

        IotScreenEngineeringDeviceStatusListReqVO reqVO = new IotScreenEngineeringDeviceStatusListReqVO();
        reqVO.setStationId("3");
        reqVO.setDeviceType(3);

        List<IotScreenEngineeringDeviceStatusListRespVO> result = service.getEngineeringDeviceStatusList(reqVO);

        assertEquals(1, result.size());
        assertEquals("18.6", String.valueOf(result.get(0).getP()));
        assertEquals("3#机组", result.get(0).getDeviceName());
    }

    @Test
    void getEngineeringGateStatusSummary_shouldUseZmqbjSpForStationThree() {
        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("3");
        station.setLabel("三号站");
        station.setStatus(0);

        IotDeviceDO gateOne = new IotDeviceDO();
        gateOne.setId(7001L);
        gateOne.setStationId("3");
        gateOne.setDeviceName("gate-open");

        IotDeviceDO gateTwo = new IotDeviceDO();
        gateTwo.setId(7002L);
        gateTwo.setStationId("3");
        gateTwo.setDeviceName("gate-close");

        IotDevicePropertyDO gateOpenProperty = new IotDevicePropertyDO();
        gateOpenProperty.setValue("1");

        IotDevicePropertyDO gateCloseProperty = new IotDevicePropertyDO();
        gateCloseProperty.setValue("1");

        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(deviceMapper.selectListByStationAndTypeAndNames(eq("3"), eq(7), isNull()))
                .thenReturn(List.of(gateOne, gateTwo));
        when(devicePropertyService.getLatestDeviceProperties(7001L))
                .thenReturn(Map.of("zmqbj_sp1", gateOpenProperty));
        when(devicePropertyService.getLatestDeviceProperties(7002L))
                .thenReturn(Map.of("zmqbj_sp2", gateCloseProperty));

        IotScreenEngineeringGateSummaryRespVO result = service.getEngineeringGateStatusSummary("3");

        assertEquals("3", result.getStationId());
        assertEquals(2L, result.getGateTotalCount());
        assertEquals(1L, result.getGateOpenAllCount());
        assertEquals(1L, result.getGateCloseAllCount());
    }

    @Test
    void getEngineeringGateStatusList_shouldUseZmqbjFieldsForStationThree() {
        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("3");
        station.setLabel("三号站");
        station.setStatus(0);

        IotDeviceDO gateDevice = new IotDeviceDO();
        gateDevice.setId(7101L);
        gateDevice.setStationId("3");
        gateDevice.setDeviceName("三号站闸门一");

        IotDevicePropertyDO openAllProperty = new IotDevicePropertyDO();
        openAllProperty.setValue("1");

        IotDevicePropertyDO closeAllProperty = new IotDevicePropertyDO();
        closeAllProperty.setValue("0");

        IotDevicePropertyDO gateUpProperty = new IotDevicePropertyDO();
        gateUpProperty.setValue("1");

        IotDevicePropertyDO gateDownProperty = new IotDevicePropertyDO();
        gateDownProperty.setValue("0");

        IotDevicePropertyDO gateLoadWeightLProperty = new IotDevicePropertyDO();
        gateLoadWeightLProperty.setValue("11.1");

        IotDevicePropertyDO gateLoadWeightRProperty = new IotDevicePropertyDO();
        gateLoadWeightRProperty.setValue("22.2");

        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(deviceMapper.selectListByStationAndTypeAndNames(eq("3"), eq(7), isNull()))
                .thenReturn(List.of(gateDevice));
        when(devicePropertyService.getLatestDeviceProperties(7101L))
                .thenReturn(Map.of(
                        "zmqbj_sp1", openAllProperty,
                        "zmqbj_sp2", closeAllProperty,
                        "zmqbj_rise", gateUpProperty,
                        "zmqbj_drop", gateDownProperty,
                        "zmqbj_zfhfk_av", gateLoadWeightLProperty,
                        "zmqbj_yfhfk_av", gateLoadWeightRProperty));

        List<IotScreenEngineeringGateDetailRespVO> result = service.getEngineeringGateStatusList("3");

        assertEquals(1, result.size());
        assertEquals("3", result.get(0).getStationId());
        assertEquals("三号站", result.get(0).getStationName());
        assertEquals(7101L, result.get(0).getDeviceId());
        assertEquals("三号站闸门一", result.get(0).getDeviceName());
        assertEquals("1", String.valueOf(result.get(0).getIsGateOpenAll()));
        assertEquals("0", String.valueOf(result.get(0).getIsGateCloseAll()));
        assertEquals("1", String.valueOf(result.get(0).getIsGateUp()));
        assertEquals("0", String.valueOf(result.get(0).getIsGateDown()));
        assertEquals("11.1", String.valueOf(result.get(0).getIsGateLoadWeightL()));
        assertEquals("22.2", String.valueOf(result.get(0).getIsGateLoadWeightR()));
    }

    @Test
    void getEngineeringRuntimeStatSummary_shouldUseYearFieldsForStationThree() {
        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("3");
        station.setLabel("三号站");
        station.setStatus(0);

        IotDeviceDO hostDevice = new IotDeviceDO();
        hostDevice.setId(30001L);
        hostDevice.setStationId("3");
        hostDevice.setDeviceName("三号主机");

        IotDevicePropertyDO startCountProperty = new IotDevicePropertyDO();
        startCountProperty.setValue("12");

        IotDevicePropertyDO totalStartDurationProperty = new IotDevicePropertyDO();
        totalStartDurationProperty.setValue("345.678");

        IotDevicePropertyDO startDurationProperty = new IotDevicePropertyDO();
        startDurationProperty.setValue("9.876");
        startDurationProperty.setUpdateTime(LocalDateTime.of(2026, 3, 26, 14, 20, 0));

        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(deviceMapper.selectListByStationAndTypeAndNames(eq("3"), eq(3), isNull()))
                .thenReturn(List.of(hostDevice));
        when(devicePropertyService.getLatestDeviceProperties(30001L))
                .thenReturn(Map.of(
                        "year_ci", startCountProperty,
                        "year_lj", totalStartDurationProperty,
                        "lj", startDurationProperty));

        List<IotScreenEngineeringRuntimeStatRespVO> result = service.getEngineeringRuntimeStatSummary("3");

        assertEquals(1, result.size());
        assertEquals("3", result.get(0).getStationId());
        assertEquals(12L, result.get(0).getStartCount());
        assertEquals("345.68", result.get(0).getTotalStartDuration().toPlainString());
        assertEquals("9.88", result.get(0).getStartDuration().toPlainString());
        assertEquals("2026-03-26 14:20:00", result.get(0).getStartDurationCollectTime());
    }

    @Test
    void getEngineeringPressValueList_shouldUseYypForStationThree() {
        IotDeviceDO pressDevice = new IotDeviceDO();
        pressDevice.setId(9001L);
        pressDevice.setStationId("3");
        pressDevice.setDeviceName("三号站压力设备");

        IotDevicePropertyDO yypProperty = new IotDevicePropertyDO();
        yypProperty.setValue("123.456");

        when(deviceMapper.selectListByStationAndTypeAndNames(eq("3"), eq(9), isNull()))
                .thenReturn(List.of(pressDevice));
        when(devicePropertyService.getLatestDeviceProperties(9001L))
                .thenReturn(Map.of("yyp", yypProperty));

        List<IotScreenEngineeringPressValueRespVO> result = service.getEngineeringPressValueList("3");

        assertEquals(1, result.size());
        assertEquals(9001L, result.get(0).getDeviceId());
        assertEquals("三号站压力设备", result.get(0).getDeviceName());
        assertEquals("123.46", String.valueOf(result.get(0).getPressValue()));
    }

    @Test
    void getEngineeringDeviceStatusSummary_shouldUseRunForStationThree() {
        DictDataRespDTO station = new DictDataRespDTO();
        station.setValue("3");
        station.setLabel("三号站");
        station.setStatus(0);

        IotDeviceDO runningDevice = new IotDeviceDO();
        runningDevice.setId(3201L);
        runningDevice.setStationId("3");
        runningDevice.setDeviceName("三号主机一");

        IotDeviceDO closedDevice = new IotDeviceDO();
        closedDevice.setId(3202L);
        closedDevice.setStationId("3");
        closedDevice.setDeviceName("三号主机二");

        IotDevicePropertyDO runningProperty = new IotDevicePropertyDO();
        runningProperty.setValue("1");

        IotDevicePropertyDO closedProperty = new IotDevicePropertyDO();
        closedProperty.setValue("0");

        when(dictDataCommonApi.getDictDataList(any())).thenReturn(List.of(station));
        when(deviceMapper.selectListByStationAndTypeAndNames(eq("3"), eq(3), isNull()))
                .thenReturn(List.of(runningDevice, closedDevice));
        when(devicePropertyService.getLatestDeviceProperties(3201L))
                .thenReturn(Map.of("run", runningProperty));
        when(devicePropertyService.getLatestDeviceProperties(3202L))
                .thenReturn(Map.of("run", closedProperty));

        IotScreenEngineeringDeviceStatusRespVO result = service.getEngineeringDeviceStatusSummary("3");

        assertEquals("3", result.getStationId());
        assertEquals(1L, result.getRunningCount());
        assertEquals(1L, result.getClosedCount());
        assertEquals(2L, result.getTotalCount());
    }

    private Long toEpochMilli(LocalDateTime time) {
        return time.atZone(BUSINESS_TIME_ZONE).toInstant().toEpochMilli();
    }
}
