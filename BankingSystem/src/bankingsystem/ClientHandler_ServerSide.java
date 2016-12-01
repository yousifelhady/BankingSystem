package bankingsystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ClientHandler_ServerSide extends Thread {

    private final Socket clientSocket;
    private String ID;
    private final String Bank; 
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean exitFlag;

    public ClientHandler_ServerSide(Socket s) {
        this.clientSocket = s;
        this.exitFlag = false;
        this.Bank = "CIB";
    }
    
    private String[] getTimeStamp()
    {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        return timeStamp.split("_");
    }
    
    private void historyUpdate(String id, String time, String date, String processType, String amount)
    {
        String[] HistoryValues = {id, time, date, processType, amount};
        String[] HistoryTable = {"AccountID", "Time", "Date", "ProcessType", "Amount"};
        DatabaseInterface.Insertion("history", HistoryTable, HistoryValues);
    }
    
    private void depositTransaction()
    {
        try
        {
            dos.writeUTF("amount?");
            String amount_to_deposit = dis.readUTF();
            String CurrentBalance = DatabaseInterface.CheckBalance(ID);
            float balance = Float.parseFloat(CurrentBalance) + Float.parseFloat(amount_to_deposit);
            CurrentBalance = String.valueOf(balance);
            DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
            String[] timeData = getTimeStamp();
            historyUpdate(ID, timeData[1], timeData[0], "Deposit", amount_to_deposit);
            dos.writeUTF(CurrentBalance);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage() + "Error during deposit transaction!");
        }
    }
    
    private void withdrawTransaction()
    {
        try
        {
            dos.writeUTF("amountw?");
            String amount_to_withdraw = dis.readUTF();
            String CurrentBalance = DatabaseInterface.CheckBalance(ID);
            float balance = Float.parseFloat(CurrentBalance) - Float.parseFloat(amount_to_withdraw);
            if(balance > 0)
            {
                CurrentBalance = String.valueOf(balance);
                DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
                String[] timeData = getTimeStamp();
                historyUpdate(ID, timeData[1], timeData[0], "Withdraw", amount_to_withdraw);
                dos.writeUTF(CurrentBalance);
            }
            else
            {
                dos.writeUTF("errorw");
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage() + "Error during withdraw transaction!");
        }
    }
    
    private void checkBalance()
    {
        try
        {
            String CurrentBalance = DatabaseInterface.CheckBalance(ID);
            String[] timeDate = getTimeStamp();
            historyUpdate(ID, timeDate[1], timeDate[0], "Check balance", "");
            dos.writeUTF(CurrentBalance);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage() + "Error during check balance");
        }
    }
    
    private void transferWithinSameBank()
    {
        try
        {
            dos.writeUTF("amount?account?");
            String amount_account = dis.readUTF();
            String[] data = amount_account.split("\n");
            if (DatabaseInterface.ValidAccount(Integer.parseInt(data[1]))) {

                String CurrentBalance = DatabaseInterface.CheckBalance(ID);
                if (Float.parseFloat(CurrentBalance) >= Float.parseFloat(data[0])) {
                    String OldBalanceReceiver = DatabaseInterface.CheckBalance(data[1]);
                    float newBalanceSender = Float.parseFloat(CurrentBalance) - Float.parseFloat(data[0]);
                    float newBalanceReceiver = Float.parseFloat(OldBalanceReceiver) + Float.parseFloat(data[0]);
                    DatabaseInterface.Update("account", "Balance", String.valueOf(newBalanceSender), "ID", ID);
                    DatabaseInterface.Update("account", "Balance", String.valueOf(newBalanceReceiver), "ID", data[1]);
                    String[] timeDate = getTimeStamp();
                    historyUpdate(ID, timeDate[1], timeDate[0], "Transfer to " + data[1], data[0]);
                    historyUpdate(data[1], timeDate[1], timeDate[0], "Transfer from " + ID, data[0]);
                    dos.writeUTF("done");
                } 
                else 
                {
                    dos.writeUTF("errorb");
                }
            } 
            else {
                dos.writeUTF("invalidid");
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage() + "Error during transfer within " + Bank);
        }
    }
    
    private void transferToAnotherBank()
    {
        try
        {
            dos.writeUTF("bankname?amount?account?");
            String bank_amount_account = dis.readUTF();
            String[] data = bank_amount_account.split("\n");
            String CurrentBalance = DatabaseInterface.CheckBalance(ID);

            if(Float.parseFloat(CurrentBalance) >= Float.parseFloat(data[1]))
            {
                ClientHandler_ClientSide ServerAsClient = new ClientHandler_ClientSide();
                String[] ip_port = DatabaseInterface.GetBankIP(data[0]);
                ServerAsClient.connectToServer(ip_port[0], Integer.parseInt(ip_port[1]));
                boolean flag = ServerAsClient.transaction(data[1], data[2]);
                if(flag)
                {
                    float balance = Float.parseFloat(CurrentBalance) - Float.parseFloat(data[1]);
                    CurrentBalance = String.valueOf(balance);
                    DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
                    String[] timeDate = getTimeStamp();
                    historyUpdate(ID, timeDate[1], timeDate[0], "Transfer to " + data[2] + " in " + data[0] + " bank", data[1]);
                    dos.writeUTF("done");
                }
                else
                {
                    dos.writeUTF("invalidid");
                }
            } 
            else {
                dos.writeUTF("errorb");
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage() + "Error during transfer to another bank");
        }
    }
    
    private void viewHistory()
    {
        try
        {
            String[] id = {"AccountID"};
            String[] id_value = {ID};
            String[] _HistoryTable_ = {"Time", "Date", "ProcessType", "Amount"};
            String output = DatabaseInterface.GetHistory(_HistoryTable_, id, id_value);
            dos.writeUTF("#" + output);
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage() + "Error during viewing history");
        }
    }
    
    private void serverWithServer()
    {
        try
        {
            dos.writeUTF("amount?account?");
            String[] data = dis.readUTF().split("\n");
            if(DatabaseInterface.ValidAccount(Integer.parseInt(data[1])))
            {
                ID = data[1];
                String CurrentBalance = DatabaseInterface.CheckBalance(ID);
                float balance = Float.parseFloat(CurrentBalance) + Float.parseFloat(data[0]);
                CurrentBalance = String.valueOf(balance);
                DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
                String[] timeDate = getTimeStamp();
                historyUpdate(ID, timeDate[1], timeDate[0], "Transfer from another bank", data[0]);
                dos.writeUTF("done");
            }
            else
            {
                dos.writeUTF("errorb");
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage() + "Error during communicating between server and server");
        }
    }
    
    private void SignUp()
    {
        try
        {
            dos.writeUTF("details?");
            String details = dis.readUTF();
            String[] data = details.split("\n");
            String[] AccountTableColumns = {"PersonName", "Pw", "Telephone", "SSN", "Balance"};
            DatabaseInterface.Insertion("account", AccountTableColumns, data);
            ID = DatabaseInterface.GetLastID();
            String[] timeDate = getTimeStamp();
            historyUpdate(ID, timeDate[1], timeDate[0], "Create an account and deposit", data[4]);
            dos.writeUTF("verified");
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage() + "Error during sign up process");
        }
    }
    
    private void signIn()
    {
        try
        {
            dos.writeUTF("username?password?");
            while (true) {
                String id_password = dis.readUTF();
                String[] data = id_password.split("\n");
                ID = data[0];
                if (DatabaseInterface.Authentication(ID, data[1])) {
                    dos.writeUTF("verified");
                    break;
                } 
                else {
                    dos.writeUTF("notverifiedlogin");
                }
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage() + "Error during sign in process");
        }
    }
    
    @Override
    public void run() {
        try {
            dis = new DataInputStream(clientSocket.getInputStream());
            dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.writeUTF("connected");
            String request_new_login = dis.readUTF();
            if (request_new_login.equals("Sign In")) {
                signIn();
            } 
            else if (request_new_login.equals("Sign Up")) {
                SignUp();
            } 
            else if (request_new_login.equals("server-transfer")) {
                serverWithServer();
            }
            while (!exitFlag) {
                String show_options = dis.readUTF();
                if (show_options.equals("exit")) {
                    dos.writeUTF("bye");
                    clientSocket.close();
                    dis.close();
                    dos.close();
                    return;
                } 
                else if (show_options.equals("showoptions")) {
                    dos.writeUTF("options");
                }
                String option = dis.readUTF();
                switch (option) {
                    case "check":
                        checkBalance();
                        break;
                    case "deposit":
                        depositTransaction();
                        break;
                    case "withdraw":
                        withdrawTransaction();
                        break;
                    case "transfersame":
                        transferWithinSameBank();
                        break;
                    case "transferother":
                        transferToAnotherBank();
                        break;
                    case "view":
                        viewHistory();
                        break;
                    case "exit":
                        dos.writeUTF("bye");
                        exitFlag = true;
                        clientSocket.close();
                        dis.close();
                        dos.close();
                        return;
                    default:
                        dos.writeUTF("options");
                        break;
                }
            }
        } 
        catch (Exception e) 
        {
            System.out.println(e.getMessage() + ": Error with a client happened!");
        }
    }
}