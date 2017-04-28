@(start:Long=0, end:Long, timeZone:String="UTC")
select *
from sample_datasets.www_access
where TD_TIME_RANGE(time, ${start}, ${end}, '${timeZone}')
