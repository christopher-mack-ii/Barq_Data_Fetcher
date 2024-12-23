package datatypes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CsvLocation {
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;

    public String getCityState() {
        return city + ", " + state;
    }
}
