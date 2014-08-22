
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;

public class DataParser {


private Hashtable sections;
private BufferedReader fin;

private URL[] base_urls;

private boolean ok;

public DataParser () {
	sections = new Hashtable();

}

public DataParser( URL file ) throws IOException {
	this( new URL[] { file } );
}

public DataParser( URL[] file ) throws IOException {

	base_urls = file;

	sections = new Hashtable(10);

	for( int i =0; i<file.length; i++ ) {
		InputStream in = file[i].openStream();

		fin = new BufferedReader(new InputStreamReader(in) );


		parse_file();

		in.close();
	}
}

public DataParser( String filename ) throws IOException, FileNotFoundException {
	this( new String[] { filename } );
}




public DataParser(String[] filename) throws IOException, FileNotFoundException {
	boolean try_file_reader=false;


	sections = new Hashtable(10);

	base_urls = new URL[ filename.length ];

	for( int i=0; i<filename.length; i++ ) {
		////debug.println( 9, "Opening file ["+filename[i]+"]" );


		URL url = getClass().getClassLoader().getResource( filename[i] );


		if( url == null  ) {
			url = new URL( "file:"+filename[i] );
		}


		base_urls[i] = url;

		if( url!=null ) {
			try {
				fin = new BufferedReader( new InputStreamReader( url.openStream() ));
			} catch(Exception ex ) {

ex.printStackTrace( System.err );

				try_file_reader = true;
			}
		}

		if( try_file_reader ) {
			System.out.println( "Using FileReader" ); 
			fin = new BufferedReader(new FileReader(filename[i]));
		}

		if( fin == null ) { throw new FileNotFoundException( filename[i] ); }
		parse_file();
		

		fin.close();
	}
}

private void parse_file() {
	String s, t;
	String key, value;
	Properties p;
	int i;
	int line=0;
	
	ok =true;

	p = (Properties) sections.get("default" );

	if( p==null ) {	
		//debug.println( 9, "starting new section [default]");
		p = new Properties();
		sections.put("default", p);
	}

	try{	
	while(fin.ready()) {
		s = fin.readLine();

		if( s== null ) {
			// when in a JAR, this seems to be the best way to determine that we're at the end...
			break;
		}

		s.trim();
		
		line++;

		// Strip out any comments
		i = s.indexOf('#');
		if( i >=0 ) {
			//debug.println(9, "Ignoring comment : "+s.substring(i, s.length()));
			s = s.substring(0, i);
		}
		s = s.trim();
	

		// load in any included files
		if( s.length()>0 && s.charAt(0) == '!' ) {
			String file = s.substring( 1, s.length() );
			//debug.println(9, "Loading in file [" + file +"]" );
			load_file( file );
		}
		else if(s.length()>0) {	
			if(s.length()>2) {		  
				if(s.charAt(0)=='[') { // New section
					t = s.substring(1, s.length()-1).toLowerCase();
					
					p = (Properties) sections.get( t );

					if( p == null ) {
						//debug.println(9, "Found new section ["+t+"]");
						p = new Properties();
						sections.put(t, p);
					}
					else {
						//debug.println(9, "Resuming Section [" +t +"]" );

					}
				}
				else {
					i = s.indexOf('=');
					
					if(i<1) {
						//debug.println(0, "Bad input [line "+line+"] : "+s);		  	
						ok = false;
		  			}
					else {
						key   = s.substring(0, i).trim().toLowerCase();
			 			value = s.substring(i+1, s.length()).trim();			
						//debug.println(9, "Adding key ["+key+"] Value ["+value+"]");
						
						p.put(key, value);
		  			}					  
				}
			}
			else {
				//debug.println(0, "Bad input [line "+line+"] : "+s);					  
				ok = false;
			}
		
		}
	}	  
	}catch(IOException e) {
		ok = false;
		//debug.println(0, "IOException caught during parsing");		
	}
}

public Hashtable get_data() {
	if(ok) {
		return sections; 			  
	}
	else {
		return null;			  
	}
}

public boolean write_data(String filename) throws IOException {
	if(!ok) {
		return false;					  
	}
	else {
					  
 		FileWriter fout = new FileWriter(filename);		  
		Enumeration e, k;
		String sec, key, value;
		Properties p;
		e=sections.keys();
		
		while(e.hasMoreElements()) {
				  
			sec =(String) e.nextElement();
 
			fout.write("["+sec+"]\n");			
			
			p = (Properties) sections.get(sec);
		
			k = p.keys();
	
			while(k.hasMoreElements()) {
				key = (String) k.nextElement();
				value = (String) p.get(key);
				fout.write(key+"="+value+"\n"); 				
			}		  
			
			fout.write("\n");
		}
	
		fout.close();	
	}
	
	return true;
}
/*
public Pos3 get_field_as_pos3( String section, String field ) 
  throws NoSuchKeyException, NoSuchSectionException, BadFormatException {


	try {	
		String[] buf = get_field_as_csv_string( section, field );

		return new Pos3( Double.parseDouble( buf[0] ), Double.parseDouble( buf[1] ), Double.parseDouble( buf[2] ));

	} catch( Exception ex ) { throw new BadFormatException(); }
	
}
*/
public String[] get_field_as_csv_string( String section, String field ) 
  throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	String s = get_field( section, field );
	Vector v = new Vector();

