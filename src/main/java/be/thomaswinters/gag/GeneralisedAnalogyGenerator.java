package be.thomaswinters.gag;

import be.thomaswinters.gag.humanevaluation.jokejudger.JokeJudgerDataParser;
import com.beust.jcommander.JCommander;
import be.thomaswinters.gag.template.AnalogyTemplateStripper;
import be.thomaswinters.goofer.classifier.JokeClassifier;
import be.thomaswinters.goofer.data.MultiRating;
import be.thomaswinters.goofer.data.Rating;
import be.thomaswinters.goofer.data.TemplateValues;
import be.thomaswinters.goofer.generators.PartialTemplateValues;
import be.thomaswinters.goofer.knowledgebase.TemplateKnowledgeBase;
import be.thomaswinters.goofer.outputfilter.SimilarValuesFilter;
import be.thomaswinters.goofer.util.RatingComparators;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * The Generalised Analogy Generator (GAG). Based on the Goofer framework.
 *
 * @author Thomas Winters
 */
public class GeneralisedAnalogyGenerator {
    /*-********************************************-*
     *  Instance variables
     *-********************************************-*/
    // ARGUMENTS
    private final GagArguments arguments;

    // TEMPLATE VALUES SPLITTER
    // private final ITemplateValuesProcessor templateValuesProcessor;

    // TOTAL KNOWLEDGEBASE
    private final TemplateKnowledgeBase knowledgebase;

    /*-********************************************-*/
    private final DecimalFormat df = new DecimalFormat("#.#######");

    /*-********************************************-*
     *  Generator
     *-********************************************-*/

    /*-********************************************-*
     *  Constructor
     *-********************************************-*/
    public GeneralisedAnalogyGenerator(GagArguments arguments)
            throws IOException, ClassNotFoundException, URISyntaxException, SQLException {
        System.out.println("Initialising GAG");
        // SET ARGUMENTS
        this.arguments = arguments;

        System.out.println("Creating Knowledgebase");
        // Create knowledgebase
        knowledgebase = new GagKnowledgeBaseBuilder(arguments).createKnowledgeBase();
    }

    public static void main(String[] args) throws Exception {
        // Parse arguments
        GagArguments gagArguments = new GagArguments();
        JCommander.newBuilder().addObject(gagArguments).build().parse(args);

        // Create generator
        GeneralisedAnalogyGenerator gag = new GeneralisedAnalogyGenerator(gagArguments);

        // Generate
        try {
            gag.generateAnalogies(new PartialTemplateValues(
                    Arrays.asList(gagArguments.getX(), gagArguments.getY(), gagArguments.getZ())));
        } catch (OutOfMemoryError e) {
            System.out.println("Out of memory.");
        }
    }

    /*-********************************************-*/

    public void generateAnalogies(PartialTemplateValues partialTemplate) throws Exception {

        // Get the input jokes
        System.out.println("Reading input jokes");
        JokeJudgerDataParser dataParser = new JokeJudgerDataParser();
        List<MultiRating<TemplateValues>> ratedTemplateValues = new ArrayList<>();
        for (URL file : arguments.getDataInput()) {
            ratedTemplateValues.addAll(dataParser.parse(file));
        }

        // Process the values
        System.out.println("Processing input jokes");
        ratedTemplateValues = knowledgebase.getTemplatevaluesProcessor().process(ratedTemplateValues);

        // Create the classifier based on the knowledgebase
        System.out.println("Training classifier");
        JokeClassifier jc = new JokeClassifier(arguments.getClassifier(), AnalogyTemplateStripper.TEMPLATE,
                ratedTemplateValues, knowledgebase.getSchemaMetrics(), arguments.getModelOutputFile());

        // Generate rated template values
        System.out.println("Generating jokes for template values " + partialTemplate);

        Stream<Rating<TemplateValues>> generations = knowledgebase.getValueGenerator().generateStream(partialTemplate)
                // Classify
                .map(jc::classify)
                // Filter: above threshold!
                .filter(e -> e.getRating() >= arguments.getRatingThreshold());

        // Sort on ratings if required
        if (arguments.isSortOnRating()) {
            generations = generations.sorted(RatingComparators.DESCENDING);
        }

        // Filter on max similarity
        if (arguments.getMaxSimilarity().isPresent()) {
            SimilarValuesFilter filter = new SimilarValuesFilter(arguments.getMaxSimilarity().get());
            generations = generations.filter(e -> filter.isDifferentEnough(e.getElement()));
        }

        // Write to file
        FileOutputStream fos = new FileOutputStream(arguments.getGenerationsOutputFile());
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        generations.map(e -> df.format(e.getRating()) + "\t"
                + AnalogyTemplateStripper.TEMPLATE.apply(e.getElement())).forEach(e -> {
            System.out.println(e);
            try {
                bw.write(e + "\n");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        bw.close();

        // Files.write(result, outputFile, Charsets.UTF_8);
        System.out.println("Wrote ratings to " + arguments.getGenerationsOutputFile());
    }

}
