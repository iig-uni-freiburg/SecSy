package de.uni.freiburg.iig.telematik.secsy.logic.test;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.invation.code.toval.misc.ListUtils;
import de.invation.code.toval.misc.ListUtils.Partition;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;



public class Test {
	

	public static void main(String[] args) throws Exception{
//		ArrayList<String> activities = new ArrayList<String>(Arrays.asList("A","B","C","D","E","F","G"));
//		ArrayList<String> originators = new ArrayList<String>(Arrays.asList("Gerd","Heinz","Manfred"));
//		ArrayList<String> attributes = new ArrayList<String>(Arrays.asList("D1","D2","D3"));
//		ArrayList<String> roles = new ArrayList<String>(Arrays.asList("Admin","Clerk","Poster"));
//		Context c = new Context(activities);
//		c.setOriginators(originators);
//		c.setRoles(roles);
//		c.setRoleMembership("Admin", Arrays.asList("Gerd"));
//		c.setRoleMembership("Clerk", Arrays.asList("Heinz","Manfred"));
//		c.setRoleMembership("Poster", Arrays.asList("Manfred"));
//		c.setRolePermission("Admin", activities);
//		c.setRolePermission("Clerk", Arrays.asList("C","F"));
//		c.setRolePermission("Poster", Arrays.asList("A"));
//		System.out.println(c.getRolesFor("Manfred"));
//		c.setRoleMembership("Clerk", Arrays.asList("Heinz"));
//		System.out.println(c.authorizedOriginatorsFor("A"));
//		c.setAttributes(attributes);
//		c.setInputAttributesFor("A", Arrays.asList("D1"));
//		System.out.println(c.getInputAttributesFor("A"));
//		List<Partition<String>> ret = getBiPartitions(Arrays.asList("1","2","3","4","5"),3);
//		for(Partition<String> part: ret)
//			System.out.println(part);
//		List<Partition<String>> partitions = ListUtils.getPartitions(Arrays.asList("1","2","3","4","5"), 2,3);
//		for(Partition<String> part: partitions)
//			System.out.println(part.getSubset(0) + " " + part.getSubset(1));
//		ListPermutations<String> it = ListUtils.getPermutations(Arrays.asList("1","2","3","4","5"));
//		while(it.hasNext())
//			System.out.println(it.next());
		
//		MXMLSequentializerSort.convertMXML("/Users/ts552/Documents/Prozesse/Modelle und Logs von Bose/cpnToolsSimulationLog.mxml");
		
//		A a = new A();
//		B b = new B();
//		b.setName("b1");
//		B b2 = new B();
//		b2.setName("b2");
//		C c = new C();
//		
//		a.setB("eins", b);
//		c.setB(a.getB("eins"));
//		System.out.println(c.getB());
//		
//		B bn = a.getB("eins");
////		bn.takeover(b2);
//		a.setB("eins", b2);
//		System.out.println(c.getB());
//		
////		a.setB("eins", b2);
//		System.out.println(c.getB());
		
		
	}
	
	private static void test(String s1){
		String s = new String();
	}
	
	public static void testLocking() throws ParameterException{
		SimulationLogEntry entry = new SimulationLogEntry("A");
		try {
//			System.out.println(entry.setActivity("B"));
////			entry.lockField(EntryField.ACTIVITY);
//			System.out.println(entry.setActivity("C"));
//			System.out.println(entry.setActivity("B"));
//			
//			Date date1 = new Date(System.currentTimeMillis()-1000);
//			System.out.println(entry.setTimestamp(date1));
////			entry.lockField(EntryField.TIME);
//			System.out.println(entry.setTimestamp(new Date()));
//			System.out.println(entry.setTimestamp(date1));
//			
//			List<DataAttribute> list1 = new ArrayList<DataAttribute>(Arrays.asList(new DataAttribute("A"),new DataAttribute("B")));
//			List<DataAttribute> list2 = new ArrayList<DataAttribute>(Arrays.asList(new DataAttribute("C"),new DataAttribute("D")));
//			System.out.println(entry.setOutputData(list1));
////			entry.lockField(EntryField.OUTPUT_DATA);
//			System.out.println(entry.addOutputData(new DataAttribute("C")));
//			System.out.println(entry.setOutputData(list2));
//			System.out.println(entry.setOutputData(list1));
			entry.setOriginatorCandidates(Arrays.asList("Gerd","Olaf"));
			entry.setOriginatorCandidate("Gerd");
			System.out.println(entry.getOriginatorCandidates());
		} catch (LockingException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static List<Partition<String>> getBiPartitions(List<String> input, int sizeOfFirstPartition){
		List<Partition<String>> result = new ArrayList<Partition<String>>();
		boolean reverse = input.size()-sizeOfFirstPartition<sizeOfFirstPartition;
		List<List<String>> r = rek(input, 0, input.size()-1-sizeOfFirstPartition, Math.min(sizeOfFirstPartition, input.size()-sizeOfFirstPartition)-1,"");
		for(List<String> l: r){
			Partition<String> part = new Partition<String>(input);
			if(reverse){
				part.addSubset(ListUtils.getListWithout(input, l));
				part.addSubset(l);
			} else {
				part.addSubset(l);
				part.addSubset(ListUtils.getListWithout(input, l));
			}
			result.add(part);
		}
		return result;
	}
	
	public static List<List<String>> rek(List<String> input, int startIndex, int endIndex, int number, String header){
		System.out.println(header+"call("+startIndex+", "+endIndex+", "+number+")");
		System.out.println(header+"start: "+startIndex+", end: "+endIndex+", number: "+number);
//		System.out.println(input + " " + itemIndex);
		List<List<String>> result = new ArrayList<List<String>>();
		
			if(number==0){
				System.out.println(header+"return trivial result");
				for(int i=startIndex; i<input.size(); i++){
					List<String> newList = new ArrayList<String>();
					newList.add(input.get(i));
					result.add(newList);
					System.out.println(header+"add: "+newList);
				}
			} else {
				//
				System.out.println(header+"go deeper");
				for(int i=startIndex; i<=endIndex; i++){
					System.out.println(header+"i = "+i);
					String head = input.get(i);
					System.out.println(header+"head: "+head);
						List<List<String>> rekResult = rek(input, i+1, endIndex+1, number-1, header+"   ");
						for(List<String> list: rekResult){
							List<String> newList = new ArrayList<String>(Collections.singletonList(head));
							newList.addAll(list);
							result.add(newList);
							System.out.println(header+"add: "+newList);
						}
				}
			}

		System.out.println(header+"return "+result);
		return result;		
	}
	
	

}