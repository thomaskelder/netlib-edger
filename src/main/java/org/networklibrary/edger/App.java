package org.networklibrary.edger;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.networklibrary.core.config.ConfigManager;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Options options = new Options();
    	Option help = OptionBuilder.withDescription("Help message").create("help");
    	Option dbop = OptionBuilder.withArgName("[URL]").hasArg().withDescription("Neo4j instance to prime").withLongOpt("target").withType(String.class).create("db");
    	Option typeop = OptionBuilder.withArgName("[TYPE]").hasArg().withDescription("Types available:").withType(String.class).create("t");
    	Option configOp = OptionBuilder.hasArg().withDescription("Alternative config file").withLongOpt("config").withType(String.class).create("c");
    	
    	options.addOption(help);
    	options.addOption(dbop);
    	options.addOption(typeop);
    	options.addOption(configOp);
    	
    	
    	CommandLineParser parser = new GnuParser();
        try {
            
            CommandLine line = parser.parse( options, args );
            
            if(line.hasOption("help") || args.length == 0){
            	HelpFormatter formatter = new HelpFormatter();
            	formatter.printHelp( "netlib-edger [OPTIONS] [FILE]", options );
            	System.out.println(EdgeImporter.printSupportedTypes());
            	return;
            }
            
            String db = null;
            if(line.hasOption("db")){
            	db = line.getOptionValue("db");
            }
            
            String type = null;
            if(line.hasOption("t")){
            	type = line.getOptionValue("t");
            }
            
            String config = null;
            if(line.hasOption("c")){
            	config = line.getOptionValue("c");
            }
            
            List<String> inputFiles = line.getArgList();
            
            if(inputFiles.size() != 1){
            	System.err.println("No or too many input files provided!");
            	return;
            }
            
            // eeesh should move that to the ConfigManager ctor
            ConfigManager confMgr = null;
            if(config != null){
            	confMgr = new ConfigManager(config);
            }
            else {
            	confMgr = new ConfigManager();
            }
            
            EdgeImporter ei = new EdgeImporter(db,type,inputFiles.get(0),confMgr);
            
            try {
				ei.execute();
			} catch (IOException e) {
				System.err.println("parsing failed" + e.getMessage());
				e.printStackTrace();
			}
            
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
    }
}