select
    true p_boolean,
    cast(1 as TINYINT) p_tinyint,
    cast(1 as SMALLINT) p_smallint,
    cast(1 as INTEGER) p_integer,
    cast(1 as BIGINT) p_bigint,
    cast(1 as UTINYINT) p_utinyint,
    cast(1 as USMALLINT) p_usmallint,
    cast(1 as UINTEGER) p_uinteger,
    cast(1 as UBIGINT) p_ubigint,
    cast(1 as HUGEINT) p_hugeint,
    cast(1.0 as FLOAT) p_float,
    cast(1.0 as DOUBLE) p_double,
    cast(1 as DECIMAL) p_decimal,
    cast('a' as VARCHAR) p_varchar,
    'A'::BLOB p_blob,
    'A'::BYTEA p_bloba,
    time '12:34:56' p_time,
    date '1992-09-20' p_date,
    timestamp '1992-09-20 12:34:56' p_timestamp,
    timestamp '1992-09-20 12:34:56.789' p_timestamp_ms,
    timestamp '1992-09-20 12:34:56.789123456' p_timestamp_ns,
    timestamp '1992-09-20 12:34:56' p_timestamp_s,
    TIMESTAMP '1992-09-20 12:34:56.789+01:00' p_timestamp_with_timezone,
    null p_null,
    '101010'::BIT p_bit,
    interval 1 year p_interval,
    [1, 2, 3] p_int_list,
    ['a', 'b'] p_varchar_list,
    [true, false] p_boolean_list,
    -- STRUCT
    -- ENUM
    uuid() p_uuid,
   '{"duck":42}'::JSON p_json,
    map {'k1':1, 'k2':3} p_varchar_int_map,
    -- UNKNOWN
    -- UNION
    cast(1 as INT16) p_int16,
    cast(1 as INT32) p_int32,
    cast(1 as INT64) p_int64,
    cast(1 as INT128) p_int128,
    1.0 p_real,
    cast(1.0 as FLOAT4) p_float4,
    cast(1.0 as FLOAT8) p_float8