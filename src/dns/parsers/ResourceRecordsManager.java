package dns.parsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dns.rcodes.RR;


public class ResourceRecordsManager {
	
	Map<String, List<RR>> domains;
	
	 static ResourceRecordsManager instance = new ResourceRecordsManager();
	 
	 private List<RR> getInternalResourceRecords(String key){
		 if(domains==null){
			 domains=new HashMap<String, List<RR>>();
		 }
		 return domains.get(key);
	 }
	 
	 private void addInternalResourceRecords(String key, List<RR> value){
		 if(domains==null){
			 domains=new HashMap<String, List<RR>>();
		 }
		 domains.put(key,value);
	 }

	 public static List<RR> getResourceRecords(String key){
		 return instance.getInternalResourceRecords(key);
	 }
	 

	 public static void addResourceRecords(String key,List<RR> value){
		 instance.addInternalResourceRecords(key, value);
	 }
	 

	
}
