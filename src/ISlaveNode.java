import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISlaveNode extends Remote {
    void putVector(ActiveBitCollection vector) throws RemoteException;
}
