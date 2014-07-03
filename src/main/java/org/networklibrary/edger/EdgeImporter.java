package org.networklibrary.edger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.networklibrary.core.config.ConfigManager;
import org.networklibrary.core.parsing.Parser;
import org.networklibrary.core.parsing.ParsingErrorException;
import org.networklibrary.core.storage.StorageEngine;
import org.networklibrary.core.types.EdgeData;
import org.networklibrary.edger.parsing.EncodeParser;
import org.networklibrary.edger.parsing.MetaAnalysisParser;
import org.networklibrary.edger.parsing.MirTarBaseParser;
import org.networklibrary.edger.parsing.TabFileParser;
import org.networklibrary.edger.parsing.TargetScanParser;
import org.networklibrary.edger.parsing.TargetScanSitesParser;
import org.networklibrary.edger.parsing.TfeParser;
import org.networklibrary.edger.parsing.StringStitch.StitchActionsParser;
import org.networklibrary.edger.parsing.StringStitch.StitchChemChemParser;
import org.networklibrary.edger.parsing.StringStitch.StitchProteinChemParser;
import org.networklibrary.edger.parsing.StringStitch.StringActionParser;
import org.networklibrary.edger.parsing.StringStitch.StringLinkParser;
import org.networklibrary.edger.storage.EdgeStorageEngine;

public class EdgeImporter {

	protected static final Logger log = Logger.getLogger(EdgeImporter.class.getName());

	private static Map<String,Class> parsers = new HashMap<String,Class>();
	private static Map<String,String> supported = new HashMap<String,String>();
	static {
		addParser("STRING_Links","STRING Links",StringLinkParser.class);
		addParser("STRING_Actions","STRING Actions",StringActionParser.class);
		addParser("ENCODE","Encode Proximal or Distal",EncodeParser.class);
		addParser("MIRTARBASE","miRTarBase miRNA targeting", MirTarBaseParser.class);
		addParser("TARGETSCAN", "TargetScan (requires miR family file via -x)", TargetScanParser.class);
		addParser("TARGETSCANSITES", "TargetScan Conserved Sites import",TargetScanSitesParser.class);
		addParser("TFE","TFe import (requires a dummy filename) from the website", TfeParser.class);
		addParser("STITCHACTIONS", "STITCH Actions", StitchActionsParser.class);
		addParser("STITCHCHEMCHEM", "STITCH Chem Chem Interactions", StitchChemChemParser.class);
		addParser("STITCHPROTCHEM", "STITCh Protein Chem Interactions",StitchProteinChemParser.class);
		addParser("METAANALYSIS", "MetaAnalysis graph", MetaAnalysisParser.class);
		addParser("TAB","Tab files", TabFileParser.class);
		
	}

	private static void addParser(String cmd, String name, Class parser){
		parsers.put(cmd,parser);
		supported.put(cmd, name);
	}
	
	private String db;
	private String type;
	private List<String> fileLocs;
	private ConfigManager confMgr;
	private List<String> extras;
	private boolean noNew;

	public EdgeImporter(String db, String type, List<String> fileLocs,ConfigManager confMgr, List<String> extras, boolean noNew) {
		this.db = db;
		this.type = type;
		this.fileLocs = fileLocs;
		this.confMgr = confMgr;
		this.extras = extras;
		this.noNew = noNew;
	}

	public void execute() {

		log.info("connecting to db: " + getDb());

//		GraphDatabaseService g = new RestGraphDatabase(db);
		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(db);

		StorageEngine<EdgeData> se = new EdgeStorageEngine(g,confMgr,noNew);

		long start = System.nanoTime();

		for(String fileLoc : getFileLocations()){
			try {

				Parser<EdgeData> p = makeParser();

				if(p.hasExtraParameters())
					p.takeExtraParameters(extras);

				p.setDataSource(fileLoc);

				if(p != null){
					while(p.ready()){
						se.storeAll(p.parse());
					}
				}

				se.finishUp();

				long end = System.nanoTime();
				long elapsed = end - start;
				log.info("finished " + fileLoc + " in " + (elapsed/1000000000));
			} catch (ParsingErrorException e){
				log.severe("error during parsing of location="+fileLoc+ ": " + e.getMessage());
			}
		}
	}

	protected Parser<EdgeData> makeParser(){
		Parser<EdgeData> p = null;

		try {
			log.info("Have type = " + getType() + " -> parser = " + parsers.get(getType()));		
			p = (Parser<EdgeData>)getParsers().get(getType()).newInstance();
		} catch (InstantiationException e) {
			log.severe("InstantiationException when creating parser for: " + getType() + ": " + e.getMessage());
		} catch (IllegalAccessException e) {
			log.severe("IllegalAccessException when creating parser for: " + getType() + ": " + e.getMessage());
		}

		return p;
	}

	protected String getType() {
		return type;
	}

	protected List<String> getFileLocations() {
		return fileLocs;
	}

	protected String getDb() {
		return db;
	}

	public static String printSupportedTypes() {
		StringBuilder buff = new StringBuilder();

		for(Entry<String,Class> p : parsers.entrySet() ){
			buff.append("\t" + p.getKey() + " = " + supported.get(p.getKey()));
			buff.append("\n");
		}

		return buff.toString();
	}

	protected ConfigManager getConfMgr() {
		return confMgr;
	}

	protected Map<String,Class> getParsers(){
		return parsers;
	}
}
