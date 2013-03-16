/* Used to achieve OR-1 Requirement */
private Vector<Vector<String>> viewOccupancy(String startDate, String endDate) {
    if(!startDate.contains("2010") and !startDate.contains('2011')) {
        startDate += "-2010";
    }
    if(endDate == null) {
        return oneDateOccupancyQuery(startDate);
    }
    else {
        if(!endDate.contains("2010")) {
            endDate += "-2010";
        }
        return twoDateOccupancyQuery(startDate, endDate);
    }

}

/* Handles OR-1 case where range of dates is given. */
private Vector<Vector<String>> twoDateOccupancyQuery(String startDate, String endDate){
    List<String> emptyRooms = findEmptyRoomsInRange(startDate, endDate);
    List<String> fullyOccupiedRooms = findOccupiedRoomsInRange(startDate, endDate, emptyRooms);
    List<String> partiallyOccupiedRooms = 
        (generateListOfAllRoomIDS().removeAll(emptyRooms)).removeAll(fullyOccupiedRooms);

    occupancyColumns = new Vector<String>();
    occupancyData = new Vector<Vector<String>>();
    occupancyColumns.addElement("RoomId");
    occupancyColumns.addElement("Occupancy Status");

    for(String room: emptyRooms) {
        Vector<String> row = new Vector<String>();
        row.addElement(room);
        row.addElement("Empty");
        occupancyData.addElement(row);
    }
    for(String room: fullyOccupiedRooms) {
        Vector<String> row = new Vector<String>();
        row.addElement(room);
        row.addElement("Fully Occupied");
        occupancyData.addElement(row);
    }
    for(String room: partiallyOccupiedRooms) {
        Vector<String> row = new Vector<String>();
        row.addElement(room);
        row.addElement("Partially Occupied");
        occupancyData.addElement(row);
    }
    return occupancyData;
}

/* Finds and returns the list of rooms that are completely empty. */
private List<String> findEmptyRoomsInRange(String startDate, String endDate) {
    /* startDate indices = 1,3,6 
    endDate indices = 2,4,5  */
    String emptyRoomsQuery = "select r1.roomid " +
                            "from rooms r1 " +
                            "where r1.roomid NOT IN (" +
                            "select roomid" +
                            "from reservations" + 
                            "where roomid = r1.roomid and ((checkin <= to_date('?', 'DD-MON-YYYY') and" +
                            "checkout > to_date('?', 'DD-MON-YYYY')) or " +
                            "(checkin >= to_date('?', 'DD-MON-YYYY') and " +
                            "checkin < to_date('?', 'DD-MON-YYYY')) or " +
                            "(checkout < to_date('?', 'DD-MON-YYYY') and " +
                            "checkout > to_date('?', 'DD-MON-YYYY'))));";

    try {
        PreparedStatement erq = conn.prepareStatement(emptyRoomsQuery);
        erq.setString(1, startDate);
        erq.setString(3, startDate);
        erq.setString(6, startDate);
        erq.setString(2, endDate);
        erq.setString(4, endDate);
        erq.setString(5, endDate);
        ResultSet emptyRoomsQueryResult = erq.executeQuery();
        List<String> emptyRooms = getEmptyRoomsFromResultSet(emptyRoomsQueryResult);
        return emptyRooms;
    } catch (SQLException e) {
        System.out.println("Empty rooms query failed.")
    }
    return null;
}

/* Returns a list of strings. This list contains the RoomIds of all completely empty rooms. */
private List<String> getEmptyRoomsFromResultSet(ResultSet emptyQueryResults) {
    ArrayList<String> rooms = new ArrayList<String>();
    boolean hasNext = emptyQueryResults.next();
    while(hasNext) {
        String room = emptyQueryResults.getString("r1.roomid");
        rooms.add(room);
        hasNext = emptyQueryResults.next();
    }
    return rooms;
}

private List<String> generateListOfAllRoomIDS() {
    String[] rooms = {"AOB", "CAS", "FNA", "HBB", "IBD", "IBS", "MWC", "RND", "RTE", "TAA"};
    return ArrayList<String>(Arrays.asList(room));
}

