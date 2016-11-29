package bankingsystem;
import java.sql.*;

public class DatabaseInterface 
{
    private static PreparedStatement preparedStmt;
    private static Connection conn;
    public static void Init (String url, String username, String pw)
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
    //check account validity
    public static boolean ValidAccount (int AccountID)
    {
        int res = 0;
        try {
            String query = "SELECT ID FROM account";
            preparedStmt = conn.prepareStatement(query);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next())
            {
                res = rs.getInt("ID");
                if (res == AccountID) {
                    return true;
                }
            }
            
        }
        catch (Exception e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        return false;
    }
    //Get user incremental ID using telephone number
    public static String GetID (String Telephone)
    {
        String res = "";
        try {
            String query = "SELECT ID FROM account WHERE Telephone = ?";
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, Telephone);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next())
            {
                res = rs.getString("ID");
            }
        }
        catch (Exception e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        return res;
    }
    //check authentication
    public static boolean Authentication (String AccountID, String pw)
    {
        try {
            String query = "SELECT Pw FROM account WHERE ID = " + AccountID;
            preparedStmt = conn.prepareStatement(query);
            //preparedStmt.setString (1, AccountID);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            //System.out.println(rs);
            String res = "";
            while(rs.next())
            {
                res = rs.getString("Pw");
            }
            if(res.equals(pw))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
            catch (SQLException e) {
            System.out.println(e.getMessage() + "Got an exception!");
        }
        return false;
    }
    //check balance
    public static String CheckBalance (String AccountID)
    {
        String res = "";
        try {
            String query = "SELECT Balance FROM account WHERE ID =" + AccountID;
            preparedStmt = conn.prepareStatement(query);
            //preparedStmt.setInt(1, Integer.parseInt(AccountID));
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next())
            {
                res = rs.getString("Balance");
            }
        }
        catch (SQLException e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        return res;
    }
    //mysql insert statement
    public static void Insertion (String tname, String[] column, String[] value)
    {
        String tmpColumn = column[0];
        String tmpValue = "\'" + value[0] + "\'";
        for (int i = 1 ; i < column.length ; i++)
        {
            tmpColumn += ", " + column[i];
            tmpValue += ", \'" + value[i] + "\'";
            
        }
        
        String query = "INSERT INTO " + tname + " (" + tmpColumn + ")"
        + " VALUES " + "(" + tmpValue + ")";
        
        try {
            //create the mysql insert preparedstatement
            preparedStmt = conn.prepareStatement(query);
            
//            for (int i = 0 ; i < column.length ; i++)
//            {
//                preparedStmt.setString (i, value[i]);
//            }
            
            //execute the preparedstatement
            preparedStmt.execute();
        }
        catch (SQLException e)
        {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
    }
    
    //mysql update statement
    public static void Update (String tname, String[] column, String[] value, String key, String keyValue)
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
            preparedStmt = conn.prepareStatement(query);
            
            for (int i = 0 ; i < column.length ; i++)
            {
                preparedStmt.setString (i, value[i]);
            }
            preparedStmt.setString (column.length + 1, keyValue);
            //execute the preparedstatement
            preparedStmt.executeUpdate();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
    }
    public static void Update (String tname, String column, String value, String key, String keyValue)
    {
        String query = "UPDATE " + tname + " SET " + column 
        + " = " + value + " WHERE " + key + " = " + keyValue;
        
        try {
            //create the mysql insert preparedstatement
            preparedStmt = conn.prepareStatement(query);
            //preparedStmt.setString (1, value);
            //preparedStmt.setString (2, keyValue);
            
            //execute the preparedstatement
            preparedStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
    }
}
