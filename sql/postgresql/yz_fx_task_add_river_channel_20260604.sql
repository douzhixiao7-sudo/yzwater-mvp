ALTER TABLE yz_fx_task
    ADD COLUMN IF NOT EXISTS river_channel_id BIGINT,
    ADD COLUMN IF NOT EXISTS river_channel_name VARCHAR(256);

COMMENT ON COLUMN yz_fx_task.river_channel_id IS '所属河道 ID';
COMMENT ON COLUMN yz_fx_task.river_channel_name IS '所属河道名称（冗余展示）';
