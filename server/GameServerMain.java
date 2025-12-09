package server;

import service.GameService;
import service.GameServiceImpl;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class GameServerMain {

    public static void main(String[] args) {
        try {
            System.out.println("Starting RMI registry on port 1099...");
            LocateRegistry.createRegistry(1099);

            GameService service = new GameServiceImpl();
            Naming.rebind("GameService", service);


            System.out.println("GameService bound. Server ready.");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

