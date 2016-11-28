package bankingsystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;

public class ClientHandler extends Thread
{
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
    String [] AccountTableColumns = {"PersonName", "Pw", "Balance"};
    public ClientHandler(Socket s)
    {
        this.clientSocket = s;
        this.exitFlag = false;
        DatabaseInterface.Init("jdbc:mysql://127.0.0.1:3306/BankingSystem", "root", "u1234q-a-z");
    }
    @Override
    public void run()
    {
        try
        {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.writeUTF("new?login?");
            request_new_login = dis.readUTF();
            if(request_new_login.equals("Sign In"))
            {
                dos.writeUTF("username?password?");
                while(true)
                {
                    id_password = dis.readUTF();
                    data = id_password.split("\n");
                    ID = data[0];
                    if(DatabaseInterface.Authentication(ID, data[1]))
                    {
                        dos.writeUTF("verified");
                        break;
                    }
                    else
                    {
                        dos.writeUTF("notverifiedlogin");
                    }
                }
            }
            else if(request_new_login.equals("Sign Up"))
            {
                dos.writeUTF("username?password?deposit?");
                username_password_deposit = dis.readUTF();
                data = username_password_deposit.split("\n");
                
                DatabaseInterface.Insertion("account", AccountTableColumns, data) ;
            }
            while(true)
            {
                show_options = dis.readUTF();
                if(show_options.equals("showoptions"))
                {
                    dos.writeUTF("options");
                }
                while(!exitFlag)
                {
                    option = dis.readUTF(); 
                    float balance;
                    switch(option)
                    {
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
                            break;
                        case "withdraw":
                            dos.writeUTF("amountw?");
                            amount_to_withdraw = dis.readUTF();
                            CurrentBalance = DatabaseInterface.CheckBalance(ID);
                            balance = Float.parseFloat(CurrentBalance) - Float.parseFloat(amount_to_withdraw);
                            CurrentBalance = String.valueOf(balance);
                            DatabaseInterface.Update("account", "Balance", CurrentBalance, "ID", ID);
                            break;
                        case "transfersame":
                            dos.writeUTF("amount?account?");
                            amount_account = dis.readUTF();
                            data = amount_account.split(" ");
                            
                            if(DatabaseInterface.ValidAccount(Integer.parseInt(data[1])))
                            {
                                
                                CurrentBalance = DatabaseInterface.CheckBalance(ID);
                                if(Float.parseFloat(CurrentBalance) >= Float.parseFloat(data[0]))
                                {
                                    String OldBalanceReceiver = DatabaseInterface.CheckBalance(data[1]);
                                    float newBalanceSender, newBalanceReceiver;
                                    newBalanceSender = Float.parseFloat(CurrentBalance) - Float.parseFloat(data[0]);
                                    newBalanceReceiver = Float.parseFloat(OldBalanceReceiver) + Float.parseFloat(data[0]);
                                    DatabaseInterface.Update("account", "Balance", String.valueOf(newBalanceSender), "ID", ID);
                                    DatabaseInterface.Update("account", "Balance", String.valueOf(newBalanceReceiver), "ID", data[1]);
                                    dos.writeUTF("done");
                                }
                                else
                                {
                                    dos.writeUTF("errorb");
                                }
                            }
                            else
                            {
                                dos.writeUTF("invalidid");
                            }
                            break;
                        /*case "transferother":
                            //beta3ak ya 3ebs el mips el gebs :D
                            dos.writeUTF("bankname?amount?account?");
                            bank_amount_account = dis.readUTF();
                            data = bank_amount_account.split(" ");
                            //check database for validity of account
                            if(//valid account)
                                    
                                
                            {
                                //check database if balance is enough for tansfer
                                if(//enough)
                                {
                                    //connect to another server
                                    //update database
                                    dos.writeUTF("done");
                                }
                                else
                                {
                                    dos.writeUTF("errorb");
                                }
                            }
                            else
                            {
                                dos.writeUTF("invalidid");
                            }
                            break;*/
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
                            break;
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage() + ": Error with a client happened!");
        }
    }
}
