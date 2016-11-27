package bankingsystem;
import java.sql.*;

public class DatabaseInterface 
{
    private Connection conn;
    public DatabaseInterface (String url, String username, String pw)
    {
        try {
            // create a mysql database connection
            String myDriver = "com.mysql.jdbc.Driver";
            Class.forName(myDriver);
            conn = DriverManager.getConnection(url, username, pw);
        }
        catch (Exception e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
    }
    
    //mysql insert statement
    public void Insertion (String tname, String[] column, String[] value)
    {
        String tmpColumn = column[0];
        String tmpValue = "?";
        for (int i = 1 ; i < column.length ; i++)
        {
            tmpColumn += ", " + column[i];
            tmpValue += ", ?";
        }
        
        String query = "INSERT INTO " + tname + " (" + tmpColumn + ")"
        + " VALUES " + tmpValue;
        
        try {
            //create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            
            for (int i = 0 ; i < column.length ; i++)
            {
                preparedStmt.setString (i, value[i]);
            }
            
            //execute the preparedstatement
            preparedStmt.execute();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
    }
    
    //mysql update statement
    public void Update (String tname, String[] column, String[] value, String key, String keyValue)
    {
        String tmpColumn = column[0] + " = ?";
        for (int i = 1 ; i < column.length ; i++)
        {
            tmpColumn += ", " + column[i] + " = ?";
        }
        
        String query = "UPDATE " + tname + " SET " + tmpColumn 
        + " WHERE " + key + " = ?";
        
        try {
            //create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            
            for (int i = 0 ; i < column.length ; i++)
            {
                preparedStmt.setString (i, value[i]);
            }
            
            //execute the preparedstatement
            preparedStmt.executeUpdate();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
    }
}
