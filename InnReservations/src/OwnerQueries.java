/* Used to achieve OR-1 Requirement */
private void viewOccupancy(String startDate, String endDate) {
    if(!startDate.contains("10") and !startDate.contains("11")) {
        startDate += "-10";
    }
    if(endDate == null) {
        oneDateOccupancyQuery(startDate);
    }
    else {
        if(!endDate.contains("10") !endDate.contains("11")) {
            endDate += "-10";
        }
        twoDateOccupancyQuery(startDate, endDate);
    }

}

private void oneDateOccupancyQuery(String inputDate) {
    Vector<Vector<String>> table = new Vector<Vector<String>>();
    String queryToExecute = "select distinct roomname, r1.roomid, " +
        "case when exists (select * from reservations) then 'Occupied' else 'Empty' end Occupied " +
        "from rooms r1, reservations re1 " +
        "where r1.roomid = re1.roomid and " +
        "EXISTS " +
            "(select * from rooms r, reservations re " +
             "where " +
              "r.roomid = re.roomid and " +
              "r1.roomid = r.roomid and " +
              "checkin <= to_date('?', 'DD-MON-YY') and " +
              "checkout > to_date('?', 'DD-MON-YY')) " +
        "UNION " +
        "select distinct roomname, r1.roomid, " +
        "case when not exists (select * from reservations) then 'Occupied' else 'Empty' end Occupied " +
        "from rooms r1, reservations re1 " +
        "where r1.roomid = re1.roomid and " +
        "NOT EXISTS " +
            "(select * from rooms r, reservations re " +
             "where " +
              "r.roomid = re.roomid and " +
              "r1.roomid = r.roomid and " +
              "checkin <= to_date('?', 'DD-MON-YY') and " +
              "checkout > to_date('?', 'DD-MON-YY'))";

    try {
        PreparedStatement ps = conn.prepareStatement(queryToExecute);
        ps.setString(1, inputDate);
        ps.setString(2, inputDate);
        ps.setString(3, inputDate);
        ps.setString(4, inputDate);
        ResultSet results = conn.executeQuery();

        boolean hasNext = results.next();
        while(hasNext) {
            Vector<String> row = new Vector<String>();
            row.addElement(results.getString(1));
            row.addElement(results.getString(2));
            table.add(row);
            hasNext = results.next();
        }

    }
}

/* Handles OR-1 case where range of dates is given. */
private void twoDateOccupancyQuery(String startDate, String endDate){
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
    return;
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
                            "where roomid = r1.roomid and ((checkin <= to_date('?', 'DD-MON-YY') and" +
                            "checkout > to_date('?', 'DD-MON-YY')) or " +
                            "(checkin >= to_date('?', 'DD-MON-YY') and " +
                            "checkin < to_date('?', 'DD-MON-YY')) or " +
                            "(checkout < to_date('?', 'DD-MON-YY') and " +
                            "checkout > to_date('?', 'DD-MON-YY'))));";

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
                                "checkout >= to_date('?', 'DD-MON-YY') and " +
                                "nextin <= to_date('?', 'DD-MON-YY')); ";
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

/* Handles OR-2 */
private void reservationMonthByMonthQuery(String query) {
    String daysCountsQuery = 
        "select *  " +
        "from " +
        "((select roomname, sum(checkout - checkin) as JAN " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JAN' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as FEB " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'FEB' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as MAR " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAR' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as APR " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'APR' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as MAY " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAY' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as JUN " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUN' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as JUL " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUL' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as AUG " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'AUG' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as SEP " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'SEP' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as OCT " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'OCT' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as NOV " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'NOV' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(checkout - checkin) as DEC " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'DEC' " +
        "group by roomname) " +
        "NATURAL JOIN  " +
        "(select roomname, sum(checkout - checkin) as Total " +
        "from reservations, rooms " +
        "where rooms.roomid = reservations.room " +
        "group by roomname));";
    String reservationCountsQuery = "select *  " +
        "from " +
        "((select roomname, count(*) as JAN " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JAN' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as FEB " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'FEB' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as MAR " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAR' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as APR " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'APR' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as MAY " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAY' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as JUN " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUN' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as JUL " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUL' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as AUG " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'AUG' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as SEP " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'SEP' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as OCT " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'OCT' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as NOV " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'NOV' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, count(*) as DEC " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'DEC' " +
        "group by roomname) " +
        "NATURAL JOIN  " +
        "(select roomname, count(*) as Total " +
        "from reservations, rooms " +
        "where rooms.roomid = reservations.room " +
        "group by roomname));";

    String revenuesQuery = "select *  " +
        "from " +
        "((select roomname, sum(rate * (checkout - checkin)) as JAN " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JAN' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as FEB " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'FEB' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as MAR " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAR' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as APR " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'APR' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as MAY " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAY' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as JUN " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUN' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as JUL " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUL' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as AUG " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'AUG' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as SEP " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'SEP' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as OCT " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'OCT' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as NOV " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'NOV' " +
        "group by roomname) " +
        "NATURAL JOIN " +
        "(select roomname, sum(rate * (checkout - checkin)) as DEC " +
        "from reservations, rooms  " +
        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'DEC' " +
        "group by roomname) " +
        "NATURAL JOIN  " +
        "(select roomname, sum(rate * (checkout - checkin)) as Total " +
        "from reservations, rooms " +
        "where rooms.roomid = reservations.room " +
        "group by roomname));";
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
                row.addElement(results.getString(column));
            }
            table.addElement(row);
            hasNext = emptyQueryResults.next();
        }        
    } catch (SQLException e) {
        System.out.println("Reservation counts query failed.");
    }

}


