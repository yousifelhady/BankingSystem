package bankingsystem;

import java.net.ServerSocket;
import java.net.Socket;

public class BankingSystem {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            ServerSocket server = new ServerSocket(5005);
            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("Connected with a client!");
                //ClientHandler ch = new ClientHandler(c);
                //ch.start();

            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage() + ": Connection with a client failed!");
        }
    }
    
}
