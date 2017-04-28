@(start:String, end:String, cond:sql="AND time > 0")
select * from sample_datasets.nasdaq
where TD_TIME_RANGE(time, '${start}', '${end}')
${cond}
