package be.thomaswinters.goofer.metrics.uni;

import be.thomaswinters.wordcounter.WordCounter;
import be.thomaswinters.goofer.metrics.IUniArgumentMetric;

import java.util.Optional;

public class OneGramWCMetric implements IUniArgumentMetric {
    private final WordCounter wc;
    private final String name;

    public OneGramWCMetric(WordCounter wc, String name) {
        this.wc = wc;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<? extends Object> rate(String value) {
        return Optional.of(Math.log(wc.getCount(value) + 1));
    }
}
