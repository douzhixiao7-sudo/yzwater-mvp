-- 坑塘几何空间索引：加速 MVT 瓦片 / bbox 查询
-- 若索引已存在可忽略报错后手动 DROP INDEX IF EXISTS 再执行

CREATE INDEX IF NOT EXISTS idx_yz_water_pond_geom
    ON yz_water_pond USING GIST (geom);

CREATE INDEX IF NOT EXISTS idx_yz_water_pond_center
    ON yz_water_pond (center_lon, center_lat)
    WHERE center_lon IS NOT NULL AND center_lat IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_yz_water_pond_village_code
    ON yz_water_pond (village_code)
    WHERE village_code IS NOT NULL AND btrim(village_code) <> '';
