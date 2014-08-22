public class GaussianParser extends Parser {


public void setup ( String filename ,METSMetadata gmd ) throws Exception {
	super.setup( filename, gmd );

//	local_md.add_subject( "spectraterms:ChemistsRef", "lahdedah" );
//	global_md.identifier= "InChi=C6H6";
}


public METSMetadata parse() {
	return local_md;

}

}
