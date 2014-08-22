import java.io.*;

public class DefaultParser extends Parser {


public void setup( String filename ,METSMetadata gmd ) throws Exception {
	super.setup( filename, gmd );
	
}


public METSMetadata parse() {
	return local_md;

}

}
