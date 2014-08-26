import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.security.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class METSWriter {

protected Document doc;
protected Element  root;
protected Element  filegrp;
protected Element  structmap;

protected Hashtable dmd_hash;

protected Element maindmd;

private int dmd_idx;

private boolean metadata_set;

private ZipOutputStream zout;

private boolean written;

public METSWriter( String zip_filename ) throws Exception {
	metadata_set = false;
	written      = false;

	dmd_hash = new Hashtable();
	dmd_idx = 0;

	zout = new ZipOutputStream( new FileOutputStream( zip_filename ) );


	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();
	doc = builder.newDocument();

	root = (Element) doc.createElement( "mets" );

	root.setAttribute( "xsi:schemaLocation", "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/standards/premis http://www.loc.gov/standards/premis/PREMIS-v1-0.xsd http://www.rdn.ac.uk/oai/ebank/20050808/ebankterms.xsd" );
	root.setAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
	root.setAttribute( "xmlns:ebankterms", "http://purl.org/ebank/terms/" );
	root.setAttribute( "xmlns:xlink", "http://www.w3.org/1999/xlink" );
	root.setAttribute( "xmlns:ebank", "http://www.rdn.ac.uk/oai/ebank_dc/" );
	root.setAttribute( "xmlns:premis", "http://www.loc.gov/standards/premis" );
	root.setAttribute( "xmlns" , "http://www.loc.gov/METS/" );
	root.setAttribute( "xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
	root.setAttribute( "xmlns:dc", "http://purl.org/dc/elements/1.1/" );
	root.setAttribute( "PROFILE", "DSpace METS SIP Profile 1.0");


	// make structmap
	make_structmap();

	Element filesec = (Element) doc.createElement( "fileSec" );
  filegrp         = (Element) doc.createElement( "fileGrp" );
	filegrp.setAttribute( "USE", "CONTENT" );
	filesec.appendChild( filegrp );
	root.appendChild( filesec );


	doc.appendChild( root );

	maindmd = addMetadata( "Main" );


	addLicenceFluff( (Element) maindmd.getParentNode() );
}




private void make_structmap() {

	structmap = (Element) doc.createElement( "structMap" );
	structmap.setAttribute( "ID", "MainStructMap" );
	structmap.setAttribute( "TYPE", "LOGICAL" );
	structmap.setAttribute( "LABEL", "DSpace" );
	
	Element ee= (Element) doc.createElement( "div" );

	ee.setAttribute( "ID", "main-structural-div" );
	ee.setAttribute( "DMDID", "Main" );

	structmap.appendChild( ee );

	root.appendChild( structmap );

	structmap = ee;


}

private void addTrivialNode2( Element parent, String name, String attrib, String contents ) {
	Element e = (Element) doc.createElement( name );
	e.setAttribute( attrib, contents );
	parent.appendChild( e );
}

private void addTrivialNode( Element parent, String name, String contents ) {
	Element e = (Element) doc.createElement( name );
	if( contents!=null ) {
		e.appendChild( doc.createTextNode( contents ) );
	}
	parent.appendChild( e );
}

private void addLicenceFluff( Element parent ) {
	Element embargo = doc.createElement( "embargo" );

	{
		Element embargoLicence = doc.createElement( "embargoLicense" );	

		addTrivialNode( embargoLicence, "url", "http://www.closed.com" );
		addTrivialNode( embargoLicence, "description", "All Rights Reserved" );
		addTrivialNode( embargoLicence, "MachineReadable" , null );

		embargo.appendChild( embargoLicence );
	}
	{
		Element embargoLicence = doc.createElement( "postEmbargoLicense" );	

		addTrivialNode( embargoLicence, "url", "http://www.creativecommons.org/licenses/by-sa/2.5" );
		addTrivialNode( embargoLicence, "description", "Creative Commons Attribution-ShareAlike 2.5 License" );

		Element mr = doc.createElement( "machineReadable" );
		Element rdf= doc.createElement( "rdf:RDF" );
		mr.appendChild( rdf );

		embargo.appendChild( embargoLicence );
		embargoLicence.appendChild( mr );

		addTrivialNode2( rdf, "License", "rdf:about", "http://creativecommons.org/licenses/by-sa/2.5/" );
		addTrivialNode2( rdf, "permits", "rdf:resource", "http://web.resource.org/cc/Reproduction" );
		addTrivialNode2( rdf, "permits", "rdf:resource", "http://web.resource.org/cc/Distribution" );
		addTrivialNode2( rdf, "requires", "rdf:resource", "http://web.resource.org/cc/Notice" );
		addTrivialNode2( rdf, "requires", "rdf:resource", "http://web.resource.org/cc/Attribution" );
		addTrivialNode2( rdf, "permits", "rdf:resource", "http://web.resource.org/cc/DerivativeWorks" );
		addTrivialNode2( rdf, "requires", "rdf:resource", "http://web.resource.org/cc/ShareAlike" );

	}

	addTrivialNode( embargo, "period", "0" );
	addTrivialNode( embargo, "release", "automatic" );

	Date d = new Date();
	addTrivialNode( embargo, "start", (1900+d.getYear()) + "-" + d.getMonth() + "-" + d.getDate() );

	parent.appendChild( embargo );

}

public String addFile( String filename, String alt_filename, String  mimetype, METSMetadata md ) throws Exception {
	// read in the file
	File fileobj = new File( filename );
  InputStream is = new FileInputStream(fileobj);
  long length = fileobj.length();
	byte[] arr = new byte[ (int) length ];

// Read in the bytes
  int offset = 0;
  int numRead = 0;
  while (offset < arr.length
         && (numRead=is.read(arr, offset, arr.length-offset)) >= 0) {
		offset += numRead;
  }

	is.close();

	// change the filename to remove any path.
	filename = fileobj.getName();

	// replace the physical filename with an alternative, if specified

	if( alt_filename != null ) {
		filename = alt_filename;
	}
	
	MessageDigest md5 = MessageDigest.getInstance( "MD5" );
	md5.update( arr );

	String digest = dumpBytes( md5.digest() ) ;

	String fileid = filename;
	String dmdid  = "DMD-" + fileid+"-" + dmd_idx;

	dmd_idx++;

	Element file = (Element) doc.createElement( "file" );
	file.setAttribute( "ID", fileid );
	file.setAttribute( "DMDID" , dmdid );
	file.setAttribute( "CHECKSUMTYPE", "MD5" );
	file.setAttribute( "CHECKSUM", digest );
	file.setAttribute( "MIMETYPE", mimetype );

	Element flocate = (Element) doc.createElement( "FLocat" );
	flocate.setAttribute( "LOCTYPE", "URL" );
	flocate.setAttribute( "xlink:type", "simple" );
	flocate.setAttribute( "xlink:href", filename );

	file.appendChild( flocate );
	filegrp.appendChild( file );


	// DMD object
	Element xmlData = addMetadata( dmdid );

	dmd_hash.put ( dmdid, xmlData );

	//  structmap
	{
		Element e1 = (Element) doc.createElement( "div" );
		e1.setAttribute( "ID", dmdid +  "-struct" );
		Element e2 = (Element) doc.createElement( "fptr" );
		e2.setAttribute( "FILEID", fileid );
		e1.appendChild( e2 );
		structmap.appendChild( e1 );
	}

	set_md( xmlData, md ); 


	// write data to zipfile
	ZipEntry ze = new ZipEntry( filename );
	zout.putNextEntry( ze );
	zout.write( arr, 0, arr.length );

	return dmdid;
}


public boolean setMetadata( METSMetadata md ) {
	boolean ret = !metadata_set;
	if( !metadata_set ) {
		set_md( maindmd, md );
		metadata_set = true;
	}
	return ret;
}

private void set_md( Element e, METSMetadata md ) {
	if (md.type != null ) {
		Element e2 = (Element) doc.createElement( "dc:type" );
		e2.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		e2.appendChild( doc.createTextNode( md.type ) );
		e.appendChild( e2 );
	}
	if (md.title != null ) {
		Element e2 = (Element) doc.createElement( "dc:title" );
		e2.appendChild( doc.createTextNode( md.title ) );
		e.appendChild( e2 );
	}
/*
	if (md.identifier != null ) {
		Element e2 = (Element) doc.createElement( "identifier" );
		e2.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		e2.appendChild( doc.createTextNode( md.identifier ) );
		e.appendChild( e2 );
	}
*/
	for( int i=0; i<md.identifier.size(); i++ ) {
		String ms = (String) md.identifier.elementAt(i);
		Element es = (Element) doc.createElement( "dc:identifier" );
		es.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		es.appendChild( doc.createTextNode( ms ));
		e.appendChild( es );
	}

	if (md.license != null ) {
		Element e2 = (Element) doc.createElement( "dc:license" );
		e2.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		e2.appendChild( doc.createTextNode( md.license ) );
		e.appendChild( e2 );
	}

	if (md.experimentDate != null ) {
		Date d = md.experimentDate;

		Element e2 = (Element) doc.createElement( "dc:experimentDate" );
		e2.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		e2.appendChild( doc.createTextNode( (1900+d.getYear()) + "-" + d.getMonth() + "-" + d.getDate() ) );
		e.appendChild( e2 );
	}

	if (md.rights != null ) {
		Element e2 = (Element) doc.createElement( "dc:rights" );
		e2.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		e2.appendChild( doc.createTextNode( md.rights ) );
		e.appendChild( e2 );
	}
	if (md.publisher != null ) {
		Element e2 = (Element) doc.createElement( "dc:publisher" );
		e2.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		e2.appendChild( doc.createTextNode( md.publisher ) );
		e.appendChild( e2 );
	}

	for( int i=0; i<md.contributor.size(); i++ ) {
		String ms = (String) md.contributor.elementAt(i);
		Element es = (Element) doc.createElement( "dc:contributor" );
		es.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		es.appendChild( doc.createTextNode( ms ));
		e.appendChild( es );
	}

	if (md.creator != null ) {
		Element e2 = (Element) doc.createElement( "dc:creator" );
		e2.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		e2.appendChild( doc.createTextNode( md.creator ) );
		e.appendChild( e2 );

		if (md.foaf != null ) {
			Element e3 = (Element) doc.createElement( "seeAlso" );
			e3.setAttribute( "xmlns" , "http://www.w3.org/2000/01/rdf-schema#" );
			e3.setAttribute( "rdf:resource", md.foaf );
			e2.appendChild( e3 );
		}


	}

	for( int i=0; i<md.description.size(); i++ ) {
		METSSubject ms = (METSSubject) md.description.elementAt(i);
		Element es = (Element) doc.createElement( "dc:description" );
		es.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		es.appendChild( doc.createTextNode( ms.value ) );
		e.appendChild( es );
		
	}

	for( int i=0; i<md.subject.size(); i++ ) {
		METSSubject ms = (METSSubject) md.subject.elementAt(i);
		Element es = (Element) doc.createElement( "dc:subject" );
		es.setAttribute( "xmlns" , "http://purl.org/dc/elements/1.1/" );
		es.appendChild( doc.createTextNode( ms.value ));
		es.setAttribute( "xsi:type", ms.type );
		e.appendChild( es );
		
	}
}
private Element addMetadata( String dmdid ) throws Exception {
	Element dmd = (Element) doc.createElement( "dmdSec" );
	dmd.setAttribute( "ID", dmdid );

	Element mdWrap = (Element) doc.createElement( "mdWrap" );

	mdWrap.setAttribute( "LABEL", "eBank Metadata" );
	mdWrap.setAttribute( "MIMETYPE", "text/xml" );
	mdWrap.setAttribute( "MDTYPE", "DC" );

	dmd.appendChild( mdWrap );
	Element xmlData= (Element) doc.createElement( "xmlData" );
	mdWrap.appendChild( xmlData );
//	Element dc     = (Element) doc.createElement( "ebank_dc" );
//	xmlData.appendChild( dc );

	root.appendChild( dmd );
//	return dc;
	return xmlData;
}

private static String dumpBytes(byte[] bs) {
        StringBuffer ret = new StringBuffer(bs.length);
        for (int i = 0; i < bs.length; i++) {
            String hex = Integer.toHexString(0x0100 + (bs[i] & 0x00FF)).substring(1);
            ret.append((hex.length() < 2 ? "0" : "") + hex);
        }
        return ret.toString();
    }

public void write() throws Exception {
	if (!written ) {
	  TransformerFactory tranFactory = TransformerFactory.newInstance();
  	Transformer aTransformer = tranFactory.newTransformer();

	  Source src = new DOMSource(doc);

		ZipEntry ze = new ZipEntry ("mets.xml" );
		zout.putNextEntry( ze );

  	Result dest = new StreamResult(zout);
	  aTransformer.transform(src, dest);

		zout.close();
		written=true;
	}

}

public static final void  main( String[] args ) throws Exception {
	try {

	String output_file = "mets.zip";
	String metadata_file = "metadata.txt";

	Vector input_filenames = new Vector();
	Vector input_filetypes = new Vector();
	Vector archive_filenames = new Vector();
	Hashtable metadata = new Hashtable();

	for( int i=0; i<args.length; i++ ) {
		if( args[i].equals( "-o" ) ) {
			i++;
			output_file = args[i];
		}

		if( args[i].equals( "-m" ) ) {
			i++;
			metadata_file = args[i];
		}
		if( args[i].equals( "-i" ) ) {
				i++;
				input_filenames.addElement( args[i] );
				i++;
				input_filetypes.addElement( args[i] );
				i++;
				archive_filenames.addElement( args[i] );
		}

		if (args[i].equals( "-h" ) ) {
			System.out.println( "Syntax: METSWriter\n\t\t[-o output-zip-file]\n\t\t[ [-i input-file-name input-mime-type mets-archive-filename] ]\n\t\t[-m metadata-file]\n" );
			System.out.println( "Supported mimetypes:" );
			ParserFactory.list();
			System.exit(1);
		}
	}

	DataParser dp = new DataParser( metadata_file );

	METSMetadata md = readMetadata( dp );

	METSWriter hate = new METSWriter( output_file );


	for( int i=0; i<input_filenames.size(); i++ ) {
				String fn = (String) input_filenames.elementAt(i);
				String ft = (String) input_filetypes.elementAt(i);
				String afn= (String) archive_filenames.elementAt(i);

			try { 
				//Parser p = ParserFactory.createParser( fn, ft, md );
				METSMetadata md2 =  ParserFactory.parse( fn, ft, md ); //p.parse();

				hate.addFile(fn , afn, ft, md2 );
			}
			catch ( Exception ex ) {
				System.out.println("Failed to parse "+fn );
				ex.printStackTrace( System.out );
			}
		}
			

	hate.setMetadata( md );

	hate.write();

	} catch ( Exception e ) {
		e.printStackTrace( System.err );
		System.exit(2);	
	}

	System.exit(0);
}


public static  METSMetadata readMetadata (DataParser dp ) throws Exception {
	METSMetadata md = new METSMetadata();
	try {
		String a = dp.get_field( "default", "type" );
		if( a!=null && a.trim().length() > 0 ) 
			md.type = a;
		a = dp.get_field( "default", "title" );
		if( a!=null && a.trim().length() > 0 ) 
			md.title = a;

		{
			Vector v = dp.get_field_as_vector( "default", "identifier" );
			for( int i=0; i< v.size(); i++ ) {
				a = (String) v.elementAt(i);
				if( a!=null && a.trim().length() > 0 ) 
					md.add_identifier(a);// = a;
			}
		}


		{
			Vector v = dp.get_field_as_vector( "default", "contributor" );
			for( int i=0; i< v.size(); i++ ) {
				a = (String) v.elementAt(i);
				if( a!=null && a.trim().length() > 0 ) 
					md.contributor.addElement(a);// = a;
			}
		}


		a = dp.get_field( "default", "license" );
		if( a!=null && a.trim().length() > 0 ) 
			md.license = a;
		a = dp.get_field( "default", "rights" );
		if( a!=null && a.trim().length() > 0 ) 
			md.rights = a;
		a = dp.get_field( "default", "publisher" );
		if( a!=null && a.trim().length() > 0 ) 
			md.publisher = a;
		a = dp.get_field( "default", "creator" );
		if( a!=null && a.trim().length() > 0 ) 
			md.creator = a;
		a = dp.get_field( "default", "foaf" );
		if( a!=null && a.trim().length() > 0 )  {
			md.foaf = a;
			md.add_identifier(a);
		}
		a = dp.get_field( "default", "orcid-id" );
		if( a!=null && a.trim().length() > 0 )  {
			md.orcid = a;
			md.add_identifier(a);
		}

	} catch(Exception ex ) {}
	return md;
}
}
