package com.jboss.examples.brms;

import java.util.List;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Results;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.ScoreCardConfiguration;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;


public class PMMLExecutionTest {

	
	public static void main(String[] args) {

		try {
			System.out.println("READING SCORECARD FROM XLS FILE");
			System.out.println(testXLSWithExecution());
			System.out.println("READING SCORECARD FROM PMML FILE");
			System.out.println(testPMMLWithExecution());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String testXLSWithExecution() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
        scconf.setWorksheetName( "scorecards" );
        kbuilder.add( ResourceFactory.newUrlResource(PMMLExecutionTest.class.getResource("/scoremodel_c.xls")),
            ResourceType.SCARD,
            scconf );
        System.out.println("XLS Score card translated.");
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
                
        FactType scorecardType = kbase.getFactType( "org.drools.scorecards.example","SampleScore" );
        
        List <FactField> fields = scorecardType.getFields();
        System.out.println("FACT FIELDS:");
        for (FactField f : fields){
        	System.out.println("name: " + f.getName() + " type: " + f.getType().toString());
        }
        
               
        Object scorecard =  scorecardType.newInstance();
        
        scorecardType.set(scorecard, "age", 10);
        session.insert( scorecard );
        session.fireAllRules();
        session.dispose();
        //occupation = 5, age = 25, validLicense -1
        return "RESULT: " + scorecardType.get( scorecard, "scorecard__calculatedScore" ).toString();

    }
	
	private static String testPMMLWithExecution() throws Exception {

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write( ks.getResources().newUrlResource(PMMLExecutionTest.class.getResource( "/SimpleScorecard.pmml" ) )
                           .setSourcePath( "/SimpleScorecard.pmml" )
                           .setResourceType( ResourceType.PMML ) );
        System.out.println("PMML Score card translated.");
        KieBuilder kieBuilder = ks.newKieBuilder( kfs );

        Results res = kieBuilder.buildAll().getResults();
        KieContainer kieContainer = ks.newKieContainer( kieBuilder.getKieModule().getReleaseId() );

        KieBase kbase = kieContainer.getKieBase();
        KieSession session = kbase.newKieSession();

        FactType scorecardType = kbase.getFactType( "org.drools.scorecards.example","SampleScore" );
        List <FactField> fields = scorecardType.getFields();
        System.out.println("FACT FIELDS:");
        for (FactField f : fields){
        	System.out.println("name: " + f.getName() + " type: " + f.getType().toString());
        }

        Object scorecard = scorecardType.newInstance();
        

        scorecardType.set(scorecard, "age", 10);
        session.insert( scorecard );
        session.fireAllRules();
        session.dispose();
        //occupation = 5, age = 25, validLicence -1
        return "RESULT: " + scorecardType.get( scorecard, "scorecard_calculatedScore" ).toString();

    }


}
