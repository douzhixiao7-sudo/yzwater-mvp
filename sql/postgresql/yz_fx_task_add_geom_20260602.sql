ALTER TABLE yz_fx_task
    ADD COLUMN IF NOT EXISTS geom geometry(Geometry, 4490);

COMMENT ON COLUMN yz_fx_task.geom IS '地图几何（点/线，SRID=4490）';
