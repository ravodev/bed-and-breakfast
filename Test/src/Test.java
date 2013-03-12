//     ---------------------
import java.sql.*;
// -----------------------


import java.util.*;
import java.io.*;
import java.lang.*;

public class Test {
    

//////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////CREATE CONNECTION///////////////////////

    public static void main(String args[]) {
            
        Connection conn;
        int target = 1;
     

	try{
            Class.forName("oracle.jdbc.OracleDriver");
        }
        catch (Exception ex)
        {
            System.out.println("Driver not found");
        };

  String url = "jdbc:oracle:thin:@hercules.csc.calpoly.edu:1522:ora10g";

        conn = null;
	try { 
           conn = DriverManager.getConnection(url, "lplarson", "3373335");
        }
        catch (Exception ex)
        {
            System.out.println("Could not open connection");
        };
       
      System.out.println("Connected");
      try {
           Statement s1 = conn.createStatement();
        String table = "CREATE TABLE Books ";
   table = table + "(LibCode INT, Title VARCHAR2(50), Author VARCHAR2 (50),";
   table = table + "PRIMARY KEY (LibCode) )";

   System.out.println(table);

   s1.executeUpdate(table);

         } catch (Exception ee) {System.out.println(ee);}
 

       try {
             Statement s2 = conn.createStatement();
 s2.executeUpdate("INSERT INTO Books VALUES(1, 'Database Systems','Ullman')");
 s2.executeUpdate("INSERT INTO Books VALUES(2, 'Artificial Intelligence', 'Russel, Norvig')");   
 s2.executeUpdate("INSERT INTO Books VALUES(3, 'Problem Solving in C', 'Hanly, Koffman')");   

           }  catch (Exception ee) {System.out.println(ee);}

   
       try {
     Statement s3 = conn.createStatement();       
     ResultSet result = s3.executeQuery("SELECT Title, Author FROM Books");
              boolean f = result.next(); 
              while (f)
                 {
                  String s = result.getString(1);
                  String a = result.getString(2);
                  System.out.println(s+", "+ a);
                  f=result.next();
                 }

            
            }  catch (Exception ee) {System.out.println(ee);}
        
try {  
     String psText = "INSERT INTO Books VALUES(?,?,?)";
     PreparedStatement ps = conn.prepareStatement(psText);

        
     ps.setInt(1, 4);
     ps.setString(2, "A Guide to LaTeX");
     ps.setString(3, "Kopka, Daly");

     ps.executeUpdate();
      } catch (Exception e03) {System.out.println(e03);}

 try { 
     Statement s4 = conn.createStatement();
     ResultSet result = s4.executeQuery("SELECT Title, Author FROM Books");
              boolean f = result.next(); 
              while (f)
                 {
                  String s = result.getString(1);
                  String a = result.getString(2);
                  System.out.println(s+", "+ a);
                  f=result.next();
                 }

              s4.executeUpdate("DROP TABLE Books");
            }  catch (Exception ee) {System.out.println(ee);}
        


 try {
     conn.close();
 }
 catch (Exception ex)
 {
     System.out.println("Unable to close connection");
 };
        
    }  

}