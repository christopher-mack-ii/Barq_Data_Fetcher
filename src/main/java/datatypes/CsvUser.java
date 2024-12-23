package datatypes;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CsvUser {
    private String uuid;
    private Double distance;
    private String city;
    private String state;

    public String getCityState() {
        return city + "," + state;
    }
}
