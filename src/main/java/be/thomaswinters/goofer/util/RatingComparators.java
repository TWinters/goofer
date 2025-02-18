package be.thomaswinters.goofer.util;

import be.thomaswinters.goofer.data.Rating;

import java.util.Comparator;

public class RatingComparators {
    public static final Comparator<Rating<?>> ASCENDING = new Comparator<Rating<?>>() {

        @Override
        public int compare(Rating<?> o1, Rating<?> o2) {
            return (int) Math.signum(o1.getRating() - o2.getRating());
        }
    };
    public static final Comparator<Rating<?>> DESCENDING = ASCENDING.reversed();
}
