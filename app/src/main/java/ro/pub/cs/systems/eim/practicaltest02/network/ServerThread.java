package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.views.WeatherForecastInformation;

public class ServerThread extends Thread {

    private boolean isRunning;

    private ServerSocket serverSocket;

    private int port;

    private HashMap data = new HashMap<String, WeatherForecastInformation>();

    public ServerThread(int port) {
        this.port = port;
    }

        public void startServer() {
            isRunning = true;
            start();
            Log.v(Constants.TAG, "startServer() method was invoked");
        }

        public void stopServer() {
            isRunning = false;
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
            Log.v(Constants.TAG, "stopServer() method was invoked");
        }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            Log.d(Constants.TAG, "deschis server la portul "+port);
            while (isRunning) {
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    Log.d(Constants.TAG, "trebuia sa se deschida ala de COMUNICARE");
                    CommunicationThread communicationThread = new CommunicationThread(this, socket);
                    communicationThread.start();
                }
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    public synchronized void setData(String city, WeatherForecastInformation weatherForecastInformation) {
        this.data.put(city, weatherForecastInformation);
    }

    public synchronized HashMap<String, WeatherForecastInformation> getData() {
        return data;
    }
}
