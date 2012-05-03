package uk.digitalsquid.droidpad2.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.digitalsquid.droidpad2.LogTag;
import uk.digitalsquid.droidpad2.buttons.Button;
import uk.digitalsquid.droidpad2.buttons.Layout;
import uk.digitalsquid.droidpad2.buttons.ModeSpec;
import android.sax.Element;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.sax.TextElementListener;


public class LayoutDecoder implements LogTag {
	private LayoutDecoder() {}
	
	private static final SAXParserFactory factory = SAXParserFactory.newInstance();
	
	public static final ModeSpec decodeLayout(InputStream stream) throws IOException {
		try {
			return internalDecodeLayout(stream);
		} catch (ParserConfigurationException e) {
			IOException e2 = new IOException("Invalid parser config");
			e2.initCause(e);
			throw e2;
		} catch (SAXException e) {
			IOException e2 = new IOException("Failed to parse XML JS layout");
			e2.initCause(e);
			throw e2;
		}
	}
	private static final ModeSpec internalDecodeLayout(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = factory.newSAXParser();
		
		DocumentListener doc = new DocumentListener();
		
		parser.getXMLReader().setContentHandler(doc.getContentHandler());
		parser.getXMLReader().parse(new InputSource(stream));
		return doc.getModeSpec();
	}
	
	private static final class DocumentListener {
		Layout layout;
		int mode;
		
		RootElement root = new RootElement("layout");
		
		public ModeSpec getModeSpec() {
			ModeSpec spec = new ModeSpec();
			spec.setLayout(layout);
			spec.setMode(mode);
			return spec;
		}
		
		public ContentHandler getContentHandler() {
			return root.getContentHandler();
		}
		
		public DocumentListener() {
			root.setElementListener(new ElementListener() {
				
				@Override
				public void start(Attributes attr) {
					String title = attr.getValue("title");
					if(title == null) title = "Custom";
					String description = attr.getValue("description");
					if(description == null) description = "Custom layout";
					
					int width = Math.abs(getInt(attr, "width", Layout.BUTTONS_X));
					int height = Math.abs(getInt(attr, "height", Layout.BUTTONS_Y));
					
					layout = new Layout(title, description, width, height);
				}
				
				@Override
				public void end() {
				}
			});
			
			Element button = root.getChild("button");
			button.setTextElementListener(new TextElementListener() {
				
				int x, y, width, height, textSize;
				boolean isReset;
				
				@Override
				public void start(Attributes attr) {
					x = getAbsInt(attr, "x", 0);
					y = getAbsInt(attr, "y", 0);
					width = getAbsInt(attr, "width", 1);
					height = getAbsInt(attr, "height", 1);
					textSize = getAbsInt(attr, "textSize", 0);
					isReset = getBoolean(attr, "reset", false);
				}
				
				@Override
				public void end(String text) {
					if(text == null) text = "Button";
					layout.add(new Button(x, y, width, height, text, textSize));
				}
			});
		}
		
		private int getAbsInt(Attributes attr, String name, int defaultValue) {
			return Math.abs(getInt(attr, name, defaultValue));
		}
		private int getInt(Attributes attr, String name, int defaultValue) {
			String value = attr.getValue(name);
			
			try {
				return Integer.valueOf(value);
			} catch(NumberFormatException e) {
				return defaultValue;
			} catch(NullPointerException e) {
				return defaultValue;
			}
		}
		
		private float getFloat(Attributes attr, String name, float defaultValue) {
			String value = attr.getValue(name);
			
			try {
				return Float.valueOf(value);
			} catch(NumberFormatException e) {
				return defaultValue;
			} catch(NullPointerException e) {
				return defaultValue;
			}
		}
		
		private boolean getBoolean(Attributes attr, String name, boolean defaultValue) {
			String value = attr.getValue(name);
			
			try {
				boolean try1 = Boolean.valueOf(value);
				if(try1) return try1;
				boolean try2 = value.equals("1");
				if(try2) return try2;
				return false;
			} catch(NumberFormatException e) {
				return defaultValue;
			} catch(NullPointerException e) {
				return defaultValue;
			}
		}
	}
}
