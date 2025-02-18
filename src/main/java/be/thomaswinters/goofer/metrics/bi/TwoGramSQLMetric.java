package be.thomaswinters.goofer.metrics.bi;

import be.thomaswinters.goofer.metrics.util.NGramsSQLMetric;
import be.thomaswinters.goofer.metrics.IBiArgumentMetric;
import be.thomaswinters.googlengrams.NgramMySQLConnector;

import java.util.Arrays;
import java.util.Optional;

public class TwoGramSQLMetric extends NGramsSQLMetric implements IBiArgumentMetric {

    /*-********************************************-*
     *  Constructor
     *-********************************************-*/
    public TwoGramSQLMetric(NgramMySQLConnector nGramService) {
        super(nGramService);
    }
    /*-********************************************-*/

    /*-********************************************-*
     *  ISchemaMetric
     *-********************************************-*/

    @Override
    public String getName() {
        return "relative_frequency";
    }

    @Override
    public Optional<Object> rate(String word1, String word2) {
        double count = count(Arrays.asList(word1, word2));
//		if (count==0) {
//			System.out.println(word1+"+"+word2+" -> " + count);			
//		}
        if (count > 0) {
            return Optional.of(Math.log(count));
        }
        return Optional.of(count);
    }
    /*-********************************************-*/

}
