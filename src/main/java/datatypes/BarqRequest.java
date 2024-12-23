package datatypes;

import constants.Constants;

public class BarqRequest {
    private final String operationName = "ProfileSearch";
    private final String query = Constants.QUERY_DEFAULT;
    private final Variables variables;

    public BarqRequest(String cursor, Double latitude, Double longitude) {
        this.variables = new Variables(cursor, latitude, longitude);
    }

    public static class Variables {
        private final Integer limit = 100;
        private final Filters filters;
        private final String cursor;

        public Variables(String cursor, Double latitude, Double longitude) {
            this.cursor = cursor;
            this.filters = new Filters(latitude, longitude);
        }
    }

    public static class Filters {
        private final Boolean requireProfileImage = true;
        private final LocationData location;

        Filters(Double latitude, Double longitude) {
            this.location = new LocationData(latitude, longitude);
        }
    }

    public static class LocationData {
        private final String type = "country";
        private final Double latitude;
        private final Double longitude;

        LocationData(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
