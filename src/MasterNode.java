import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MasterNode implements Remote {

    public static void main(String[] args) {
        int numSlaves = Integer.parseInt(args[0]); // TODO parameterize
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            // quick test
            for (int i = 0; i < numSlaves; i++) {
                ISlaveNode node = (ISlaveNode) registry.lookup("SlaveNode_" + i);
                node.putVector(null);
                System.out.println("Put a vector to slave" + i);
            }
            // TODO run experiments


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Complete");
    }

    private static void reallocate() {
        // TODO
    }
}
