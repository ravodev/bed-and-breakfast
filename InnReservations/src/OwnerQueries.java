/* Used to achieve OR-1 Requirement */
private void viewOccupancy(String startDate, String endDate) {
    if(!startDate.contains("2010")) {
        startDate += "-2010";
    }
    if(endDate == null) {
        oneDateOccupancyQuery(startDate);
        return;
    }
    else {
        if(!endDate.contains("2010")) {
            endDate += "-2010";
        }
        twoDateOccupancyQuery(startDate, endDate);
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
                                "checkout >= to_date('01-JAN-2010', 'DD-MON-YYYY') and " +
                                "nextin <= to_date('13-JAN-2010', 'DD-MON-YYYY')); ";
    try {
        PreparedStatement oq = conn.prepareStatement(occupiedRoomsQuery);
        /* Check if each room is completely occupied. */
        for(String room: nonEmptyRooms) {
            oq.setString(1, room);
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

