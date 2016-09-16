package de.subcentral.fx.settings;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class IndentingXMLConfiguration extends XMLConfiguration {
	@Override
	protected Transformer createTransformer() throws ConfigurationException {
		Transformer transformer = super.createTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		return transformer;
	}
}