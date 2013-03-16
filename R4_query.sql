SELECT k.roomid, MAX(k.Price)
FROM
(SELECT rm.roomid,
      CASE
         WHEN TO_CHAR(d.curdate) = '01-JAN-10' THEN rm.basePrice * 1.25
         WHEN TO_CHAR(d.curdate) = '04-JUL-10' THEN rm.basePrice * 1.25
         WHEN TO_CHAR(d.curdate) = '06-SEP-10' THEN rm.basePrice * 1.25
         WHEN TO_CHAR(d.curdate) = '30-OCT-10' THEN rm.basePrice * 1.25
         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'MONDAY' THEN rm.basePrice
         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'TUESDAY' THEN rm.basePrice
         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'WEDNESDAY' THEN rm.basePrice
         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'THURSDAY' THEN rm.basePrice
         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'FRIDAY' THEN rm.basePrice
         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'SATURDAY' THEN rm.basePrice * 1.10
         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'SUNDAY' THEN rm.basePrice * 1.10
      END AS Price
FROM (SELECT to_date('%s') + rownum - 1 AS CurDate
      FROM reservations 
      WHERE rownum <= to_date('%s') - to_date('%s') + 1) d,
     reservations r, 
     rooms rm 
WHERE r.room = '%s' and rm.roomid = r.room and 
      d.curdate not in (SELECT d.curdate 
                        FROM (SELECT to_date('%s') + rownum - 1 AS CurDate 
                              FROM reservations 
                              WHERE rownum <= to_date('%s') - to_date('%s') + 1) d, 
                             reservations r 
                        WHERE r.room = '%s' and 
                              d.curdate between r.checkin and r.checkout-1)) k
WHERE k.roomid IN (select roomid
					from rooms r
					where roomid not in 
						(select r1.roomid
						 from rooms r1, reservations v1
						 where r1.roomid = v1.room and
						       ((checkin <= to_date('%s', 'DD-MON-YYYY') and
						       	checkout > to_date('%s', 'DD-MON-YYYY')) or
						       	(checkin >= to_date('%s', 'DD-MON-YYYY') and
						       	 checkin < to_date('%s', 'DD-MON-YYYY'))  or
						       	(checkout > to_date('%s', 'DD-MON-YYYY') and
						       	 checkout < to_date('%s', 'DD-MON-YYYY')))
						))
GROUP BY k.roomid;