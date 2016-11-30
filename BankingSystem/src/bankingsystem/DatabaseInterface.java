package bankingsystem;
import java.sql.*;

public class DatabaseInterface 
{
    private static PreparedStatement preparedStmt;
    private static Connection conn;
    
    public static void Init (String url, String username, String pw)
    {
        try {
            String myDriver = "com.mysql.jdbc.Driver";
            Class.forName(myDriver);
            conn = DriverManager.getConnection(url, username, pw);
        }
        catch (Exception e) {
            System.out.println(e.getMessage() + "Error connecting to database!");
        }
    }

    public static boolean ValidAccount (int AccountID)
    {
        int res;
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
            System.out.println(e.getMessage() + "Error during checking validity of an account!");
        }
        return false;
    }

    public static String GetLastID ()
    {
        String res = "";
        try {
            String query = "SELECT ID FROM account ORDER BY ID DESC LIMIT 1";
            preparedStmt = conn.prepareStatement(query);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next())
            {
                res = rs.getString("ID");
            }
        }
        catch (SQLException e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        return res;
    }

    public static boolean Authentication (String AccountID, String pw)
    {
        try {
            String query = "SELECT Pw FROM account WHERE ID = " + AccountID;
            preparedStmt = conn.prepareStatement(query);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
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
            System.out.println(e.getMessage() + "Error during authentication!");
        }
        return false;
    }

    public static String CheckBalance (String AccountID)
    {
        String res = "";
        try {
            String query = "SELECT Balance FROM account WHERE ID =" + AccountID;
            preparedStmt = conn.prepareStatement(query);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next())
            {
                res = rs.getString("Balance");
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage() + "Error in CheckBalance!");
        }
        return res;
    }

    public static void Insertion (String tname, String[] column, String[] value)
    {
        String tmpColumn = column[0];
        String tmpValue;
        if (column[0].equals("ID")) {
            tmpValue = value[0];
        }
        else {
            tmpValue = "\'" + value[0] + "\'";
        }
        for (int i = 1 ; i < column.length ; i++)
        {
            tmpColumn += ", " + column[i];
            tmpValue += ", \'" + value[i] + "\'";
        }
        
        String query = "INSERT INTO " + tname + " (" + tmpColumn + ")"
        + " VALUES " + "(" + tmpValue + ")";
        
        try {
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.execute();
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage() + "Error in insertion to database!");
        }
    }

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
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage() + "Error in updating database!");
        }
    }
    
    public static void Update (String tname, String column, String value, String key, String keyValue)
    {
        String query = "UPDATE " + tname + " SET " + column 
        + " = " + value + " WHERE " + key + " = " + keyValue;
        
        try {
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage() + "Error in updating database!");
        }
    }

    public static String GetHistory (String[] column, String[] attr, String[] attrValue)
    {
        String tmp;
        String tmpColumn = column[0];
        String hist = "";
        if (attr[0].equals("AccountID")) {
            tmp = "(" + attr[0] + " = " + attrValue[0];
        }
        else {
            tmp = "(" + attr[0] + " = \'" + attrValue[0] + "\'";
        }
        for (int i = 1 ; i < column.length ; i++)
        {
            tmpColumn += ", " + column[i];
        }
        for (int j = 1; j < attr.length; j++) {
            if (attr[0].equals("AccountID")) {
                tmp += " AND " + attr[j] + " = " + attrValue[j];
            }
            else {
                tmp += " AND " + attr[j] + " = \'" + attrValue[j] + "\'";
            }
        }
        tmp += ")";
        String query = "SELECT "+ tmpColumn + " FROM history WHERE " + tmp;
        try {
            preparedStmt = conn.prepareStatement(query); 
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for(int i = 1; i <= columnsNumber; i++)
                    hist += rs.getString(i) + "\t\t\t";
                hist += "\r\n";
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage() + "Error in getting history from database!");
        }
        return hist;
    }

    public static String[] GetBankIP (String bankName)
    {
        String []res = new String [2];
        String query = "SELECT ServerIP, ServerPort FROM bank WHERE Name =\'" + bankName + "\'";
        try {
            preparedStmt = conn.prepareStatement(query); 
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for(int i = 1; i <= columnsNumber; i++)
                    res[i-1] = rs.getString(i);
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage() + "Error in getting bank's IP and port number!");
        }
        return res;
    }
}