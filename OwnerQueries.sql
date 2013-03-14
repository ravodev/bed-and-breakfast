
-- OR-1 One Date Query
select distinct roomname, rooms.roomid, 'Occupied'
from rooms, reservations
where rooms.roomid = reservations.roomid and
      EXISTS 
      (select * from rooms r1, reservations v1
        where r1.roomid = rooms.roomid and
              v1.roomid = r1.roomid and
              checkin <= to_date('22-OCT-2010', 'DD-MON-YYYY') and -- Default Year is 2010
              checkout > to_date('22-OCT-2010', 'DD-MON-YYYY')) -- Same Date for checkin/checkout
UNION
select distinct roomname, rooms.roomid, 'Empty'
from rooms, reservations
where rooms.roomid = reservations.roomid and
      NOT EXISTS 
      (select * from rooms r1, reservations v1
        where r1.roomid = rooms.roomid and
              v1.roomid = r1.roomid and
              checkin <= to_date('22-OCT-2010', 'DD-MON-YYYY') and -- Default Year is 2010
              checkout > to_date('22-OCT-2010', 'DD-MON-YYYY')); -- Same Date for checkin/checkout

-- OR-1 Range of Dates



-- IF FULLY OCCUPIED Returns no Tuples
-- IF PARTIALLY OCCUPIED Returns some Tuples

select count(*)
from(
select checkout, nextin
from (select roomid, checkout, lead(checkin) over (order by checkin) nextin
      from reservations
      where roomid = 'RND')
where checkout <> nextin and
      checkout >= to_date('01-JAN-2010', 'DD-MON-YYYY') and
      nextin <= to_date('13-JAN-2010', 'DD-MON-YYYY')
); 



select r1.roomid, 'Empty' as Status
from rooms r1
where r1.roomid NOT IN (
select roomid
from reservations
where roomid = r1.roomid and ((checkin <= to_date('29-MAR-2010', 'DD-MON-YYYY') and
       checkout > to_date('10-APR-2010', 'DD-MON-YYYY')) or
       (checkin >= to_date('29-MAR-2010', 'DD-MON-YYYY') and
         checkin < to_date('10-APR-2010', 'DD-MON-YYYY')) or
       (checkout < to_date('10-APR-2010', 'DD-MON-YYYY') and
        checkout > to_date('29-MAR-2010', 'DD-MON-YYYY')))
);


-- OR-2
select roomname, to_char(checkout, 'MON') 'Month', count(*)
from reservations, rooms 
where to_char(checkout, 'YYYY') = '2010'
group by to_char(checkout, 'MON');

select roomname, to_char(checkout, 'MON') 'Month',
       sum(checkout - checkin) 'Days Occupied'
from reservations, rooms
where to_char(checkout, 'YYYY') = '2010'
group by to_char(checkout, 'MON');

select roomname, to_char(checkout, 'MON') 'Month',
       sum(checkout - checkin) * rate 'Revenue'
from reservations, rooms
where to_char(checkout, 'YYYY') = '2010'
group by to_char(checkout, 'MON');

-- OR-3

-- NO ROOM GIVEN
select code, checkin, checkout
from reservations
where checkin >= to_date('start' ,'DD-MON-YYYY') and -- FILL IN START FROM INPUT
      checkin < to_date('end', 'DD-MON-YYYY'); -- FILL IN END FROM INPUT

-- ROOM GIVEN
select code, checkin, checkout
from reservations, rooms
where checkin >= to_date('start' ,'DD-MON-YYYY') and -- FILL IN START FROM INPUT
      checkin < to_date('end', 'DD-MON-YYYY') and -- FILL IN END FROM INPUT
      roomid = 'ROOMID';  -- FILL IN ROOMID FROM INPUT


-- OR-4

-- LIST OF ROOMS
select roomid, roomname
from rooms;

-- DETAILED ROOM INFO
select *
from rooms
where roomid = 'IDINPUT';  -- GET IDINPUT FROM USER

-- TOTAL NIGHTS OCCUPIED IN 2010
select sum(checkout - checkin)
from reservations
where to_char(checkout, 'YYYY') = '2010' and
      roomid = 'ROOMID';  -- FILL IN ROOMID FROM INPUT

-- Percent of Time occupied is calculated in Java

-- TOTAL REVENUE IN 2010
select sum(checkout - checkin) * rate
from reservations
where to_char(checkout, 'YYYY') = '2010' and
      roomid = 'ROOMID'; -- FILL IN ROOMID FROM INPUT

-- Use this as total revenue for doing percent of overall 2010 revenue for a room.
select sum(Revenue)
from (
        select sum(checkout - checkin) * rate as Revenue
        from reservations
        where to_char(checkout, 'YYYY') = '2010' and
        group by room;
    );

-- OR-5
select reservations.*, rooms.roomname
from reservations, rooms
where rooms.roomid = reservations.room;













