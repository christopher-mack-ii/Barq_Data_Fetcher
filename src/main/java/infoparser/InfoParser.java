package infoparser;

import constants.Constants;
import datatypes.CsvUser;
import datatypes.DistCount;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfoParser {
    public static void main(String[] args) throws IOException {
        List<CsvUser> csvUsers;
        try (BufferedReader reader = new BufferedReader(new FileReader(Constants.ASSET_DIRECTORY + "/user_locations.csv"))) {
            csvUsers = reader.lines()
                    .map(line -> line.split(","))
                    .map(parts -> new CsvUser(
                            parts[3], // UUID
                            Double.parseDouble(parts[2]), // Distance
                            parts[0], // City
                            parts[1]  // State
                    )).toList();
        }
        Map<String, CsvUser> usersMap = csvUsers.stream()
                .sorted(Comparator.comparing(CsvUser::getUuid).thenComparing(CsvUser::getDistance))
                .collect(Collectors.toMap(
                        CsvUser::getUuid,
                        csvUser -> csvUser,
                        (existing, replacement) -> existing
                ));
        Map<String, DistCount> usersCityStateMap = new HashMap<>();
        for (CsvUser user : usersMap.values()) {
            String cityState = user.getCityState();
            usersCityStateMap.merge(cityState,
                    new DistCount(1L, user.getDistance()),
                    (existing, newValue) -> {
                        existing.setCount(existing.getCount() + 1);
                        if (existing.getMaxDistance() < user.getDistance()) {
                            existing.setMaxDistance(user.getDistance());
                        }
                        return existing;
                    });
        }
        String csv = usersCityStateMap.entrySet().stream()
                .map(entry -> entry.getKey() + "," + entry.getValue().getCount() + "," + entry.getValue().getMaxDistance())
                .collect(Collectors.joining("\n", "City,State,Count,Max Distance\n", ""));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.ASSET_DIRECTORY + "/data_results.csv"))) {
            writer.write(csv);
        }
    }
}
