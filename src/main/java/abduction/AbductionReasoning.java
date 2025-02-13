package abduction;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AbductionReasoning {
    public static Set<Set<OWLAxiom>> findCommonAxioms(Set<Set<OWLAxiom>> explanationsSet, OWLOntology ontology) {
        Set<OWLAxiom> aBoxAxioms = ontology.getABoxAxioms(Imports.INCLUDED);
        Set<Set<OWLAxiom>> commonAxiomsSet = new HashSet<>();


        for (Set<OWLAxiom> explanationAxioms : explanationsSet) {
            Set<OWLAxiom> commonAxioms = new HashSet<>();

            for (OWLAxiom explanationAxiom : explanationAxioms) {
                {
                    if (aBoxAxioms.contains(explanationAxiom)) {
                        commonAxioms.add(explanationAxiom);
                    }
                }
            }
            commonAxiomsSet.add(commonAxioms);
        }

        return commonAxiomsSet;
    }

   /* public void applyAbductionForIndividuals(OWLOntology ontology, Set<Set<OWLAxiom>> explanations, OWLNamedIndividual individualWithExplan, OWLNamedIndividual individualWithoutExplan, Set<OWLAxiom> commonAxioms) {

        OWLOntology ontology1 = ontology;

        // Modify axioms for the first individual
        Set<OWLAxiom> axiomsForFirstIndividual = new HashSet<>();
        replaceIndividualInAxiom(commonAxioms, individualWithExplan, individualWithoutExplan, axiomsForFirstIndividual);

        // Check for missing axioms
        Set<OWLAxiom> missingAxioms = new HashSet<>();
        Set<OWLAxiom> aBoxAxioms = ontology.getABoxAxioms(Imports.INCLUDED);

        for (OWLAxiom axiom : axiomsForFirstIndividual) {
            if (!aBoxAxioms.contains(axiom)) {
                missingAxioms.add(axiom);
            }
        }

        // Add missing axioms to the ontology
        OWLOntologyManager manager = ontology1.getOWLOntologyManager();

        System.out.println("Missing axioms for " + individualWithoutExplan + ": " + missingAxioms);
    }*/

    public Set<OWLAxiom> applyAbductionForIndividuals(
            OWLOntology ontology,
            //Set<Set<OWLAxiom>> explanations,
            OWLNamedIndividual individualWithExplan,
            OWLNamedIndividual individualWithoutExplan,
            Set<OWLAxiom> commonAxioms) {

        // Create a modified set of axioms for the new individual
        Set<OWLAxiom> axiomsForNewIndividual = new HashSet<>();

        // Replace the original individual with the new one (dynamic mapping)
        replaceIndividualInAxiom(commonAxioms, individualWithExplan, individualWithoutExplan, axiomsForNewIndividual);

        // Find missing axioms not present in the current ontology
        Set<OWLAxiom> missingAxioms = new HashSet<>();
        Set<OWLAxiom> aBoxAxioms = ontology.getABoxAxioms(Imports.INCLUDED);

        for (OWLAxiom axiom : axiomsForNewIndividual) {
            if (!aBoxAxioms.contains(axiom)) {
                missingAxioms.add(axiom);
            }
        }

        // Add missing axioms to the ontology
      //  OWLOntologyManager manager = ontology.getOWLOntologyManager();
       // manager.addAxioms(ontology, missingAxioms);

        System.out.println("Missing axioms for " + individualWithoutExplan + ": " + missingAxioms);
        return missingAxioms;
    }


/*    private void replaceIndividualInAxiom(Set<OWLAxiom> commonAxioms, OWLNamedIndividual original, OWLNamedIndividual replacement, Set<OWLAxiom> modifiedAxioms) {
        // Get the OWLDataFactory from the ontology manager
        OWLOntologyManager owlmanager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = owlmanager.getOWLDataFactory();
        OWLNamedIndividual fixedVar = dataFactory.getOWLNamedIndividual(IRI.create("http://example.org/xyz"));

        // Iterate over the axioms in commonAxioms
        for (OWLAxiom axiom : commonAxioms) {
            // Visit the axiom and replace the individual where needed
            axiom.accept(new OWLObjectVisitorAdapter() {

                @Override
                public void visit(OWLClassAssertionAxiom axiom) {
                    if (axiom.getIndividual().equals(original)) {
                        OWLAxiom newAxiom = dataFactory.getOWLClassAssertionAxiom(axiom.getClassExpression(), replacement);
                        modifiedAxioms.add(newAxiom);
                    }
                }

                // Handle OWL Object Property Assertion Axiom
                @Override
                public void visit(OWLObjectPropertyAssertionAxiom axiom) {
                    if (axiom.getSubject().equals(original)) {
                        if (axiom.getObject().isNamed()) {
                            OWLNamedIndividual objectInd = axiom.getObject().asOWLNamedIndividual();

                            // Replace subject with replacement and object with fixedVar
                            OWLAxiom newAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(axiom.getProperty(), replacement, fixedVar);
                            modifiedAxioms.add(newAxiom);

                            // Now, look for the class assertion involving the object (ML in this case)
                            for (OWLAxiom classAxiom : commonAxioms) {
                                if (classAxiom instanceof OWLClassAssertionAxiom) {
                                    OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) classAxiom;
                                    if (classAssertionAxiom.getIndividual().equals(objectInd)) {
                                        // If the object individual is of type "AI"
                                        // OWLClassAssertionAxiom newClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(dataFactory.getOWLClass(IRI.create("http://www.semanticweb.org/CEX-Paper#AI")), fixedVar);
                                        OWLAxiom newAxi = dataFactory.getOWLClassAssertionAxiom(((OWLClassAssertionAxiom) classAxiom).getClassExpression(), fixedVar);
                                        modifiedAxioms.add(newAxi);
                                    }
                                }
                            }
                        }
                    }
                }


            });
        }
    }*/


    private void replaceIndividualInAxiom(Set<OWLAxiom> commonAxioms,
                                          OWLNamedIndividual original,
                                          OWLNamedIndividual replacement,
                                          Set<OWLAxiom> modifiedAxioms) {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        // Counter for generating unique placeholder names
        Map<OWLNamedIndividual, OWLNamedIndividual> individualMappings = new HashMap<>();
        int[] counter = {1};  // Array used for mutable counter in lambda

        // Iterate over the axioms in commonAxioms
        for (OWLAxiom axiom : commonAxioms) {
            axiom.accept(new OWLObjectVisitorAdapter() {

                @Override
                public void visit(OWLClassAssertionAxiom axiom) {
                    OWLNamedIndividual individual = axiom.getIndividual().asOWLNamedIndividual();

                    // Replace 'original' with 'replacement' (Alice → Bob)
                    if (individual.equals(original)) {
                        modifiedAxioms.add(dataFactory.getOWLClassAssertionAxiom(axiom.getClassExpression(), replacement));
                    }
                    // Replace other individuals with unique placeholders
                    else {
                        OWLNamedIndividual placeholder = individualMappings.computeIfAbsent(
                                individual,
                                key -> dataFactory.getOWLNamedIndividual(IRI.create("http://example.org/ind" + counter[0]++))
                        );
                        modifiedAxioms.add(dataFactory.getOWLClassAssertionAxiom(axiom.getClassExpression(), placeholder));
                    }
                }

                @Override
                public void visit(OWLObjectPropertyAssertionAxiom axiom) {
                    OWLNamedIndividual subject = axiom.getSubject().asOWLNamedIndividual();
                    OWLNamedIndividual object = axiom.getObject().asOWLNamedIndividual();

                    // Replace 'original' in subject with 'replacement' (Alice → Bob)
                    OWLNamedIndividual newSubject = subject.equals(original) ? replacement : subject;

                    // Replace other individuals in object with unique placeholders
                    OWLNamedIndividual newObject = individualMappings.computeIfAbsent(
                            object,
                            key -> dataFactory.getOWLNamedIndividual(IRI.create("http://example.org/ind" + counter[0]++))
                    );

                    modifiedAxioms.add(dataFactory.getOWLObjectPropertyAssertionAxiom(axiom.getProperty(), newSubject, newObject));
                }
            });
        }
    }

}
