import java.io.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class SlaveNode implements Remote {

    private String id;

    protected SlaveNode(String id) {
        this.id = id;
    }

    public static void main(String[] args) {
        String id = args[0]; // TODO parameterize arg list
        // TODO: setup name and IP address correctly
        try {
            Naming.rebind("//localhost/SlaveNode" + id, new SlaveNode(id));
        } catch (Exception e) {
            System.err.println("Slave node exception: " + e.toString());
            e.printStackTrace();
        }
    }

    // RMI
    public void putVector(BitmapVector vector) throws IllegalArgumentException, RemoteException {
        if (vector == null) {
            System.out.println("Null vector"); // TODO throw exception
            return;
            // throw new IllegalArgumentException("Null vector");
        }
        final String path = getVectorPath(vector.toString(), vector.getId());
        try {
            FileWriter writer = new FileWriter(path);
            vector.getWords().forEach(word -> {
                try {
                    writer.write(word + "/n");
                } catch (IOException e) {
                    handleIOException(e);
                }
            });
        } catch (IOException e) {
            handleIOException(e);
        }

    }

    // RMI
    public void sendVector(BitmapVectorType vectorType, String vectorId, String otherNode) throws IllegalArgumentException {
        if (Stream.of(vectorType, vectorId, otherNode).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Must provide nonnull arguments");
        }
        final String vectorPath = getVectorPath(vectorType.toString(), vectorId);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(vectorPath));
            List<Long> list = new ArrayList<Long>();
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(Long.parseLong(line));
            }
            SlaveNode node = (SlaveNode) Naming.lookup(otherNode);
            switch (vectorType) {
                case WAH:
                    node.putVector(new WAHVector(vectorId, list));
                default:
                    throw new IllegalArgumentException();
            }
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    // RMI
    public String getId() {
        return this.id;
    }

    private String getVectorPath(String vectorType, String vectorID) {
        return vectorType + "/" + vectorID;
    }

    private void handleIOException(IOException e) throws IllegalArgumentException {
        e.printStackTrace();
        throw new IllegalArgumentException("Could not write BitmapVector at this time. Reason: " + e.getMessage());
    }
}
