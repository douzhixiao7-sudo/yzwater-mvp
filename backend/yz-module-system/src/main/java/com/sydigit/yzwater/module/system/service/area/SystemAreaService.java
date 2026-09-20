package com.sydigit.yzwater.module.system.service.area;

import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.service.area.dto.SystemAreaGeoJsonImportResult;
import com.sydigit.yzwater.module.system.service.area.dto.SystemAreaNode;

import java.util.List;

/**
 * 行政区划 Service 接口
 */
public interface SystemAreaService {

    /**
     * 获得指定根节点下的子树（返回根节点的 children）
     *
     * @param rootId 根节点编码
     * @return 子节点树
     */
    List<SystemAreaNode> getAreaTreeChildren(Long rootId);

    /**
     * 从框架内置地区数据（area.csv）刷新到 system_area 表
     *
     * @return 刷新写入的数据条数
     */
    int refreshFromBuiltin();

    /**
     * 导入行政区划 GeoJSON（按 properties.name 匹配名称，写入 gemo 字段）
     *
     * @param geoJsonBytes GeoJSON 文件内容（UTF-8）
     * @return 导入结果
     */
    SystemAreaGeoJsonImportResult importGeoJson(byte[] geoJsonBytes);

    /**
     * 导入行政区划 GeoJSON（按 features.properties.adcode 匹配 system_area.id，写入 gemo 字段）
     *
     * <p>说明：仅对已存在的行政区划写入/更新几何，不新增行政区划。</p>
     *
     * @param geoJsonBytes GeoJSON 文件内容（UTF-8）
     * @return 导入结果
     */
    SystemAreaGeoJsonImportResult importGeoJsonByAdcode(byte[] geoJsonBytes);

    /**
     * 导入行政区划子节点 GeoJSON（按 properties.FROMENTIID 匹配父节点 system_area.id，新增/更新子节点并写入 gemo）
     *
     * <p>字段映射：</p>
     * <ul>
     *     <li>父节点：properties.FROMENTIID -> system_area.id</li>
     *     <li>子节点：properties.ENTIID -> id；properties.NAME -> name；type 固定为 6</li>
     *     <li>几何：优先使用 feature.geometry；若缺失则尝试 properties.coordinates 作为 Point 坐标</li>
     * </ul>
     *
     * @param geoJsonBytes GeoJSON 文件内容（UTF-8）
     * @return 导入结果
     */
    SystemAreaGeoJsonImportResult importGeoJsonChildrenByFromEntiId(byte[] geoJsonBytes);

    /**
     * 创建行政区划
     *
     * @param area 行政区划
     * @return 主键
     */
    Long create(SystemAreaDO area);

    /**
     * 更新行政区划
     *
     * @param area 行政区划
     */
    void update(SystemAreaDO area);

    /**
     * 逻辑删除行政区划
     *
     * @param id 行政区划编码
     */
    void delete(Long id);

    /**
     * 获取行政区划（包含 gemo 的 WKT）
     *
     * @param id 行政区划编码
     * @return 行政区划
     */
    SystemAreaDO get(Long id);
}
