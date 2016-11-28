package bankingsystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client 
{

    public static void ShowOptions()
    {
        System.out.println("Choose an option:\n1)Check on current balance.\n2)Deposit cash.\n3)Withdraw cash.\n4)Transfer money to another account within the same bank.\n5)Transfer money to another account in another bank.\n6)View transaction history.\n7)Exit.");
    }
    static int close = 0;
    static int state;
    public static void checkServerMessage(String msg, DataInputStream dis, DataOutputStream dos)
    {
        if (msg.equals("new?login?"))
        {
            state = 1; 
            System.out.println("Sign In \n Sign Up"); 
        }
        else if(msg.equals("username?password?deposit?")) // sign up
        {
            state = 2;
            System.out.println("Enter Username, Password, Deposit");
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
        else if(msg.equals("notverifiednew"))// error while creating new account
        {
            state = 2;
            System.out.println("ERROR try again!");
            System.out.println("Enter Username, Password, Deposit");
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
        else if(msg.matches("[0-9].*"))//balance + go back to options
        {
            state = 5;
            System.out.println("Current balane is" + msg);
            ShowOptions();
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
            state = 8;
            System.out.println("Invalid account ID! Try Again.");
            System.out.println("Please enter the amount to be transfered and the account id");
        }
        else if(msg.equals("errorb"))
        {
            state = 8;
            System.out.println("Cannot transfer this amount! Try Again.");
            System.out.println("Please enter the amount to be transfered and the account id");
        }
        else if(msg.equals("done"))
        {
            state = 5;
            ShowOptions();
        }
        else if(msg.equals("bankname?amount?account?")) // option 5 transfer to another bank
        {
            state = 9;
            System.out.println("Please enter the Bank name , the amount to be transfered and the account id");
        }
        else if((msg.substring(0,1)).matches("#"))
        {
            System.out.println(msg);
        }
        else if(msg.equals("bye"))
        {
            close = 1;
        }

    }

    public static void checkClientInput(DataInputStream dis, DataOutputStream dos)
    {   
        try {
            String username; String password;
            String deposit; String toBeSent;
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
                deposit = uname.nextLine();
                toBeSent = username + "\n" + password + "\n" + deposit;
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
        catch (Exception ee)
        {
            System.out.println("Something went wrong");
        }


    }
    public static void main(String[] args) {

        try {
            //1.create client socket and connect to server
            Socket client = new Socket("127.0.0.1", 1234);
            //2.creat comm streams
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());
            DataInputStream dis = new DataInputStream(client.getInputStream());
            
            //3.perform I/O with server
            //Scanner cin = new Scanner(System.in);
            while (true) {
                
                String serverMessage= dis.readUTF();
                
                
                // Server Message + shkl l output 3l system
                checkServerMessage(serverMessage,dis,dos);
                
                //String userInput = cin.nextLine();
                // check input from user & send to server
                checkClientInput(dis,dos);
                //dos.writeUTF(userInput);
                
                //System.out.println("Server Says :" + serverResp);
                //System.out.println(output);
                if(close == 1)
                    break;
                
            }

            //4.terminate connection with server
            //client.close();
            //dis.close();
            //dos.close();

        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        
    }

}