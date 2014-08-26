public class ParserFactory {


private static final Object[][] parsers = new Object[][] {
	{ new String("chemical/x-gaussian-log")  , new GaussianParser() },
	{ new String("chemical/x-inchi")         , new InChiParser()    },
	{ new String("chemical/x-cml")           , new CMLParser()      },
	{ new String("chemical/x-smiles")				 , new SMILESParser() },
	{ new String("chemical/x-wavefunction")				 , new WavefunctionParser() },
	{ new String("x-uportal/x-description")				 , new UPortalParser( "abstract" ) },
	{ new String("x-uportal/x-project")				 , new UPortalParser( "project" ) },
	{ new String("text/plain")               , new DefaultParser() },
};

public static final void list() {
	for( int i=0; i< parsers.length; i++ ) {
		System.out.println( (String) parsers[i][0] );
	}
}

public static final synchronized METSMetadata  parse( String filename, String mimetype, METSMetadata global_metadata ) throws InstantiationException {

	try {	
		for ( int i=0; i< parsers.length; i++ ) {
			if( mimetype.equals( (String) parsers[i][0] ) ) {
				// attempt 
				Parser p = (Parser) parsers[i][1];
				p.setup( filename, global_metadata );
				return p.parse();
			}
		}
/*
	if( mimetype.equals( 	 "chemical/x-gaussian-log" )) {
			return new GaussianParser( filename, global_metadata );
	}
	else if( mimetype.equals( "chemical/inchi" )) {
			return new InChiParser( filename, global_metadata );
	}
	else if( mimetype.equals( "xml/cml" )) {
			return new CMLParser( filename, global_metadata );
	}
*/
		//return new Parser( filename, global_metadata );
	Parser p = new DefaultParser();
	p.setup( filename, global_metadata );
	return p.parse();

	}catch(Exception ex ) {
		ex.printStackTrace( System.err );
		throw new InstantiationException();
	}

	//return new METSMetadata(); 
}

}
