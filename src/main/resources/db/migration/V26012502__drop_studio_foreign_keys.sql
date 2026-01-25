-- 1. Studio 관련 외래키 제거
-- Rooms
ALTER TABLE rooms
    DROP CONSTRAINT IF EXISTS fk_rooms_on_studio;

-- Studio Images
ALTER TABLE studio_images
    DROP CONSTRAINT IF EXISTS fk_studio_images_on_studio;

-- Studio Options (Studio 및 Option과의 관계 모두 제거)
ALTER TABLE studio_options
    DROP CONSTRAINT IF EXISTS fk_studio_options_on_studio;
ALTER TABLE studio_options
    DROP CONSTRAINT IF EXISTS fk_studio_options_on_option;

-- Studio Forbidden Instruments (Studio 및 Instrument과의 관계 모두 제거)
ALTER TABLE studio_forbidden_instruments
    DROP CONSTRAINT IF EXISTS fk_studio_forbidden_instruments_studio;
ALTER TABLE studio_forbidden_instruments
    DROP CONSTRAINT IF EXISTS fk_studio_forbidden_instruments_instrument;

-- Studio Prices
ALTER TABLE studio_prices
    DROP CONSTRAINT IF EXISTS fk_studio_prices_on_studio;

-- Studio Building Info
ALTER TABLE studio_building_info
    DROP CONSTRAINT IF EXISTS fk_studio_building_info_on_studio;

-- Studio View Logs
ALTER TABLE studio_view_logs
    DROP CONSTRAINT IF EXISTS fk_studio_view_logs_on_studio;

-- Studios (Owner와의 관계 제거)
ALTER TABLE studios
    DROP CONSTRAINT IF EXISTS fk_studios_on_owner;


-- 2. Subway 관련 외래키 제거
-- Subway Stations Nearby Studios (Studio 및 Subway Station과의 관계 모두 제거)
ALTER TABLE subway_stations_nearby_studios
    DROP CONSTRAINT IF EXISTS fk_subway_stations_nearby_studios_on_studio;
ALTER TABLE subway_stations_nearby_studios
    DROP CONSTRAINT IF EXISTS fk_subway_stations_nearby_studios_on_subway_station;

-- Subway Station Lines (Subway Station 및 Line과의 관계 모두 제거)
ALTER TABLE subway_station_lines
    DROP CONSTRAINT IF EXISTS fk_subway_station_lines_on_subway_station;
ALTER TABLE subway_station_lines
    DROP CONSTRAINT IF EXISTS fk_subway_station_lines_on_subway_line;
