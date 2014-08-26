import java.util.Date;
import java.util.Vector;

public class METSMetadata {

	String type				=null;
	String title			=null;
	Vector identifier	=new Vector();
	String license    =null;
	Date experimentDate=new Date();
	String rights     =null;
	String publisher	=null;
	Vector contributor=new Vector();
	String creator    =null;
	String foaf       =null;
	String orcid      =null;
	Vector description= new Vector();
	Vector subject    = new Vector();

public void add_subject( String type, String value ) {
	subject.addElement( new METSSubject( type, value ) );
}
public void add_identifier( String value ) {
	identifier.addElement( new String(value) );
}

public void add_description( String type, String value ) {
	description.addElement( new METSSubject( type, value ) );
}
}
