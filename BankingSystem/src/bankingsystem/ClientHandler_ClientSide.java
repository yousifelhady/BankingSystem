package bankingsystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler_ClientSide 
{
    private boolean close = false;
    private int state;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Socket client;
    
    public String receiveFromServer()
    {
        String r = "";
        try
        {
            r = dis.readUTF();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage() + ": Error receiving from server!");
        }
        return r;
    }
    
    public void connectToServer(String address, int port)
    {
        try
        {
            client = new Socket(address, port);
            dos = new DataOutputStream(client.getOutputStream());
            dis = new DataInputStream(client.getInputStream());
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage() + ": Error connecting to server!");
        }
    }
    
    public boolean getCloseFlag()
    {
        return close;
    }
    
    public void ShowOptions()
    {
        System.out.println("Choose an option:\n1)Check on current balance.\n2)Deposit cash.\n3)Withdraw cash.\n4)Transfer money to another account within the same bank.\n5)Transfer money to another account in another bank.\n6)View transaction history.\n7)Exit.");
    }
    
    public boolean transaction(String amount, String accountID)
    {
        try
        {
            while(true)
            {
                String serverMessage= this.receiveFromServer();
                switch(serverMessage)
                {
                    case "connected":
                        dos.writeUTF("server-transfer");
                        break;
                    case "amount?account?":
                        dos.writeUTF(amount + "\n" + accountID);
                        break;
                    case "done":
                        dos.writeUTF("exit");
                        break;
                    case "errorb":
                        return false;
                    case "bye":
                        return true;
                }
            }
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage() + ": Error communicating with another banking system!");
        }
        return false;
    }
    
    public void checkServerMessage(String msg)
    {   
        if (msg.equals("connected"))
        {
            state = 1; 
            System.out.println("Sign In\nSign Up"); 
        }
        else if(msg.equals("username?amount?"))
        {
            state = 2;
        }
        else if(msg.equals("details?")) // sign up
        {
            state = 2;
            System.out.println("Enter Username, Password, Telephone, SSN, Amount to deposit");
        }
        else if(msg.equals("username?password?")) //sign in
        {
            state = 3;
            System.out.println("Enter Username, Password");
        }
        else if(msg.equals("verified"))
        {
            state = 4;
            System.out.println("Signed in Successfully!");
        }
        else if(msg.equals("notverifiedlogin")) // error while sign in
        {
            state = 3;
            System.out.println("ERROR try again!");
            System.out.println("Enter Username, Password");
        }
        else if(msg.equals("options"))
        {
            state = 5;
            ShowOptions();
        }
        else if(msg.matches("[0-9.]*"))//balance + go back to options
        {
            state = 4;
            System.out.println("Current balane is: " + msg);
            //ShowOptions();
        }
        else if(msg.equals("amount?")) // option 2 deposit
        {
            state = 6;
           System.out.println("Please enter the amount");
        }
        else if(msg.equals("amountw?")) // option 3 withdraw
        {
            state = 7;
            System.out.println("Please enter the amount");
        }
        else if(msg.equals("amount?account?")) // transfer within same bank 
        {
            state = 8;
            System.out.println("Please enter the amount to be transfered and the account id");
        }
        else if(msg.equals("invalidid"))
        {
            //state = 8;
            state = 4 ;
            System.out.println("Invalid account ID!");
            //.out.println("Please enter the amount to be transfered and the account id");
        }
        else if(msg.equals("errorb"))
        {
            //state = 8;
            state = 4 ;
            System.out.println("Cannot transfer this amount!.");
            //System.out.println("Please enter the amount to be transfered and the account id");
        }
        else if(msg.equals("done"))
        {
            state = 4;
            //ShowOptions();
        }
        else if(msg.equals("bankname?amount?account?")) // option 5 transfer to another bank
        {
            state = 9;
            System.out.println("Please enter the Bank name , the amount to be transfered and the account id");
        }
        else if (msg.equals("errorw"))
        {
            state = 4 ;
            System.out.println("Your Current Balance is not enough!!!");
        }
        else if((msg.substring(0,1)).matches("#"))
        {
            System.out.println(msg);
        }
        else if(msg.equals("bye"))
        {
            close = true;
        }
    }
    public void checkClientInput()
    {   
        try {
            String username; String password;
            String deposit; String toBeSent;
            String ssn; String telephone;
            String amount; String accountID;
            Scanner m = new Scanner(System.in);
            if (state == 1)
            {
               Scanner uinput = new Scanner(System.in);
               String userInput = uinput.nextLine();
               dos.writeUTF(userInput); // sign in or sign up // to be sent to server
            }
            else if (state == 2) // sign up
            {
                Scanner uname = new Scanner(System.in);
                username = uname.nextLine();
                password = uname.nextLine();
                telephone = uname.nextLine();
                ssn = uname.nextLine();
                deposit = uname.nextLine();
                toBeSent = username + "\n" + password + "\n" + telephone + "\n" + ssn + "\n" + deposit;
                dos.writeUTF(toBeSent);
            }
            else if (state == 3)
            {
                Scanner signin = new Scanner(System.in);
                username = signin.nextLine();
                password = signin.nextLine();
                toBeSent = username + "\n" + password;
                dos.writeUTF(toBeSent);
            }
            else if (state == 4)
            {
                dos.writeUTF("showoptions");
            }
            else if (state == 5)
            {
                Scanner opno = new Scanner(System.in);
                int OptionNo = opno.nextInt();
                switch (OptionNo)
                {
                    case 1:
                        dos.writeUTF("check");
                        break;
                    case 2:
                        dos.writeUTF("deposit");
                        break;
                    case 3:
                        dos.writeUTF("withdraw");
                        break;
                    case 4:
                        dos.writeUTF("transfersame");
                        break;
                    case 5:
                        dos.writeUTF("transferother");
                        break;
                    case 6:   
                        dos.writeUTF("view");
                        break;
                    case 7:
                        dos.writeUTF("exit");
                        break;
                }   
            }
            else if (state == 6)
            {
                Scanner b = new Scanner(System.in);
                String balance = b.nextLine();
                dos.writeUTF(balance);
            }
            else if(state == 7)
            {
                 Scanner w = new Scanner(System.in);
                 String  withdraw = w.nextLine();
                 dos.writeUTF(withdraw);
            }
            else if(state == 8)
            {
                amount = m.nextLine();
                accountID = m.nextLine();
                toBeSent = amount + "\n" + accountID;
                dos.writeUTF(toBeSent);
            }
            else if(state == 9)
            {
                String bankname = m.nextLine();
                amount = m.nextLine();
                accountID = m.nextLine();
                toBeSent = bankname + "\n" + amount + "\n" + accountID;
                dos.writeUTF(toBeSent);
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage() + "Error sending options to server!");
        }
    }
}