package com.tagger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class NERTagger {

	AbstractSequenceClassifier<CoreLabel>  classifier;
	
	public static void main(String[] args) throws ClassCastException, ClassNotFoundException, IOException{
		
		if(args.length<2){
			System.out.println("nertagger.jar <inputdir> <outputdir>");
		}
		
		NERTagger tagger = new NERTagger();
		tagger.createDirStructAndTag(args[0], args[1]);

	}
	
	public NERTagger() throws ClassCastException, ClassNotFoundException, IOException{
		
		this.classifier = CRFClassifier.getClassifier("classifiers/english.all.3class.distsim.crf.ser.gz");
	}
	
	private ArrayList<NERTag> getNERSegments(String text) throws ClassCastException, ClassNotFoundException, IOException {

	    String[] output = classifier.classifyToString(text, "tsv", false).split("\\n");
	    ArrayList<NERTag> tags = new ArrayList<NERTag>();
	    String token="";
	    String tag=""; 
	    for(String val: output){
	    	if(!val.isEmpty()){
	    		String[] tokens = val.split("\\t");
	    		if(tokens[1].contentEquals("O")){
	    			if(!token.isEmpty()){
	    				tags.add(new NERTag(token.toLowerCase(),tag));
	    				token = "";
	    			}
    				tags.add(new NERTag(tokens[0].toLowerCase(),"O"));
	    		} else if (token.isEmpty()){
	    			token = tokens[0];
	    			tag = tokens[1];
	    		} else {
	    			token = token + " " + tokens[0];
	    		}
	    	  }
	      }
	      
	    if(!token.isEmpty()){
	    	tags.add(new NERTag(token.toLowerCase(),tag));
	    }
	    
	    return tags;
	}
	
	private String readFileContents(File file){
		String content ="";
		
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			
			NodeList blockList = doc.getElementsByTagName("block");
			for (int temp = 0; temp < blockList.getLength(); temp++) {
				
				Node blockNode = blockList.item(temp);
				if (blockNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) blockNode;
					if(eElement.getAttribute("class").contentEquals("full_text")){
						NodeList textList = eElement.getElementsByTagName("p");
						for(int i=0;i< textList.getLength();i++){
							content = content + " " + textList.item(i).getTextContent();
						}
					}
				}
			}
			
	    } catch (Exception e) {
	       e.printStackTrace();
	    }
		
		return content;
	}
	
	public void saveTags(ArrayList<NERTag> tags,String filePath) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(filePath, "UTF-8");
		for(NERTag tag: tags){
			writer.println(tag.getTag()+","+tag.getToken());
		}
		writer.close();
		
	}
	
	public void createDirStructAndTag(String inputDirPath, String outputDirPath) throws ClassCastException, ClassNotFoundException, IOException{
		File[] files = new File(inputDirPath).listFiles();
		for (File file: files){
			if(file.isDirectory()){
				new File(outputDirPath+"/"+file.getName()).mkdir();
				createDirStructAndTag(inputDirPath+"/"+file.getName(),outputDirPath+"/"+file.getName());
			}
			else if(file.isFile() && file.getName().toLowerCase().endsWith(".xml")){
				this.saveTags(this.getNERSegments(this.readFileContents(file)),outputDirPath+"/"+file.getName().replaceAll("xml", "txt"));
				System.out.println("Tagged " + file.getAbsolutePath());
			}
		}
	}
}
