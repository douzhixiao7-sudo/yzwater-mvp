ALTER TABLE yz_fx_task
    ADD COLUMN IF NOT EXISTS geom_meta TEXT;

COMMENT ON COLUMN yz_fx_task.geom_meta IS '地图扩展元数据 JSON：vertexMarkers/vertexLabels/riskSegments/riskSegmentNames/riskSegmentCenters';
