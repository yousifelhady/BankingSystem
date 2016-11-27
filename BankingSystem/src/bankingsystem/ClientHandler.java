package bankingsystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;

public class ClientHandler extends Thread
{
    private Socket clientSocket;
    private String username_password;
    private String request_new_login;
    private String show_options;
    private String option;
    private boolean exitFlag;
    private String username_password_deposit;
    private String amount_to_deposit;
    private String amount_to_withdraw;
    private String amount_account;
    private String[] data;
    private String bank_amount_account;
    public ClientHandler(Socket s)
    {
        this.clientSocket = s;
        this.exitFlag = false;
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
            if(request_new_login == "Sign In")
            {
                dos.writeUTF("username?password?");
                while(true)
                {
                    username_password = dis.readUTF();
                    data = username_password.split("\n");
                    //check in database
                    if(//check is correct)
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
            else if(request_new_login == "Sign Up")
            {
                dos.writeUTF("username?password?deposit?");
                username_password_deposit = dis.readUTF();
                data = username_password_deposit.split("\n");
                //update database
            }
            while(true)
            {
                show_options = dis.readUTF();
                if(show_options == "showoptions")
                {
                    dos.writeUTF("options");
                }
                while(!exitFlag)
                {
                    option = dis.readUTF();
                    switch(option)
                    {
                        case "check":
                            //check on database
                            //dos.writeUTF(amount of money);
                            break;
                        case "deposit":
                            dos.writeUTF("amount?");
                            amount_to_deposit = dis.readUTF();
                            //update database
                            break;
                        case "withdraw":
                            dos.writeUTF("amountw?");
                            amount_to_withdraw = dis.readUTF();
                            //update database
                            break;
                        case "transfersame":
                            dos.writeUTF("amount?account?");
                            amount_account = dis.readUTF();
                            data = amount_account.split(" ");
                            //check database for validity of account
                            if(//valid account)
                            {
                                //check database if balance is enough for tansfer
                                if(//enough)
                                {
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
                            break;
                        case "transferother":
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
