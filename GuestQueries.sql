set pagesize 14;
set linesize 200;


--- R-1

SELECT Name
FROM Rooms;

SELECT Name, Beds, BedType, MaxOcc, Price, Decor
FROM Rooms;

--- R-2

--- iterate through (CheckOut-CheckIn) amount of times in java, parameterize CurDate and the
--- dates in the IN statement
SELECT '22-OCT-10' AS CurDate, 'Occupied' AS Status
FROM Rooms rm, Reservations res
WHERE res.Room = rm.ID AND res.Room = 'AOB' AND 
	res.ID IN (SELECT res.ID
				FROM Reservations res
				WHERE res.CheckIn <= '22-OCT-10' AND
				res.CheckOut > '22-OCT-10')
UNION
SELECT '22-OCT-10' AS CurDate, (parameterized price from R-3) AS Status
FROM Rooms rm, Reservations res
WHERE res.Room = rm.ID AND res.Room = 'AOB' AND 
	res.ID NOT IN (SELECT res.ID
				FROM Reservations res
				WHERE res.CheckIn <= '22-OCT-10' AND
				res.CheckOut > '22-OCT-10');
				

																			
--- R-3

if SELECT to_char(CurDate, 'D') == (6,7) 
price =
	SELECT (1.1 * Price) as Cost
	FROM Rooms
	WHERE Room = 'AOB'
else if SELECT to_char(CurDate, 'D') == 1-5
	SELECT Price
	FROM Rooms
	WHERE Room = 'AOB'
else if CurDate == '1-JAN-10' OR '4-JUL-10' OR '6-SEP-10' OR '30-OCT-10'
	SELECT (1.25 * Price) as Cost
	FROM Rooms
	WHERE Room = 'AOB'

if(AAA)
price = price *.9
else if AARP
price = price * .85

--- R-4
select roomid, roomname
from rooms r
where roomid not in 
	(select r1.roomid
	 from rooms r1, reservations v1
	 where r1.roomid = reservations.room and
	       ((checkin <= to_date(startDate, 'DD-MON-YYYY') and
	       	checkout > to_date(endDate, 'DD-MON-YYYY')) or
	       	(checkin >= to_date(startDate, 'DD-MON-YYYY') and
	       	 checkin < to_date(endDate, 'DD-MON-YYYY'))  or
	       	(checkout > to_date(startDate, 'DD-MON-YYYY') and
	       	 checkout < to_date(endDate, 'DD-MON-YYYY')))
	);


--- R-6
