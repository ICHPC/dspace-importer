import java.io.*;

public class WavefunctionParser extends Parser {


public void setup( String filename ,METSMetadata gmd ) throws Exception {
	super.setup( filename, gmd );
  BufferedReader fin = new BufferedReader(new InputStreamReader( new FileInputStream( filename ) ) ) ;


	
}


public METSMetadata parse() {
	return local_md;

}

}
