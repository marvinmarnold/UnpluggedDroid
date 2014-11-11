package co.gounplugged.unpluggeddroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import es.theedg.hydra.HydraMsg;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class UnpluggedConnectedThread extends Thread {
	
	// Constants
	private String TAG = "UnpluggedConnectedThread";
	
	// Bluetooth SDK
    private final InputStream mInputStream;
    private final OutputStream mOutputStream;
    private final UnpluggedNode unpluggedNode;
 
    public UnpluggedConnectedThread(BluetoothSocket bluetoothSocket, UnpluggedNode unpluggedNode_) {
        InputStream tInputStream = null;
        OutputStream tOutputStream = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
        	tInputStream = bluetoothSocket.getInputStream();
        	tOutputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) { }
 
        this.mInputStream = tInputStream;
        this.mOutputStream = tOutputStream;
        this.unpluggedNode = unpluggedNode_;
        Log.d(TAG, "created a new");
    }
 
    @Override
    public void run() {
    	Log.d(TAG, "running chat stream");
        byte[] buffer = new byte[50];  // buffer store for the stream
        int bytes; // bytes returned from read()
 
        // Keep listening to the InputStream until an exception occurs
        while (true) {
        	Log.d(TAG, "new message received");
            try {
                // Read from the InputStream
            	bytes = mInputStream.read(buffer);
            	byte[] trimmedBuffer = trim(buffer);
            	logBuffer(trimmedBuffer);
            	handleRead(bytes, trimmedBuffer);
            } catch (IOException e) {
            	unpluggedNode.cancel();
                break;
            }
        }
    }
    
    public void logBuffer(byte[] buffer) {
    	int i = 0;
    	for(byte b : buffer) {
    		Log.d(TAG, "Byte " + Integer.toString(i) + ": " + b);
    		i++;
    	}
    }
    
    public void handleRead(int bytes, byte[] buffer) {
        // Send the obtained bytes to the UI activity 	
		try {
			String str = new String(buffer, "UTF-8");
			Log.d(TAG, "reveived chat: " + str);
			HydraMsg hydraMsg = new HydraMsg(buffer);
			hydraMsg.send(this, unpluggedNode.getUnpluggedMesh());
		} catch (UnsupportedEncodingException e) {	}
    	
    }
 
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
    	Log.d(TAG, "writing HydraMsg");
        try {
        	mOutputStream.write(bytes);
//        	unpluggedNode.sendHydraMsg(bytes);
        	Log.d(TAG, "chat wrote");
        } catch (IOException e) { }
    }
    
    public void cancel() {
    	try {
			mOutputStream.close();
			mInputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    static byte[] trim(byte[] bytes)
    {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }
    
}