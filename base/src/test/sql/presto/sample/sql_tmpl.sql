@(start:String="2019-01-01", end:String="2019-01-02", cond:sql="AND time > 0")
select * from sample_datasets.nasdaq
where TD_TIME_RANGE(time, '${start}', '${end}')
${cond}
