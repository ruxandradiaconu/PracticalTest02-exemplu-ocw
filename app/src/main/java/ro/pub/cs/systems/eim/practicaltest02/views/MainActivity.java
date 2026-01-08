package ro.pub.cs.systems.eim.practicaltest02.views;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import ro.pub.cs.systems.eim.practicaltest02.R;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.network.ClientThread;
import ro.pub.cs.systems.eim.practicaltest02.network.ServerThread;

public class MainActivity extends AppCompatActivity {

    private EditText serverPortEditText;
    private Button connectServerButton;
    private EditText clientAddressEditText;
    private EditText clientPortEditText;
    private EditText cityClientEditText;
    private Button getInformationButton;
    private Spinner informationSpinner;
    private TextView weatherResultTextView;

    private ServerThread serverThread;
    private ClientThread clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        serverPortEditText=findViewById(R.id.serverPortEditText);
        clientAddressEditText=findViewById(R.id.addressClientEditText);
        clientPortEditText=findViewById(R.id.clientPortEditText);
        cityClientEditText=findViewById(R.id.clientCityEditText);

        informationSpinner = (Spinner)findViewById(R.id.infoSpinner);

        connectServerButton=findViewById(R.id.connectServerButton);
        connectServerButton.setOnClickListener(new ConnectServerClickListener());

        getInformationButton=findViewById(R.id.getInfoButton);
        getInformationButton.setOnClickListener(new ClientGetInformationButtonClickListener());

        weatherResultTextView = findViewById(R.id.weather_forecast_text_view);

    }

    @Override
    protected void onDestroy() {
        if (serverThread != null) {
            serverThread.stopServer();
        }
        super.onDestroy();
    }

    private class ConnectServerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String serverPort = serverPortEditText.getText().toString();
            // improvizat ca sa nu mai trebuiasca mereu sa bag port
            if (serverPort == null || serverPort.trim().isEmpty()) {
                serverPort = Constants.port;
            }
            if (serverPort == null || serverPort.trim().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Server port should not be null",
                        Toast.LENGTH_LONG).show();
                return;
            }

            serverThread = new ServerThread(Integer.parseInt(serverPort));
            serverThread.startServer();
        }
    }

    private class ClientGetInformationButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String port = clientPortEditText.getText().toString();
            String address = clientAddressEditText.getText().toString();
            String city = cityClientEditText.getText().toString();
            String infoTypeSpinner = informationSpinner.getSelectedItem().toString();
            // improvizat ca sa nu mai trebuiasca mereu sa bag port si adresa localhost
            if (port == null || port.trim().isEmpty() || address == null || address.isEmpty()){
                port = Constants.port;
                address = Constants.address;
            }
            if (port == null || port.trim().isEmpty() ||
                    address == null || address.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Client port should not be null",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (city == null || city.isEmpty() ||
                    infoTypeSpinner == null || infoTypeSpinner.isEmpty()){
                Toast.makeText(getApplicationContext(), "Some client information are not correct",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server " +
                        "to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            weatherResultTextView.setText("no info yet");

            clientThread = new ClientThread(Integer.parseInt(port), address, city, infoTypeSpinner, weatherResultTextView);
            clientThread.start();
        }

    }

}