private List<String> findOccupiedRoomsInRange(String startDate, String endDate, String<List> emptyRooms) {
    ArrayList<String> nonEmptyRooms = generateListOfAllRoomIDS().removeAll(emptyRooms);
    ArrayList<String> fullyOccupiedRooms = new ArrayList<String>();
    String occupiedRoomsQuery = "select count(*) " +
                                "from(" +
                                "select checkout, nextin " +
                                "from (select roomid, checkout, lead(checkin) over (order by checkin) nextin " +
                                "from reservations " + 
                                "where roomid = '?') " +
                                "where checkout <> nextin and " +
                                "checkout >= to_date('?', 'DD-MON-YYYY') and " +
                                "nextin <= to_date('?', 'DD-MON-YYYY')); ";
    try {
        PreparedStatement oq = conn.prepareStatement(occupiedRoomsQuery);
        /* Check if each room is completely occupied. */
        for(String room: nonEmptyRooms) {
            oq.setString(1, room);
            oq.setString(2, startDate);
            oq.setString(3, endDate);
            ResultSet result = conn.executeQuery();
            result.next();
            /* If the count is 0, the room is fully occupied. Else it is partially occupied. */
            if(0 == result.getInt(1)) {
                fullyOccupiedRooms.add(room);
            }
            oq.clearParameters();
        }
    } catch (SQLException e) {
        System.out.println("Occupied Query Failed.");
        return null;
    }
    return fullyOccupiedRooms;
}


private void reservationMonthByMonthQuery(String query) {
    String daysCountsQuery = "
        select * 
        from
        ((select roomname, sum(checkout - checkin) as JAN
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JAN'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as FEB
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'FEB'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as MAR
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAR'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as APR
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'APR'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as MAY
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAY'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as JUN
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUN'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as JUL
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUL'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as AUG
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'AUG'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as SEP
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'SEP'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as OCT
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'OCT'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as NOV
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'NOV'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(checkout - checkin) as DEC
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'DEC'
        group by roomname)
        NATURAL JOIN 
        (select roomname, sum(checkout - checkin) as Total
        from reservations, rooms
        where rooms.roomid = reservations.room
        group by roomname));";
    String reservationCountsQuery = "select * 
        from
        ((select roomname, count(*) as JAN
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JAN'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as FEB
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'FEB'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as MAR
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAR'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as APR
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'APR'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as MAY
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAY'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as JUN
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUN'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as JUL
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUL'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as AUG
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'AUG'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as SEP
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'SEP'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as OCT
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'OCT'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as NOV
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'NOV'
        group by roomname)
        NATURAL JOIN
        (select roomname, count(*) as DEC
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'DEC'
        group by roomname)
        NATURAL JOIN 
        (select roomname, count(*) as Total
        from reservations, rooms
        where rooms.roomid = reservations.room
        group by roomname));";

    String revenuesQuery = "select * 
        from
        ((select roomname, sum(rate * (checkout - checkin)) as JAN
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JAN'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as FEB
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'FEB'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as MAR
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAR'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as APR
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'APR'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as MAY
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAY'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as JUN
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUN'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as JUL
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUL'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as AUG
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'AUG'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as SEP
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'SEP'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as OCT
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'OCT'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as NOV
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'NOV'
        group by roomname)
        NATURAL JOIN
        (select roomname, sum(rate * (checkout - checkin)) as DEC
        from reservations, rooms 
        where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'DEC'
        group by roomname)
        NATURAL JOIN 
        (select roomname, sum(rate * (checkout - checkin)) as Total
        from reservations, rooms
        where rooms.roomid = reservations.room
        group by roomname));";
    try {
        String queryToExecute;
        switch(query) {
            case "counts":
                queryToExecute = reservationCountsQuery;
                break;
            case "days":
                queryToExecute = daysCountsQuery;
                break;
            default:
                queryToExecute = revenuesQuery;
                break;

        }
        ResultSet results = conn.executeQuery(queryToExecute);
        Vector<Vector<String>> table = new Vector<Vector<String>>();
        String[] headers = {"Roomname", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL",
                    "AUG", "SEP", "OCT", "NOV", "DEC", "TOTAL"};
        Vector<String> columnHeaders = new Vector<String>();
        columnHeaders.addAll(Arrays.asList(headers));



        boolean hasNext = results.next();
        while(hasNext) {
            Vector<String> row = new Vector<String>();
            for(int column = 1; column < 14) {
                row.add(results.getString(column));
            }
            table.add(row);
            hasNext = emptyQueryResults.next();
        }        
    } catch (SQLException e) {
        System.out.println("Reservation counts query failed.");
    }

}

