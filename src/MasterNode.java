import java.rmi.Naming;
public class MasterNode {

    public static void main(String[] args) {
        int numSlaves = Integer.parseInt(args[0]); // TODO parameterize
        try {
            for (int i = 0; i < numSlaves; i++) {
                SlaveNode node = (SlaveNode) Naming.lookup("//localhost/SlaveNode" + i);
                node.putVector(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Complete");
    }

    private static void reallocate() {
        // TODO
    }
}
