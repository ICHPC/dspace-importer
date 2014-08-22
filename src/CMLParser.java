import java.io.*;
import javax.xml.parsers.*; 
 
import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;  

import java.io.File;
import java.io.IOException;

import org.w3c.dom.*;

public class CMLParser extends Parser {

/* Extract <scalar dictRef="gauss:XXX">VAL</scalar> fields */

private class Foo {
	String key;
	boolean global;
	boolean local;
	Foo ( String s, boolean b, boolean l ) {
		key   =s;	
		global=b;
		local =l;
	}
}

private Foo[] keys = new Foo [] {
new Foo(	"gauss:calctype", true, true ),
new Foo(	"gauss:method",	true,	true	),
new Foo(	"gauss:basis",	true,	true	),
new Foo(	"gauss:pop",	true,	true	),
new Foo(	"gauss:version",	true,	true	),
new Foo(	"gauss:hf",	true,	true	),
new Foo(	"gauss:rmsd",	true,	true	),
new Foo(	"gauss:rmsf",	true,	true	),
new Foo(	"gauss:scrf",	true,	true	),
new Foo(	"gauss:state",	true,	true	),
new Foo(	"gauss:pgvalue",	true,	true	),
new Foo(	"gauss:ginc",	true,	true	),
new Foo(	"gauss:polar",	true,	true	),
};

public void setup( String filename, METSMetadata gmd ) throws Exception {
	super.setup( filename, gmd );
//  BufferedReaderfin = new BufferedReader(new InputStreamReader( new FileInputStream( filename ) ) ) ;

//	String CML = fin.readLine();
   DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
   DocumentBuilder builder = factory.newDocumentBuilder();
   Document document = builder.parse( new File(filename) );

	NodeList nl = document.getElementsByTagName( "scalar" );

	for( int i=0; i< nl.getLength(); i++ ) {
		Element n = (Element) nl.item(i);
		if( n.getAttribute( "dictRef" ).startsWith( "gauss:" ) ) {
			String key = n.getAttribute( "dictRef" );
			String val = null;

			NodeList nl2 = n.getChildNodes();
			for( int j=0; j<nl2.getLength(); j++ ) {
				Node n2 = nl2.item(j);
				if( n2.getNodeType() == Node.TEXT_NODE ) {
					val = n2.getNodeValue();
					break;
				}
			}

			if( val!=null ){
				int idx = check_key( key );
				if( idx >=0 ) {
					if( keys[idx].global ) {	
						global_md.add_subject( key, val );
					}
					if( keys[idx].local ) {	
						local_md.add_subject( key, val );
					}
				}
			}
		}
	}	

	// Now get the formula
	nl = document.getElementsByTagName( "formula" );

	for(int i=0; i<nl.getLength(); i++ ) {
		Element n = (Element)(nl.item(i) );
		if( n.hasAttribute( "concise" ) ) {
				global_md.title = n.getAttribute("concise");
				break;
		}
	}
		
	
}


private int check_key( String k ) {
	for(int i=0; i < keys.length; i++ ) {
		if ( keys[i].key.equals(k) ) { return i; }
	}
	return -1;
}

public METSMetadata parse() {
	return local_md;

}

}
