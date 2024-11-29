import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.Scanner;

public class WeatherService {

    private static final String API_KEY = "2dc4d621-36c2-4433-8b81-ffe88a77d295";
    private static final String BASE_URL = "https://api.weather.yandex.ru/v2/forecast";

    public static void main(String[] args) {
        double latitude = 55.75;
        double longitude = 37.62;

        System.out.println("Введите число от 1 до 7, для вычисления средней температуры за этот промежуток");
        Scanner scanner = new Scanner(System.in);
        int limit = scanner.nextInt();

        try {
            String weatherData = getWeatherData(latitude, longitude, limit);
            System.out.println("JSON:");
            System.out.println(weatherData);

            JsonObject jsonObject = JsonParser.parseString(weatherData).getAsJsonObject();

            JsonObject fact = jsonObject.getAsJsonObject("fact");
            int currentTemp = fact.get("temp").getAsInt();
            System.out.println("Текущая температура: " + currentTemp + "°C");

            JsonArray forecasts = jsonObject.getAsJsonArray("forecasts");
            double avgTemperature = calculateAverageTemperature(forecasts, limit);
            System.out.println("Средняя температура за " + limit + " дней: " + avgTemperature + "°C");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getWeatherData(double lat, double lon, int limit) throws Exception {
        OkHttpClient client = new OkHttpClient();

        String url = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&lang=ru_RU&limit=" + limit;

        Request request = new Request.Builder()
                .url(url)
                .header("X-Yandex-Weather-Key", API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Ошибка запроса: " + response.code());
            }
            return response.body().string();
        }
    }

    private static double calculateAverageTemperature(JsonArray forecasts, int limit) {
        double totalTemp = 0;
        int count = 0;

        for (int i = 0; i < limit && i < forecasts.size(); i++) {
            JsonObject forecast = forecasts.get(i).getAsJsonObject();
            JsonObject parts = forecast.getAsJsonObject("parts");

            double dayTemp = parts.getAsJsonObject("day").get("temp_avg").getAsDouble();
            double nightTemp = parts.getAsJsonObject("night").get("temp_avg").getAsDouble();

            double avgTemp = (dayTemp + nightTemp) / 2;
            totalTemp += avgTemp;
            count++;
        }

        return totalTemp / count;
    }
}