/* Handles OR-3 */
private void browseReservationsQuery(String startDate, String endDate, String room) {
    String queryToExecute;
    if(room == null) {
        queryToExecute = "select code, checkin, checkout
            from reservations
            where checkin >= to_date('?' ,'DD-MON-YY') and 
            checkin < to_date('?', 'DD-MON-YY');";
    } else {
        if(startDate == null) {
            queryToExecute = "select code, checkin, checkout
                from reservations
                where room = '?'";
        }
        else {
            queryToExecute = "select code, checkin, checkout
                from reservations
                where checkin >= to_date('start' ,'DD-MON-YY') and 
                checkin < to_date('end', 'DD-MON-YY') and 
                room = '?';";
        }
    }

    try {
        PreparedStatement ps = conn.prepareStatement(queryToExecute);
        if(room == null) {
            ps.setString(1, startDate);
            ps.setString(2, endDate);
        } else if(startDate == null) {
            ps.setString(1, room);
        } else {
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ps.setString(3, room);
        }
        ResultSet results = conn.executeQuery();
        String[] headers = {"Room, CheckIn, CheckOut"};
        Vector<String> columnHeaders = new Vector<String>().addAll(Arrays.asList(headers));

        Vector<Vector<String>> table = new Vector<Vector<String>>();
        boolean hasNext = results.next();
        while(hasNext) {
            Vector<String> row = new Vector<String>();
            row.addElement(results.getString(1));
            row.addElement(results.getString(2));
            row.addElement(results.getString(3));
            table.add(row);
            hasNext = results.next();
        }
    } catch (SQLException e) {
        System.out.println("Reservations query failed.")
    }
}

private int totalNightsOccupied(String room) {
    String queryToExecute = "select sum(checkout - checkin)
        from reservations
        where to_char(checkout, 'YY') = '2010' and
        room = '?';";

    try {
        PreparedStatement ps = conn.prepareStatement(queryToExecute);
        ps.setString(1, room);
        ResultSet results = conn.executeQuery();
        results.next();
        return results.getInt(1);
    } catch(SQLException e) {
        System.out.println("Nights occupied query failed");
        return -1;
    }
}

private float getTotalRevenueForRoom(String room) {
    String queryToExecute = "select sum((checkout - checkin) * rate)
        from reservations
        where to_char(checkout, 'YY') = '2010' and
        roomid = '?';";
    try {
        PreparedStatement ps = conn.prepareStatement(queryToExecute);
        ps.setString(1, room);
        ResultSet results = conn.executeQuery();
        results.next();
        return results.getFloat(1);
    } catch(SQLException e) {
        System.out.println("Total Revenue query failed");
        return -1.0;
    }
}

private float getTotalRevenueAllRooms() {
    String queryToExecute = "select sum((checkout - checkin) * rate)
        from reservations
        where to_char(checkout, 'YY') = '2010';";
    try {
        conn.executeQuery(queryToExecute);
        results.next();
        return results.getFloat(1);
    } catch (SQLException e) {
        return -1.0;
    }
}

