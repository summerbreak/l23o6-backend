package org.fffd.l23o6.util.strategy.train;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nullable;


public class GSeriesSeatStrategy extends TrainSeatStrategy {
    public static final GSeriesSeatStrategy INSTANCE = new GSeriesSeatStrategy();

    private final Map<Integer, String> BUSINESS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> FIRST_CLASS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> SECOND_CLASS_SEAT_MAP = new HashMap<>();

    private final Map<GSeriesSeatType, Map<Integer, String>> TYPE_MAP = new HashMap<>() {{
        put(GSeriesSeatType.BUSINESS_SEAT, BUSINESS_SEAT_MAP);
        put(GSeriesSeatType.FIRST_CLASS_SEAT, FIRST_CLASS_SEAT_MAP);
        put(GSeriesSeatType.SECOND_CLASS_SEAT, SECOND_CLASS_SEAT_MAP);
    }};

    private GSeriesSeatStrategy() {

        int counter = 0;

        for (String s : Arrays.asList("1车1A", "1车1C", "1车1F")) {
            BUSINESS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("2车1A", "2车1C", "2车1D", "2车1F", "2车2A", "2车2C", "2车2D", "2车2F", "3车1A", "3车1C", "3车1D", "3车1F")) {
            FIRST_CLASS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("4车1A", "4车1B", "4车1C", "4车1D", "4车2F", "4车2A", "4车2B", "4车2C", "4车2D", "4车2F", "4车3A", "4车3B", "4车3C", "4车3D", "4车3F")) {
            SECOND_CLASS_SEAT_MAP.put(counter++, s);
        }

    }

    public enum GSeriesSeatType implements SeatType {
        BUSINESS_SEAT("商务座"), FIRST_CLASS_SEAT("一等座"), SECOND_CLASS_SEAT("二等座"), NO_SEAT("无座");
        private String text;

        GSeriesSeatType(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public static GSeriesSeatType fromString(String text) {
            for (GSeriesSeatType b : GSeriesSeatType.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }


    public @Nullable String allocSeat(int startStationIndex, int endStationIndex, GSeriesSeatType type, boolean[][] seatMap) {
        //endStationIndex - 1 = upper bound
        // TODO

        if (type == GSeriesSeatType.NO_SEAT) {
            return GSeriesSeatType.NO_SEAT.getText(); // No seat is allocated for NO_SEAT type
        }
        int seatCount = seatMap[0].length;

        int start = 0;
        int end = 0;
        switch (type) {
            case BUSINESS_SEAT:
                end = BUSINESS_SEAT_MAP.size();
                break;
            case FIRST_CLASS_SEAT:
                start = BUSINESS_SEAT_MAP.size();
                end = BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size();
                break;
            case SECOND_CLASS_SEAT:
                start = BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size();
                end = BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size() + SECOND_CLASS_SEAT_MAP.size();
                break;
        }

        boolean flag = true;
        for (int j = start; j < end; j++) {
            for (int i = startStationIndex; i < endStationIndex; i++) {
                if (seatMap[i][j]) {
                    // If the seat is not available, can't allocate it
                    flag = false;
                    break;
                }
            }
            if (flag) {
                for (int i = startStationIndex; i < endStationIndex; i++) {
                    seatMap[i][j] = true;
                }
                return TYPE_MAP.get(type).get(j); // Return the seat identifier
            } else {
                flag = true;
            }
        }

        // If no available seat is found, return null
        return null;
    }

    public void freeSeat(int startStationIndex, int endStationIndex, boolean[][] seatMap, String seatName) {
        // used in 'cancelOrders'
        if (seatName.equals(GSeriesSeatType.NO_SEAT.getText())) {
            return;
        }

        int j = 0;
        for (int i = 0; i < GSeriesSeatType.values().length; i++) {
            Map<Integer, String> seatNameMap = TYPE_MAP.get(GSeriesSeatType.values()[i]);
            for (Map.Entry<Integer, String> entry : seatNameMap.entrySet()) {
                if (entry.getValue().equals(seatName)) {
                    j = entry.getKey();
                }
            }
        }

        for (int i = startStationIndex; i < endStationIndex; i++) {
            seatMap[i][j] = false;
        }
    }

    public Map<GSeriesSeatType, Integer> getLeftSeatCount(int startStationIndex, int endStationIndex, boolean[][] seatMap) {
        // TODO
        int seatCount = seatMap[0].length;
        Map<GSeriesSeatType, Integer> leftSeatCountMap = new HashMap<>();

        for (GSeriesSeatType type : GSeriesSeatType.values()) {
            if (type != GSeriesSeatType.NO_SEAT) {
                int leftCount = 0;
                for (int j = 0; j < seatCount; j++) {
                    boolean flag = true;
                    for (int i = startStationIndex; i < endStationIndex; i++) {
                        if (seatMap[i][j]) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag && TYPE_MAP.get(type).containsKey(j)) {
                        // Count the available seats of the specified type
                        leftCount++;
                    }
                }
                leftSeatCountMap.put(type, leftCount);
            }
        }
        return leftSeatCountMap;
    }

    public boolean[][] initSeatMap(int stationCount) {
        return new boolean[stationCount - 1][BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size() + SECOND_CLASS_SEAT_MAP.size()];
    }
}
