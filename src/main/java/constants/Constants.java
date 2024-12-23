package constants;

public interface Constants {
    String QUERY_DEFAULT = """
            query ProfileSearch($filters: ProfileSearchFiltersInput! = {}, $cursor: String = "", $limit: Int = 100) {
                profileSearch(
                    filters: $filters
                    cursor: $cursor
                    limit: $limit
                    sort: distance) {
                    ...MinimalProfileFragment
                }
            }
            fragment MinimalProfileFragment on Profile {
                uuid
                location {
                    distance
                }
            }""";

    String ASSET_DIRECTORY = "../MapThing/src/assets";
}