	StringTokenizer tok = new StringTokenizer( s, "," );
	while( tok.hasMoreTokens() ) {
		String t = tok.nextToken();
		v.addElement( t.trim() );
	}
	
	return (String[]) v.toArray( new String[] {} );
}

public Rectangle get_field_as_rectangle( String section, String field )
	throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	String s;
	int x = 0, y = 0;
	int w = 0, h = 0;

	s = get_field( section, field );

	try {
		StringTokenizer tok = new StringTokenizer( s, "," );
		x = Integer.parseInt( tok.nextToken().trim() );		
		y = Integer.parseInt( tok.nextToken().trim() );
		w = Integer.parseInt( tok.nextToken().trim() );		
		h = Integer.parseInt( tok.nextToken().trim() );
	}
	catch (Exception e) {
		throw new BadFormatException("Converion error : Section ["+section+"] Field ["+field+"] = ["+ s + "] : " + e); 
	}

	return new Rectangle( x, y, w, h );


}


public Dimension get_field_as_dimension( String section, String field )
	throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	String s;
	int x = 0, y = 0;

	s = get_field( section, field );

	try {
		StringTokenizer tok = new StringTokenizer( s, "," );
		x = Integer.parseInt( tok.nextToken().trim() );		
		y = Integer.parseInt( tok.nextToken().trim() );
	}
	catch (Exception e) {
		throw new BadFormatException("Converion error : Section ["+section+"] Field ["+field+"] = ["+ s + "] : " + e); 
	}

	return new Dimension( x, y );


}


public Point get_field_as_point( String section, String field )
	throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	String s;
	int x = 0, y = 0;

	s = get_field( section, field );

	try {
		StringTokenizer tok = new StringTokenizer( s, "," );
		x = Integer.parseInt( tok.nextToken().trim() );		
		y = Integer.parseInt( tok.nextToken().trim() );
	}
	catch (Exception e) {
		throw new BadFormatException("Converion error : Section ["+section+"] Field ["+field+"] = ["+ s + "] : " + e); 
	}

	return new Point( x, y );


}


public boolean get_field_as_boolean( String section, String field ) 
	throws NoSuchKeyException, NoSuchSectionException {
	String s;
	boolean i;

	s = get_field( section, field );

		s = s.toLowerCase().trim();

		if( s.equals("yes") || s.equals("true") || s.equals("1" ) ) {
			i = true;		
		}
		else {
			i = false;
		}
			
	return i;

}

public int get_field_as_integer( String section, String field ) 
	throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	return get_field_as_int( section, field );
}


public int get_field_as_int( String section, String field ) 
	throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	String s;
	int i;

	s = get_field( section, field );

	try {
		i = Integer.parseInt( s );		
	}
	catch (NumberFormatException e) {
		throw new BadFormatException("Cannot parse integer: Section ["+section+"] Field ["+field+"] = ["+ s + "]"); 
	}

	return i;

}

public double get_field_as_double( String section, String field ) 
	throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	String s;
	double i;

	s = get_field( section, field );

	try {
		i = Double.parseDouble( s );		
	}
	catch (NumberFormatException e) {
		throw new BadFormatException("Cannot parse double: Section ["+section+"] Field ["+field+"] = ["+ s + "]"); 
	}

	return i;

}


public long get_field_as_long( String section, String field ) 
	throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	String s;
	long i;

	s = get_field( section, field );

	try {
		i = Long.parseLong( s );		
	}
	catch (NumberFormatException e) {
		throw new BadFormatException("Cannot parse long: Section ["+section+"] Field ["+field+"] = ["+ s + "]"); 
	}

	return i;

}

