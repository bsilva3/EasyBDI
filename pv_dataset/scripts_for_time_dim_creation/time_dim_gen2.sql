use pv_schema;


# time span
SET @d0 = "2010/07/1 00:00";
SET @d1 = "2013/06/30 23:30";

SET @date = date_sub(@d0, interval 30 minute);

# set up the time dimension table
DROP TABLE IF EXISTS time_dimension;
CREATE TABLE `time_dimension` (
  `complete_date` datetime 	 NOT NULL,
  `date` date NOT NULL,
  half_hour time NOT NULL,
  `year` 	 smallint DEFAULT NULL,
  `month` 	 smallint DEFAULT NULL,
  `day` 	 smallint DEFAULT NULL,
  `week` 	 smallint DEFAULT NULL,
  `quarter` 	 smallint DEFAULT NULL,
  `weekday` 	 smallint DEFAULT NULL,
  `monthname`  char(10) DEFAULT NULL,
  `dayname` char(10) DEFAULT NULL,
  PRIMARY KEY (`complete_date`)
);

# populate the table with dates
INSERT INTO time_dimension
SELECT @date := date_format(date_add(@date, interval 30 minute), "%Y/%c/%e %H:%i") as complete_date,
    date_format(@date, "%Y/%c/%e") as date,
	date_format(@date, '%H:%i') as half_hour,
    year(@date) as year,
    month(@date) as month,
    day(@date) as day,
    week(@date, 3) as week,
    quarter(@date) as quarter,
    weekday(@date)+1 as weekday,
    monthname(@date) as monthname,
    dayname(@date) as dayname
FROM T
WHERE date_add(@date, interval 30 minute) <= @d1
ORDER BY complete_date
;

SELECT date_add(@date, interval 30 minute) <= @d1, @date, @d1;

Select * from time_dimension;