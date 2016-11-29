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
        catch (SQLException e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        return false;
    }
    
    //Get user incremental ID
    public static int GetLastID ()
    {
        int res = 0;
        try {
            String query = "SELECT ID FROM account ORDER BY ID DESC LIMIT 1";
            preparedStmt = conn.prepareStatement(query);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next())
            {
                res = rs.getInt("ID");
            }
        }
        catch (SQLException e) {
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
        String tmpColumn = column[0];
        String tmpValue = "\'" + value[0] + "\'";
        for (int i = 1 ; i < column.length ; i++)
        {
            tmpColumn += ", " + column[i];
            tmpValue += ", \'" + value[i] + "\'";
        }
        
        String query = "UPDATE " + tname + " SET " + tmpColumn + " = " + tmpValue
        + " WHERE " + key + " = " + keyValue;
        
        try {
            //create the mysql insert preparedstatement
            preparedStmt = conn.prepareStatement(query);
            
//            for (int i = 0 ; i < column.length ; i++)
//            {
//                preparedStmt.setString (i, value[i]);
//            }
//            preparedStmt.setString (column.length + 1, keyValue);

            //execute the preparedstatement
            preparedStmt.executeUpdate();
        }
        catch (SQLException e)
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
    
    //history accessing
    public static String GetHistory (String[] column, String[] attr, String[] attrValue)
    {
        String tmp = "";
        String tmpColumn = column[0];
        String hist = "";
        if (attr.equals("AccountID")) {
            tmp = "(" + attr[0] + " = " + attrValue[0];
        }
        else {
            tmp = "(" + attr[0] + " = \'" + attrValue[0] + "\'";
        }
        for (int i = 1 ; i < column.length ; i++)
        {
            tmpColumn += " AND " + column[i];
        }
        for (int j = 1; j < attr.length; j++) {
            if (attr.equals("AccountID")) {
                tmp += " AND " + attr[j] + " = " + attrValue[j];
            }
            else {
                tmp += " AND " + attr[j] + " = \'" + attrValue[j] + "\'";
            }
            if (j == column.length - 1)
            {
                tmp += " )";
            } 
        }
        String query = "SELECT "+ tmpColumn + " FROM history WHERE " + tmp;
        try {
            //create the mysql insert preparedstatement
            preparedStmt = conn.prepareStatement(query); 
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            
            while (rs.next()) {
                for(int i = 1; i < columnsNumber; i++)
                    hist += rs.getString(i) + " ";
                System.out.println();
            }
        }
        catch (SQLException e)
        {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        return hist;
    }
}
