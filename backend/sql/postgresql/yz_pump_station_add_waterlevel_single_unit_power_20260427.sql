ALTER TABLE yz_pump_station
    ADD COLUMN IF NOT EXISTS normal_water_level numeric(10, 2),
    ADD COLUMN IF NOT EXISTS pre_drop_water_level numeric(10, 2),
    ADD COLUMN IF NOT EXISTS minimum_operating_water_level numeric(10, 2),
    ADD COLUMN IF NOT EXISTS single_unit_power numeric(10, 2);

COMMENT ON COLUMN yz_pump_station.normal_water_level IS '常水位(m)';
COMMENT ON COLUMN yz_pump_station.pre_drop_water_level IS '防办预降水位(m)';
COMMENT ON COLUMN yz_pump_station.minimum_operating_water_level IS '最低运行水位(m)';
COMMENT ON COLUMN yz_pump_station.single_unit_power IS '单机组功率(KW)';
