package bankingsystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ClientHandler_ServerSide extends Thread {

    private Socket clientSocket;
    private String id_password;
    private String request_new_login;
    private String show_options;
    private String option;
    private String ID;
    private boolean exitFlag;
    private String details;
    private String amount_to_deposit;
    private String amount_to_withdraw;
    private String amount_account;
    private String[] data;
    private String bank_amount_account;
    private String CurrentBalance;
    //DatabaseInterface di;
    

    public ClientHandler_ServerSide(Socket s) {
        this.clientSocket = s;
        this.exitFlag = false;
        DatabaseInterface.Init("jdbc:mysql://127.0.0.1:3306/BankingSystem", "root", "u1234q-a-z");
    }

    @Override
    public void run() {
        float balance;
        String[] DateTime;
        String timeStamp;
        String[] HistoryTable = {"AccountID", "Time", "Date", "ProcessType", "Amount"};
        String[] AccountTableColumns = {"PersonName", "Pw", "Telephone", "SSN", "Balance"};
        String[] HistoryValues = new String[5];
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.writeUTF("connected");
            request_new_login = dis.readUTF();
            if (request_new_login.equals("Sign In")) {
                dos.writeUTF("username?password?");
                while (true) {
                    id_password = dis.readUTF();
                    data = id_password.split("\n");
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
            else if (request_new_login.equals("Sign Up")) {
                dos.writeUTF("details?");
                details = dis.readUTF();
                data = details.split("\n");
                DatabaseInterface.Insertion("account", AccountTableColumns, data);
                ID = DatabaseInterface.GetLastID();
                dos.writeUTF("verified");
            } 
            else if (request_new_login.equals("server-transfer")) {
                dos.writeUTF("amount?account?");
                data = dis.readUTF().split("\n");
                if(DatabaseInterface.ValidAccount(Integer.parseInt(data[1])))
                {
                    ID = data[1];
                    CurrentBalance = DatabaseInterface.CheckBalance(ID);
                    balance = Float.parseFloat(CurrentBalance) + Float.parseFloat(data[0]);
                    CurrentBalance = String.valueOf(balance);
                    DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
                    
                    timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    DateTime = timeStamp.split("_");
                    
                    HistoryValues[0] = ID;  HistoryValues[1] = DateTime[1];
                    HistoryValues[2] = DateTime[0]; HistoryValues[3] = "Deposit";
                    HistoryValues[4] = amount_to_deposit;
                    DatabaseInterface.Insertion("history", HistoryTable, HistoryValues);
                    
                    dos.writeUTF("done");
                }
                else
                {
                    dos.writeUTF("errorb");
                }
            }
            while (!exitFlag) {
                show_options = dis.readUTF();
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

                option = dis.readUTF();
                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                DateTime = timeStamp.split("_");
                switch (option) {
                    case "check":
                        CurrentBalance = DatabaseInterface.CheckBalance(ID);
                        HistoryValues[0] = ID;  HistoryValues[1] = DateTime[1];
                        HistoryValues[2] = DateTime[0]; HistoryValues[3] = "Check balance";
                        HistoryValues[4] = "";
                        DatabaseInterface.Insertion("history", HistoryTable, HistoryValues);
                        dos.writeUTF(CurrentBalance);
                        break;
                    case "deposit":
                        dos.writeUTF("amount?");
                        amount_to_deposit = dis.readUTF();
                        CurrentBalance = DatabaseInterface.CheckBalance(ID);
                        balance = Float.parseFloat(CurrentBalance) + Float.parseFloat(amount_to_deposit);
                        CurrentBalance = String.valueOf(balance);
                        DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
                        HistoryValues[0] = ID;  HistoryValues[1] = DateTime[1];
                        HistoryValues[2] = DateTime[0]; HistoryValues[3] = "Deposit";
                        HistoryValues[4] = amount_to_deposit;
                        DatabaseInterface.Insertion("history", HistoryTable, HistoryValues);
                        dos.writeUTF(CurrentBalance);
                        break;
                    case "withdraw":
                        dos.writeUTF("amountw?");
                        amount_to_withdraw = dis.readUTF();
                        CurrentBalance = DatabaseInterface.CheckBalance(ID);
                        balance = Float.parseFloat(CurrentBalance) - Float.parseFloat(amount_to_withdraw);
                        if(balance > 0)
                        {
                            CurrentBalance = String.valueOf(balance);
                            DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
                            HistoryValues[0] = ID;  HistoryValues[1] = DateTime[1];
                            HistoryValues[2] = DateTime[0]; HistoryValues[3] = "Withdraw";
                            HistoryValues[4] = amount_to_withdraw;
                            DatabaseInterface.Insertion("history", HistoryTable, HistoryValues);
                            dos.writeUTF(CurrentBalance);
                        }
                        else
                        {
                            dos.writeUTF("errorw");
                        }
                        break;
                    case "transfersame":
                        dos.writeUTF("amount?account?");
                        amount_account = dis.readUTF();
                        data = amount_account.split("\n");

                        if (DatabaseInterface.ValidAccount(Integer.parseInt(data[1]))) {

                            CurrentBalance = DatabaseInterface.CheckBalance(ID);
                            if (Float.parseFloat(CurrentBalance) >= Float.parseFloat(data[0])) {
                                String OldBalanceReceiver = DatabaseInterface.CheckBalance(data[1]);
                                float newBalanceSender, newBalanceReceiver;
                                newBalanceSender = Float.parseFloat(CurrentBalance) - Float.parseFloat(data[0]);
                                newBalanceReceiver = Float.parseFloat(OldBalanceReceiver) + Float.parseFloat(data[0]);
                                DatabaseInterface.Update("account", "Balance", String.valueOf(newBalanceSender), "ID", ID);
                                DatabaseInterface.Update("account", "Balance", String.valueOf(newBalanceReceiver), "ID", data[1]);
                                
                                HistoryValues[0] = ID;  HistoryValues[1] = DateTime[1];
                                HistoryValues[2] = DateTime[0]; HistoryValues[3] = "Transfer to " + data[1];
                                HistoryValues[4] = data[0];
                                DatabaseInterface.Insertion("history", HistoryTable, HistoryValues);
                        
                                HistoryValues[0] = data[1];  HistoryValues[1] = DateTime[1];
                                HistoryValues[2] = DateTime[0]; HistoryValues[3] = "Transfer from" + ID;
                                HistoryValues[4] = data[0];
                                DatabaseInterface.Insertion("history", HistoryTable, HistoryValues);
                                
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
                        break;
                    case "transferother":
                        dos.writeUTF("bankname?amount?account?");
                        bank_amount_account = dis.readUTF();
                        data = bank_amount_account.split("\n");
                        CurrentBalance = DatabaseInterface.CheckBalance(ID);
                        
                        if(Float.parseFloat(CurrentBalance) >= Float.parseFloat(data[1]))
                        {
                            ClientHandler_ClientSide ServerAsclient = new ClientHandler_ClientSide();
                            ServerAsclient.connectToServer("127.0.0.1", 5005);
                            boolean flag = ServerAsclient.transaction(data[1], data[2]);
                            if(flag)
                            {
                                balance = Float.parseFloat(CurrentBalance) - Float.parseFloat(data[1]);
                                
                                CurrentBalance = String.valueOf(balance);
                                DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
                                
                                HistoryValues[0] = ID;  HistoryValues[1] = DateTime[1];
                                HistoryValues[2] = DateTime[0]; HistoryValues[3] = "Transfer to " + data[2] + " in " + data[0] + " bank";
                                HistoryValues[4] = data[1];
                                DatabaseInterface.Insertion("history", HistoryTable, HistoryValues);
                            
                                dos.writeUTF("done");
                            }
                            else
                            {
                                dos.writeUTF("errorb");
                            }
                        } 
                        else {
                            dos.writeUTF("errorb");
                        }
                        break;
                    case "view":
                        //check database for history
                        //dos.writeUTF(#history);
                        break;
                    case "exit":
                        dos.writeUTF("bye");
                        exitFlag = true;
                        clientSocket.close();
                        dis.close();
                        dos.close();
                        return;
                }
            }
        } 
        catch (Exception e) 
        {
            System.out.println(e.getMessage() + ": Error with a client happened!");
        }
    }
}
