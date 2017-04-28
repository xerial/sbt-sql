@(start:Long, end:Long)
select * from sample_datasets.nasdaq
where time between ${start} and ${end}

