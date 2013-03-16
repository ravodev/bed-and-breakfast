SELECT x.num
FROM (SELECT TRUNC(dbms_random.value(100000, 999999)) num
		FROM dual) x
WHERE x.num NOT IN (SELECT code
					FROM reservations);