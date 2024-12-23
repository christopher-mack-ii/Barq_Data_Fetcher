package datatypes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DistCount {
    private Long count;
    private Double maxDistance;
}
