package gov.nysenate.sage.model;

import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class ParseStream {
	private String PACKAGE;
	
	public ParseStream(String PACKAGE) {
		this.PACKAGE = PACKAGE;
	}
	public Object parseStream(URL url) throws JAXBException {
		String packageName = PACKAGE;
		JAXBContext jc = JAXBContext.newInstance(packageName);
		Unmarshaller u = jc.createUnmarshaller();
		Object o = u.unmarshal(url);
		
		return o;
	}
}
