package bankingsystem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BankServer {
    
    public static void main(String[] args) 
    {
        try
        {
            ServerSocket server = new ServerSocket(1234);
            
            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("Connected with a client!");
                ClientHandler_ServerSide ch = new ClientHandler_ServerSide(clientSocket);
                ch.start();

            }
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage() + ": Connection with a client failed!");
        }
    }
    
}
