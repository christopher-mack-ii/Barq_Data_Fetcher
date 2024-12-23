package infofetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import constants.Constants;
import datatypes.BarqRequest;
import datatypes.BarqResponse;
import datatypes.CsvLocation;
import datatypes.CsvUser;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;


public class InfoFetcher {
    private static final URI apiUrl = URI.create("https://api.barq.app/graphql");
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Gson gson = new Gson();
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final int CURSOR_DELAY = (int) (1000 * 1.5);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter auth token: ");
        String authToken = scanner.nextLine();

        System.out.println("Enter start point: ");
        int startPoint = scanner.nextInt();

        System.out.println("Enter end point: ");
        int endPoint = scanner.nextInt();

        try {
            List<CsvLocation> locations;
            try (BufferedReader reader = new BufferedReader(new FileReader(Constants.ASSET_DIRECTORY + "/us_cities.csv"))) {
                locations = reader.lines()
                        .skip(1) // Skip the header
                        .map(line -> line.split(","))
                        .map(parts -> new CsvLocation(
                                parts[0].trim(),
                                parts[1].trim(),
                                Double.parseDouble(parts[2].trim()),
                                Double.parseDouble(parts[3].trim())
                        )).toList();
            }
            int numLocations = locations.size();
            if (endPoint > numLocations) {
                endPoint = numLocations;
            }
            for (int i = startPoint; i < endPoint; i++) {
                List<CsvUser> locationUserList = new ArrayList<>();
                CsvLocation location = locations.get(i);
                for (int j = 0; j < 10; j++) {
                    String cursor = String.valueOf(j * 100);
                    String requestBody = gson.toJson(new BarqRequest(cursor, location.getLatitude(), location.getLongitude()));
                    RequestEntity<Object> req = new RequestEntity<>(requestBody, getHeaders(authToken), HttpMethod.POST, apiUrl);
                    ResponseEntity<Object> res;
                    try {
                        System.out.println("Currently processing location: #" + i + ", " + location.getCityState() + " at cursor " + cursor);
                        res = restTemplate.exchange(req, Object.class);
                        if (res.getBody() == null) {
                            throw new Exception("Null response returned.");
                        } else if (res.getStatusCode().is4xxClientError() || res.getStatusCode().is5xxServerError()) {
                            throw new Exception(res.getBody().toString());
                        }
                    } catch (Exception e) {
                        System.err.printf("Failed on location: #%d, %s at cursor %s%nError: %s%n", i, location.getCityState(), cursor, e.getMessage());
                        System.exit(1);
                        return;
                    }
                    BarqResponse barqResponse = objectMapper.readValue(gson.toJson(res.getBody()), BarqResponse.class);
                    locationUserList.addAll(
                            barqResponse.getData().getProfileSearch().stream()
                                    .map(obj -> new CsvUser(
                                            obj.getUuid(),
                                            obj.getLocation().getDistance(),
                                            location.getCity(),
                                            location.getState()
                                    ))
                                    .toList()
                    );
                    Thread.sleep(CURSOR_DELAY);
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.ASSET_DIRECTORY + "/user_locations.csv", true))) {
                    for (CsvUser csvUser : locationUserList) {
                        writer.write(String.format("%s,%s,%s,%s%n", csvUser.getCity(), csvUser.getState(), csvUser.getDistance(), csvUser.getUuid()));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static MultiValueMap<String, String> getHeaders(String authToken) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put("accept", Collections.singletonList("*/*"));
        headers.put("content-type", Collections.singletonList("application/json"));
        headers.put("authorization", Collections.singletonList("Bearer " + authToken));
        return headers;
    }
}