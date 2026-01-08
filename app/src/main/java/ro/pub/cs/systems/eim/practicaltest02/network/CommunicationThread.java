package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.views.WeatherForecastInformation;


public class CommunicationThread extends Thread {

    private Socket socket;
    private ServerThread serverThread;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.socket = socket;
        this.serverThread = serverThread;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            Log.v(Constants.TAG, "Connection opened with " + socket.getInetAddress() +
                    ":" + socket.getLocalPort());

            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            String city = bufferedReader.readLine();
            String informationType = bufferedReader.readLine();
            Log.d(Constants.TAG, "city=" + city + " si infoType=" + informationType);

            HashMap<String, WeatherForecastInformation> data = serverThread.getData();
            WeatherForecastInformation weatherForecastInformation = null;
            if (data.containsKey(city)) {
                Log.i(Constants.TAG, "[COMMUNICATION] Getting the information from the cache...");
                weatherForecastInformation = data.get(city);
                Log.d(Constants.TAG, weatherForecastInformation.toString());
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION] Getting the information from the webservice...");
                OkHttpClient httpClient = new OkHttpClient();

                Request request = null;

                String urlAddress = Constants.URL + "?q=" + city + "&appid=" + Constants.key +
                        "&units=metric";
                request = new Request.Builder()
                        .url(urlAddress)
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Se extrage conținutul răspunsului sub formă de text.
                        String content = response.body().string();
                        // Aici se poate procesa răspunsul (content).
                        Log.i(Constants.TAG, content);
                        JSONObject result = new JSONObject(content);
                        JSONObject windJSONArray = result.getJSONObject("wind");
                        JSONArray weatherJSONArray = result.getJSONArray("weather");
                        JSONObject mainJSONArray = result.getJSONObject("main");

//                        Log.d(Constants.TAG, "json array=" + windJSONArray);
                        String temperature = null, wind_speed = null, condition = null, pressure = null,
                                humidity = null;
                        if(weatherJSONArray != null) {
                            JSONObject jsonObject = weatherJSONArray.getJSONObject(0);
                            condition = jsonObject.getString("main");
                            Log.d(Constants.TAG, "condition="+condition);
                        }
                        if(windJSONArray != null) {
                            // parse windspeed {"speed":9.26,"deg":60}
//                            JSONObject jsonObject = windJSONArray.getJSONObject(0);
                            wind_speed = windJSONArray.getString("speed");
                            Log.d(Constants.TAG, "wind_speed="+wind_speed);
                        }

                        if(mainJSONArray != null) {
                            temperature = mainJSONArray.getString("temp");
                            pressure=mainJSONArray.getString("pressure");
                            humidity = mainJSONArray.getString("humidity");
                            Log.d(Constants.TAG, "temp="+temperature+" pressure="+pressure+" hum="+humidity);
                        }

                        weatherForecastInformation =  new WeatherForecastInformation(temperature, wind_speed,
                                condition, pressure, humidity);
                        serverThread.setData(city, weatherForecastInformation);
                        // return content;
                    } else {
                        // Se gestionează cazul de eroare (ex: 404 Not Found, 500 Server Error).
                        Log.e(Constants.TAG, "Cererea nu a avut succes. Cod: " + response.code());
                    }
                } catch (IOException e) {
                    // Se gestionează erorile de rețea (ex: fără conexiune, timeout).
                    Log.e(Constants.TAG, "Cererea de rețea a eșuat: " + e.getMessage());
                    if (Constants.DEBUG) {
                        e.printStackTrace();
                    }
                }

            }

            String resultWeather = null;
            switch (informationType){
                case "all": {
                    resultWeather = weatherForecastInformation.toString();
                    break;
                } case "temperature" : {
                    resultWeather = weatherForecastInformation.getTemperature();
                    break;
                } case "pressure" : {
                    resultWeather = weatherForecastInformation.getPressure();
                    break;
                } case "condition" : {
                    resultWeather = weatherForecastInformation.getCondition();
                    break;
                } case "wind_speed" : {
                    resultWeather = weatherForecastInformation.getWindSpeed();
                    break;
                } case "humidity" : {
                    resultWeather = weatherForecastInformation.getHumidity();
                    break;
                } default:
                    resultWeather = "[COMMUNICATION THREAD] Wrong information type " +
                            "(all / temperature / wind_speed / condition / humidity / pressure)!";
            }
            printWriter.println(resultWeather);
            printWriter.flush();


        } catch(Exception exception){
            Log.e(Constants.TAG, "An exception has occurred: " + exception.getMessage());
            if (Constants.DEBUG) {
                exception.printStackTrace();
            }
        } finally {
            try {
                socket.close();
                Log.v(Constants.TAG, "Connection closed");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
