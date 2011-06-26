package a2dp.Vol;
import android.bluetooth.BluetoothDevice;

public interface IBluetoothA2dp {
	 boolean connectSink(BluetoothDevice device);
	    boolean disconnectSink(BluetoothDevice device);
	    boolean suspendSink(BluetoothDevice device);
	    boolean resumeSink(BluetoothDevice device);
	    BluetoothDevice[] getConnectedSinks();  // change to Set<> once AIDL supports
	    BluetoothDevice[] getNonDisconnectedSinks();  // change to Set<> once AIDL supports
	    int getSinkState(BluetoothDevice device);
	    boolean setSinkPriority(BluetoothDevice device, int priority);
	    int getSinkPriority(BluetoothDevice device);

}
