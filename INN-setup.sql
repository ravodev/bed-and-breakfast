-- Andrew Sinclair
-- asinclai@calpoly.edu
-- CPE/CSC 365 Section 3 Winter 2013
CREATE TABLE Rooms (
   RoomId Char(3) Primary Key,
   RoomName Varchar2(25) Unique,
   Beds Int,
   BedType Varchar2(10),
   MaxOcc Int,
   Price Int,
   Decor Varchar2(15)
);

CREATE TABLE Reservations (
   Code Int Primary Key,
   Room Char(3) References Rooms(RoomId),
   CheckIn Date,
   CheckOut Date,
   Rate Float,
   LastName Varchar2(15),
   FirstName Varchar2(15),
   Adults Int,
   Kids Int
);
