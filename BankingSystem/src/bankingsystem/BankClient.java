package bankingsystem;

public class BankClient 
{
    public static void main(String[] args) 
    {
        
        ClientHandler_ClientSide client = new ClientHandler_ClientSide();
        client.connectToServer("127.0.0.1", 1234);
        while (true) 
        {
            String serverMessage= client.receiveFromServer();
            client.checkServerMessage(serverMessage);
            client.checkClientInput();
            if(client.getCloseFlag())
                break;
        }
    }
}
