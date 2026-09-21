-- 风险隐患点地图字段（一次性执行，可重复执行）
-- 依赖：PostgreSQL + PostGIS

CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE yz_fx_task
    ADD COLUMN IF NOT EXISTS counter_measures TEXT;

ALTER TABLE yz_fx_task
    ADD COLUMN IF NOT EXISTS geom geometry(Geometry, 4490);

ALTER TABLE yz_fx_task
    ADD COLUMN IF NOT EXISTS geom_meta TEXT;

COMMENT ON COLUMN yz_fx_task.counter_measures IS '应对措施';
COMMENT ON COLUMN yz_fx_task.geom IS '地图几何（点/线，SRID=4490）';
COMMENT ON COLUMN yz_fx_task.geom_meta IS '地图扩展元数据 JSON：vertexMarkers/vertexLabels/riskSegments/riskSegmentNames/riskSegmentCenters';
