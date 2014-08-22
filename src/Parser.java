public class Parser {

METSMetadata local_md;
METSMetadata global_md;

public Parser() {}

public void setup( String filename, METSMetadata md )  throws Exception{
	global_md = md;
	local_md  = new METSMetadata();
}

public METSMetadata parse() {
	return local_md;
}

}
