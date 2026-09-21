package com.sydigit.yzwater.module.iot.dal.mysql.device;

import cn.hutool.core.util.ObjectUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Nullable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IoT 设备 Mapper
 *
 * @author lijun
 */
@Mapper
public interface IotDeviceMapper extends BaseMapperX<IotDeviceDO> {

    default PageResult<IotDeviceDO> selectPage(IotDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceDO>()
                .likeIfPresent(IotDeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IotDeviceDO::getStationId, reqVO.getStationId())
                .eqIfPresent(IotDeviceDO::getProductId, reqVO.getProductId())
                .eqIfPresent(IotDeviceDO::getDeviceType, reqVO.getDeviceType())
                .likeIfPresent(IotDeviceDO::getNickname, reqVO.getNickname())
                .likeIfPresent(IotDeviceDO::getSerialNumber, reqVO.getSerialNumber())
                .eqIfPresent(IotDeviceDO::getUseStatus, reqVO.getUseStatus())
                .eqIfPresent(IotDeviceDO::getState, reqVO.getStatus())
                .eqIfPresent(IotDeviceDO::getGatewayId, reqVO.getGatewayId())
                .apply(
                        ObjectUtil.isNotNull(reqVO.getGroupId()),
                        "{0} = ANY (string_to_array(group_ids, ',')::int8[])",
                        reqVO.getGroupId()
                )
                .orderByDesc(IotDeviceDO::getUpdateTime)
                .orderByDesc(IotDeviceDO::getId));
    }

    default IotDeviceDO selectByDeviceName(String deviceName) {
        return selectOne(IotDeviceDO::getDeviceName, deviceName);
    }

    default IotDeviceDO selectByProductKeyAndDeviceName(String productKey, String deviceName) {
        return selectOne(IotDeviceDO::getProductKey, productKey,
                IotDeviceDO::getDeviceName, deviceName);
    }

    default IotDeviceDO selectByProductIdAndDeviceName(Long productId, String deviceName) {
        return selectOne(new LambdaQueryWrapperX<IotDeviceDO>()
                .eqIfPresent(IotDeviceDO::getProductId, productId)
                .eqIfPresent(IotDeviceDO::getDeviceName, deviceName));
    }

    default long selectCountByGatewayId(Long id) {
        return selectCount(IotDeviceDO::getGatewayId, id);
    }

    default Long selectCountByProductId(Long productId) {
        return selectCount(IotDeviceDO::getProductId, productId);
    }

    default List<IotDeviceDO> selectListByCondition(@Nullable Integer deviceType,
                                                    @Nullable Long productId) {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eqIfPresent(IotDeviceDO::getDeviceType, deviceType)
                .eqIfPresent(IotDeviceDO::getProductId, productId));
    }

    default List<IotDeviceDO> selectListByStationAndTypeAndNames(@Nullable String stationId,
                                                                  @Nullable Integer deviceType,
                                                                  @Nullable Collection<String> deviceNames) {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eqIfPresent(IotDeviceDO::getStationId, stationId)
                .eqIfPresent(IotDeviceDO::getDeviceType, deviceType)
                .inIfPresent(IotDeviceDO::getDeviceName, deviceNames)
                .orderByDesc(IotDeviceDO::getUpdateTime)
                .orderByDesc(IotDeviceDO::getId));
    }

    default List<IotDeviceDO> selectListByStationAndGroupId(@Nullable String stationId,
                                                             @Nullable Long groupId) {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eqIfPresent(IotDeviceDO::getStationId, stationId)
                .apply(
                        ObjectUtil.isNotNull(groupId),
                        "{0} = ANY (string_to_array(group_ids, ',')::int8[])",
                        groupId
                )
                .orderByDesc(IotDeviceDO::getUpdateTime)
                .orderByDesc(IotDeviceDO::getId));
    }

    default List<IotDeviceDO> selectListByState(Integer state) {
        return selectList(IotDeviceDO::getState, state);
    }

    default List<IotDeviceDO> selectListByProductId(Long productId) {
        return selectList(IotDeviceDO::getProductId, productId);
    }

    default Long selectCountByGroupId(Long groupId) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDO>()
                .apply(
                        ObjectUtil.isNotNull(groupId),
                        "{0} = ANY (string_to_array(group_ids, ',')::int8[])",
                        groupId
                ));
    }


    default Long selectCountByCreateTime(@Nullable LocalDateTime createTime) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDO>()
                .geIfPresent(IotDeviceDO::getCreateTime, createTime));
    }

    default List<IotDeviceDO> selectByProductKeyAndDeviceNames(String productKey, Collection<String> deviceNames) {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getProductKey, productKey)
                .in(IotDeviceDO::getDeviceName, deviceNames));
    }

    default IotDeviceDO selectBySerialNumber(String serialNumber) {
        return selectOne(IotDeviceDO::getSerialNumber, serialNumber);
    }

    default IotDeviceDO selectByQrCode(String qrCode) {
        return selectOne(IotDeviceDO::getQrCode, qrCode);
    }

    /**
     * 查询二维码为空的设备
     *
     * @return 设备列表
     */
    default List<IotDeviceDO> selectListByQrCodeEmpty() {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .nested(wrapper -> wrapper.isNull(IotDeviceDO::getQrCode)
                        .or().eq(IotDeviceDO::getQrCode, "")));
    }

    /**
     * 查询指定前缀下的最大流水号
     *
     * @param prefix 编码前缀（设备类型编码）
     * @param prefixLength 前缀长度
     * @return 最大流水号（四位数字），不存在返回 null
     */
    @Select("""
            SELECT MAX(CAST(SUBSTRING(TRIM(serial_number) FROM (#{prefixLength} + 1)) AS INT))
            FROM iot_device
            WHERE serial_number IS NOT NULL
              AND LEFT(TRIM(serial_number), #{prefixLength}) = #{prefix}
              AND SUBSTRING(TRIM(serial_number) FROM (#{prefixLength} + 1)) ~ '^[0-9]+$'
            """)
    Integer selectMaxSerialNumberByPrefix(@Param("prefix") String prefix, @Param("prefixLength") int prefixLength);

    /**
     * 查询指定产品下的设备数量
     *
     * @return 产品编号 -> 设备数量的映射
     */
    default Map<Long, Integer> selectDeviceCountMapByProductId() {
        List<Map<String, Object>> result = selectMaps(
                new QueryWrapper<IotDeviceDO>()
                        .select("product_id", "COUNT(1) AS device_count")
                        .groupBy("product_id")
        );

        return result.stream().collect(Collectors.toMap(
                map -> ((Number) map.get("product_id")).longValue(),
                map -> ((Number) map.get("device_count")).intValue()
        ));
    }


    /**
     * 查询各个状态下的设备数量
     *
     * @return 设备状态 -> 设备数量的映射
     */
    default Map<Integer, Long> selectDeviceCountGroupByState() {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<IotDeviceDO>()
                .select("state", "COUNT(1) AS deviceCount")
                .groupBy("state"));
        return result.stream().collect(Collectors.toMap(
            map -> Integer.valueOf(map.get("state").toString()),
            map -> Long.valueOf(map.get("devicecount").toString())
        ));
    }

}
