-- 坑塘台账字段：中心点坐标、备注、资源类型
ALTER TABLE yz_water_pond ADD COLUMN IF NOT EXISTS center_lon numeric(12, 8);
ALTER TABLE yz_water_pond ADD COLUMN IF NOT EXISTS center_lat numeric(12, 8);
ALTER TABLE yz_water_pond ADD COLUMN IF NOT EXISTS remark varchar(500);
ALTER TABLE yz_water_pond ADD COLUMN IF NOT EXISTS resource_type varchar(64);

COMMENT ON COLUMN yz_water_pond.center_lon IS '中心点经度（WGS84）';
COMMENT ON COLUMN yz_water_pond.center_lat IS '中心点纬度（WGS84）';
COMMENT ON COLUMN yz_water_pond.remark IS '备注';
COMMENT ON COLUMN yz_water_pond.resource_type IS '资源类型（如坑塘水面）';

-- 历史数据：ZYLX 曾写入 land_type，回填 resource_type
UPDATE yz_water_pond
SET resource_type = land_type
WHERE (resource_type IS NULL OR btrim(resource_type) = '')
  AND land_type IS NOT NULL
  AND btrim(land_type) <> '';

-- 已有几何数据回填中心点
UPDATE yz_water_pond
SET center_lon = ST_X(ST_Centroid(geom)),
    center_lat = ST_Y(ST_Centroid(geom))
WHERE geom IS NOT NULL
  AND (center_lon IS NULL OR center_lat IS NULL);
