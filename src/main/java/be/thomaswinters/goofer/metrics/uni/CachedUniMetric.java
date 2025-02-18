package be.thomaswinters.goofer.metrics.uni;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import be.thomaswinters.goofer.metrics.IUniArgumentMetric;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CachedUniMetric implements IUniArgumentMetric {

    private static final int DEFAULT_CACHE_SIZE = 10;

    private final IUniArgumentMetric metric;
    private final Cache<String, Optional<? extends Object>> cache;

    public CachedUniMetric(IUniArgumentMetric metric, int cacheSize) {
        this.metric = metric;
        this.cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build();
    }

    public CachedUniMetric(IUniArgumentMetric metric) {
        this(metric, DEFAULT_CACHE_SIZE);
    }

    @Override
    public String getName() {
        return metric.getName();
    }

    @Override
    public Optional<? extends Object> rate(String value) {
        try {
            return cache.get(value, () -> metric.rate(value));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

}
