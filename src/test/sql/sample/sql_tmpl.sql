select * from sample_datasets.nasdaq
where TD_TIME_RANGE(time, '${start:String}', '${end:String}')