public Color get_field_as_colour( String section, String field ) 
 throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	String s;

	s = get_field( section, field );

	s = s.toLowerCase().trim();

	if( s.equals( "black" )) {	return ( Color.black ); }
	if( s.equals( "white" )) {	return ( Color.white ); }
	if( s.equals( "red"   )) {	return ( Color.red   ); }
	if( s.equals( "green" )) {	return ( Color.green ); }
	if( s.equals( "blue"  )) {	return ( Color.blue  ); }
	if( s.equals( "yellow")) {	return ( Color.yellow); }
	if( s.equals( "cyan"  )) {	return ( Color.cyan  ); }
	if( s.equals( "magenta")) {	return ( Color.magenta ); }

	StringTokenizer tok = new StringTokenizer( get_field( section, field ), "," );
	int r = Integer.parseInt( tok.nextToken() );
	int g = Integer.parseInt( tok.nextToken() );
	int b = Integer.parseInt( tok.nextToken() );

	return new Color( r,g,b );
}


public boolean has_section( String section ) {
	Hashtable h = null;
	try {
		h = (Hashtable) sections.get(section);
	} catch(Exception e) {
		h = null;
	}

	if(h==null)
		return false;
	else
		return true;
}

public String get_field( String section, String field ) 
	throws NoSuchKeyException, NoSuchSectionException {

	Hashtable s;
	String t;

	if(section==null) {
		throw new NoSuchSectionException();
	}
	if(field==null) {
		throw new NoSuchKeyException();
	}

	s = (Hashtable) sections.get(section);

	if( s == null) {
		throw new NoSuchSectionException("Section ["+section+"] not found");
	}

	
	t = (String) s.get(field);

	if( t == null ) {
		throw new NoSuchKeyException("Field ["+field+"] in section ["+section+"] not found");
	}

	return new String(t);
}

public void set_field( String section, String field, String data ) 
 throws NullPointerException {
	Hashtable s;
	
	if( section== null || field == null )
		throw new NullPointerException();

	s = (Hashtable) sections.get(section);

	if(s==null) {
		s = new Hashtable();
		sections.put(section, s);
	}

	s.put(field, data);
}



public int count_sections( String prefix ) {
   int i=0;
   while( has_section( prefix+"-" + i ) ) {
      i++;
   }

   return i;
}


public static void main(String[] args) throws Exception {
	DataParser dp = new DataParser(args[0]);
	dp.write_data( args[0]+".out");
}


public long get_field_as_milliseconds( String section, String field ) 
	throws NoSuchKeyException, NoSuchSectionException, BadFormatException {
	String s;
	long i;

	s = get_field( section, field );

	try {
		i = Long.parseLong( s );		
	}
	catch( Exception ex ) {

	try {
		StringTokenizer tok = new StringTokenizer( s, ":"  );

		// hours:minutes
		i = Long.parseLong( tok.nextToken() ) * (1000*60*60) + Long.parseLong( tok.nextToken() ) * (1000*60 );

		// seconds
		if( tok.hasMoreTokens() ) {
			i += Long.parseLong( tok.nextToken() ) * 1000;
		}

		// milliseconds
		if( tok.hasMoreTokens() ) {
			i += Long.parseLong( tok.nextToken() );
		}

	}
	catch (Exception e2) {
		throw new BadFormatException("Cannot parse integer: Section ["+section+"] Field ["+field+"] = ["+ s + "]"); 
	}
	}
	return i;

}

private void load_file( String file ) {
	
			file = file.trim();

			for(int i=0; i < base_urls.length; i++ ) {

				URL u = null;
				try {

					String path = base_urls[i].getPath();

					int idx = path.lastIndexOf( '/' );
					if ( idx >= 0 ) {
						path = path.substring( 0, idx );
					}

					u = new URL( base_urls[i].getProtocol(), base_urls[i].getHost(), base_urls[i].getPort(), path +"/" + file );

	
					DataParser dp = new DataParser( u );
					Enumeration ee = dp.sections.keys();

					while( ee.hasMoreElements() ) {
						Object key = ee.nextElement();
						sections.put( key, dp.sections.get( key ));
					}
					//debug.println( 9, "Imported [" + u.toString() +"]" );
					return ;
				} catch( Exception ex ) {
					if( u!=null ) {
						System.out.println( "Not found ["+u.toString() +"]" ); 	
					}
				} 
			}
}


}



