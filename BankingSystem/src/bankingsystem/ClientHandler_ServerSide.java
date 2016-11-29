package bankingsystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;

public class ClientHandler_ServerSide extends Thread {

    private Socket clientSocket;
    private String id_password;
    private String request_new_login;
    private String show_options;
    private String option;
    private String ID;
    private boolean exitFlag;
    private String username_password_deposit;
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
                username_password_deposit = dis.readUTF();
                data = username_password_deposit.split("\n");
                String[] AccountTableColumns = {"PersonName", "Pw", "Telephone", "SSN", "Balance"};
                DatabaseInterface.Insertion("account", AccountTableColumns, data);
                dos.writeUTF("verified");
            } 
            else if (request_new_login.equals("server-transfer")) {
                dos.writeUTF("amount?account?");
                data = dis.readUTF().split(" ");
                //data[0] => amount
                //data[1] => accountID
                //update database
                dos.writeUTF("done");
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
                float balance;
                switch (option) {
                    case "check":
                        CurrentBalance = DatabaseInterface.CheckBalance(ID);
                        dos.writeUTF(CurrentBalance);
                        break;
                    case "deposit":
                        dos.writeUTF("amount?");
                        amount_to_deposit = dis.readUTF();
                        CurrentBalance = DatabaseInterface.CheckBalance(ID);
                        balance = Float.parseFloat(CurrentBalance) + Float.parseFloat(amount_to_deposit);
                        CurrentBalance = String.valueOf(balance);
                        DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
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
                                dos.writeUTF("done");
                            } 
                            else {
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
                            ServerAsclient.transaction(data[1], data[2]);
                            //update database
                            dos.writeUTF("done");
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
