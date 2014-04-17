package org.networklibrary.edger.parsing.StringStitch;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.networklibrary.core.parsing.Parser;
import org.networklibrary.core.types.EdgeData;

public class StitchActionsParser implements Parser<EdgeData> {
public static String SOURCE_NAME = "stitch_actions";
	
	private List<String> columns = null;
	
	public StitchActionsParser(){
	}
	
	public Collection<EdgeData> parse(String line) {
		List<EdgeData> res = null;
		
		if(!line.isEmpty()){
			res = new LinkedList<EdgeData>();
			
			String[] values = line.split("\\s",-1);
			
			if(values.length != columns.size()){
				throw new IllegalArgumentException("number of elements in row does not match number of columns " + line);
			}
			
//			item_id_a       item_id_b       mode    action  a_is_acting     score
			Map<String,Object> props = new HashMap<String,Object>();
			
			if(!values[3].isEmpty()){
				props.put("action", values[3]);
			}
			
			props.put("score", Integer.parseInt(values[5]));
			
			props.put("data_source",SOURCE_NAME);
			String from = values[0];
			String to = values[1];
			
			if(from.contains("9606.")){
				from = from.replace("9606.","");
			}
			if(to.contains("9606.")){
				to = to.replace("9606.","");
			}
			
			res.add(new EdgeData(from,to,values[2],props));
			if("0".equals(values[4])){
				res.add(new EdgeData(to,from,values[2],props));
			}
			
		}
		
		return res;
	}

	public void parseHeader(String header) {
		columns = Arrays.asList(header.split("\\t",-1));
	}

	public boolean hasHeader() {
		return true;
	}
}