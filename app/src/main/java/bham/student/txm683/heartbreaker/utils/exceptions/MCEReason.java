package bham.student.txm683.heartbreaker.utils.exceptions;

public enum MCEReason {
    //Used when the requested map file cannot be found
    FILE_DECODING,
    //Used when a door doesn't have strictly two clear tiles on opposite sides
    INVALID_DOOR_LOCATION,
    //used when searchForTileType is called with a color constant
    //that isn't defined in TileType
    SEARCH_FOR_INVALID_TILE_TYPE
}
