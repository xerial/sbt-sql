select * from sample_datasets.nasdaq
where time between ${start:Long} and ${end:Long}

