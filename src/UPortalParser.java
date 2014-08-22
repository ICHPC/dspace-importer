import java.io.*;

public class UPortalParser extends Parser {

private String field;

public UPortalParser( String field ) {
	super();
	this.field = field;
}

public void setup( String filename ,METSMetadata gmd ) throws Exception {
	super.setup( filename, gmd );
  BufferedReader fin = new BufferedReader(new InputStreamReader( new FileInputStream( filename ) ) ) ;

	String str = fin.readLine();

	fin.close();

	str= str.trim();
	if (str.length()>0) {
		global_md.add_description( field, str );
	}
	
}


public METSMetadata parse() {
	return local_md;

}

}
