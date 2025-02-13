import abduction.AbductionReasoning;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import io.PelletExplanation;
import ontology.OntologyLoader;
import ontology.QueryParser;
import org.semanticweb.owlapi.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class AbductionManager {

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLException {
        // Record the start time
        long startTime = System.currentTimeMillis();

        // Create a scanner for console input
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for the input file path
        System.out.print("Enter the path to the input file: ");
        String inputPath = scanner.nextLine();

        // Read the input file
        List<String> lines = Files.readAllLines(Paths.get(inputPath));

        // Extract ontology path and namespace (appears only once)
        String localOntologyPath = lines.get(0).split("=")[1].trim();
        String ns = lines.get(1).split("=")[1].trim();

        // Prepare output file path in resources folder
        String outputFilePath = "src/main/resources/query_results.txt";
        Files.write(Paths.get(outputFilePath), "Query Results:\n\n".getBytes());

        // Iterate over the lines and process the queries
        int i = 2;  // Start after the ontology path and namespace lines
        while (i < lines.size()) {
            // Skip empty lines
            if (lines.get(i).trim().isEmpty()) {
                i++;
                continue;
            }

            // Extract query, individual with explanation, and individual without explanation
            String queryStr = lines.get(i).split("=")[1].trim();
            String individualWithExplan = lines.get(i + 1).split("=")[1].trim();
            String individualWithoutExplan = lines.get(i + 2).split("=")[1].trim();

            // Process each query
            Set<OWLAxiom> missingAxioms = processQuery(ns, localOntologyPath, queryStr, individualWithExplan, individualWithoutExplan);

            // Save results to file
            String result = "Query: " + queryStr + "\nIndividual with Explanation: " + individualWithExplan +
                    "\nMissing Individual: " + individualWithoutExplan +
                    "\nMissing Axioms: " + missingAxioms + "\n\n";
            Files.write(Paths.get(outputFilePath), result.getBytes(), StandardOpenOption.APPEND);

            // Skip the next 3 lines for the current query (query, individualWithExplan, individualWithoutExplan)
            i += 4;
        }

        // Record the end time
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");

        // Close the scanner
        scanner.close();
    }

    public static Set<OWLAxiom> processQuery(String ns, String localOntologyPath, String queryStr, String individualWithExplan, String individualWithoutExplan) throws OWLOntologyCreationException, OWLException, IOException {
        PelletExplanation.setup();
        OntologyLoader ontologyLoader = new OntologyLoader();
        QueryParser queryParser = new QueryParser();
        AbductionReasoning abductionReasoning = new AbductionReasoning();

        OWLOntology ontology = ontologyLoader.loadOntology(localOntologyPath);
        PelletReasoner reasoner = ontologyLoader.getReasoner(ontology);
        PelletExplanation expGen = new PelletExplanation(reasoner);

        // Retrieve individuals from ontology
        OWLNamedIndividual owlIndividualWithExplan = ontologyLoader.getIndividualByName(ontology, individualWithExplan);
        OWLNamedIndividual owlIndividualWithoutExplan = ontologyLoader.getIndividualByName(ontology, individualWithoutExplan);
        OWLClassExpression query = queryParser.parseQueryString(ns, queryStr);

        // Get instances of the query
        Set<OWLNamedIndividual> individuals = ontologyLoader.getIndividualsForQuery(reasoner, query);

        // Process the individuals
        for (OWLNamedIndividual selectedIndividual : individuals) {
            if (selectedIndividual.equals(owlIndividualWithExplan)) {
                Set<Set<OWLAxiom>> explanations = expGen.getInstanceExplanations(selectedIndividual, query, 3);
                Set<Set<OWLAxiom>> allCommonAxiomsSet = abductionReasoning.findCommonAxioms(explanations, ontology);

                for (Set<OWLAxiom> commonAxiomSet : allCommonAxiomsSet) {
                    // Apply abduction reasoning and return missing axioms
                    return abductionReasoning.applyAbductionForIndividuals(ontology, owlIndividualWithExplan, owlIndividualWithoutExplan, commonAxiomSet);
                }
            }
        }

        return Collections.emptySet(); // Return empty if no explanation found
    }
}
