import java.io.*;

public class SMILESParser extends Parser {


public void setup( String filename ,METSMetadata gmd ) throws Exception {
	super.setup( filename, gmd );
  BufferedReader fin = new BufferedReader(new InputStreamReader( new FileInputStream( filename ) ) ) ;

	String SMILES = fin.readLine();

	fin.close();

	SMILES= SMILES.trim();
	if (SMILES.length()>0) {
		global_md.add_subject( "SMILES", SMILES );
	}
	
}


public METSMetadata parse() {
	return local_md;

}

}
