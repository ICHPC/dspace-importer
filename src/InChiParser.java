import java.io.*;

public class InChiParser extends Parser {


public void setup( String filename ,METSMetadata gmd ) throws Exception {
	super.setup( filename, gmd );
  BufferedReader fin = new BufferedReader(new InputStreamReader( new FileInputStream( filename ) ) ) ;

	String inchi1 = fin.readLine();
	String inchi2 = fin.readLine();

	fin.close();

	if( inchi1 != null && inchi1.startsWith( "InChI=" ) ) {	
		global_md.add_identifier( inchi1 );
		global_md.add_subject( "InChI", inchi1 );
	}
	if( inchi2!=null &&  inchi2.startsWith( "InChIKey=" ) ) {	
	//	global_md.identifier= inchi2;
		global_md.add_identifier( inchi2 );
		global_md.add_subject( "InChIKey", inchi2 );
	}

	
}


public METSMetadata parse() {
	return local_md;

}

}
