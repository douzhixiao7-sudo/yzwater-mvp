ALTER TABLE yz_fx_task
    ADD COLUMN IF NOT EXISTS counter_measures varchar(2048);

COMMENT ON COLUMN yz_fx_task.counter_measures IS '应对措施';
