package helpers;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.openexi.proc.common.AlignmentType;
import org.openexi.proc.common.EXIOptionsException;
import org.openexi.proc.common.GrammarOptions;
import org.openexi.proc.grammars.GrammarCache;
import org.openexi.sax.EXIReader;
import org.openexi.schema.EXISchema;
import org.openexi.scomp.EXISchemaFactory;
import org.openexi.scomp.EXISchemaFactoryException;
import org.openexi.scomp.EXISchemaReader;
import org.pmw.tinylog.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import core.Config;

/**
 * @author Nagasena
 * See http://openexi.sourceforge.net/ for information about EXI
 * Most of this source code was taken from their open source project, OpenEXI/Nagasena
 */
public class EXIDecoder {
    /**
     * Default constructor
     */
    public EXIDecoder() {
        super();
    }

    /**
     * This is the main function for decoding EXI. In short, this takes in binary EXI, and returns
     * a XML String.
     * 
     * 
     * @param binaryEXI Input binary representing the EXI encoded material
     * @param alignment String variable for different alignment methods. Options are: bitPacked, compress, preCompress,byteAlign
     * @param preserveComments Boolean switch for preserving comments in XML
     * @param preservePIs Boolean switch for preserving Programming Instructions
     * @param preserveDTD Boolean switch for preserving DTD
     * @param preserveNamespace Boolean switch for preserving name space
     * @param preserveLexicalValues Boolean switch for preserving lexical values
     * @param blockSize Integer - default value is 1000000
     * @param maxValueLength Integer - default value is -1
     * @param maxValuePartitions Integer - default value is -1
     * @param schemaFileName String of the file path to the schema of the XML
     * @param exiSchemaFileName String String of the file path to the schema of EXI
     * @param strict Boolean switch for determining if it should be in strict mode or not
     * @param useSchema String for determining schema type, default is "EXIG"
     * @return String consisting of the decoded XML
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws EXIOptionsException
     * @throws TransformerConfigurationException
     * @throws EXISchemaFactoryException
     * @throws ClassNotFoundException
     */
    public String decodeEXI(
            byte[] binaryEXI, 
//            String decodedString,
            String alignment,
// Preservation options.
            Boolean preserveComments,
            Boolean preservePIs,
            Boolean preserveDTD,
            Boolean preserveNamespace,
            Boolean preserveLexicalValues,
            int blockSize,
            int maxValueLength,
            int maxValuePartitions,
// Schema options.
            String schemaFileName,
            String exiSchemaFileName,
            Boolean strict,
            String useSchema
   ) throws FileNotFoundException, IOException,
        SAXException, EXIOptionsException, TransformerConfigurationException,
                                                   EXISchemaFactoryException, ClassNotFoundException {
    	

        ByteArrayInputStream in = null;
//        Writer out = null;
        StringWriter stringWriter = new StringWriter();

// The Grammar Cache stores schema and EXI options information. The settings must match when encoding
// and subsequently decoding a data set.
        GrammarCache grammarCache;

// All EXI options can expressed in a single short integer. DEFAULT_OPTIONS=2;
        short options = GrammarOptions.DEFAULT_OPTIONS;

        try {
            
// Standard SAX methods parse content and lexical values.
            SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            TransformerHandler transformerHandler = saxTransformerFactory.newTransformerHandler();

// EXIReader infers and reconstructs the XML file structure.
            EXIReader reader = new EXIReader();
        
// Set alignment and compression
            if (alignment.equals("bitPacked"))
                    reader.setAlignmentType(AlignmentType.bitPacked);
            if (alignment.equals("compress"))
                    reader.setAlignmentType(AlignmentType.compress);
            if (alignment.equals("preCompress"))
                    reader.setAlignmentType(AlignmentType.preCompress);
            if(alignment.equals("byteAligned"))
                    reader.setAlignmentType(AlignmentType.byteAligned);

// If using strict interpretation of the schema, set STRICT_OPTIONS and continue.
            if (strict) {
                options = GrammarOptions.STRICT_OPTIONS;
            }
// Otherwise, check for preservation settings.            
            else 
            {
                if (preserveComments) options = GrammarOptions.addCM(options);
                if (preservePIs) options = GrammarOptions.addPI(options);
                if (preserveDTD) options = GrammarOptions.addDTD(options);
                if (preserveNamespace) options = GrammarOptions.addNS(options);
            }             

            // Set preservation preferences handled directly in the transmogrifier.
            reader.setPreserveLexicalValues(preserveLexicalValues);
            
            // Set the number of elements processed as a block.
            if (blockSize!=1000000) reader.setBlockSize(blockSize);
            
            // Set the maximum length for values stored in the String Table for reuse.
            if (maxValueLength>-1) reader.setValueMaxLength(maxValueLength);  
            
            // Set the maximum number of values stored in the String Table.
            if (maxValuePartitions >-1) 
                reader.setValuePartitionCapacity(maxValuePartitions);
            
            //byte[] bytes = new byte[(int)byteString.getBytes().length];
            in = new ByteArrayInputStream(binaryEXI);
//            out = new OutputStreamWriter(new FileOutputStream(destinationFile), "UTF-8");

// Set the schema and EXI Options in the Grammar Cache.
            FileInputStream fis = null;
            
    // Create a schema and set it to null. If useSchema == "None" it remains null.
            EXISchema schema = null;
            
    // If using an XSD, it must be converted to an EXISchema.
            if(useSchema.equals("XSD")) {
                try {
                    InputSource is = new InputSource(schemaFileName);
                    EXISchemaFactory factory = new EXISchemaFactory();
                    schema = factory.compile(is); 
                }
                finally {
                }
            }
// If using an EXIG, it can be read directly.
            else if (useSchema.equals("EXIG")) {
                try {
                    fis = new FileInputStream(exiSchemaFileName);
                    schema = new EXISchemaReader().parse(fis);
                }
                finally {
                    if (fis != null) fis.close();
                }
            }
            else if (!"None".equals(useSchema))
              assert false;
            grammarCache = new GrammarCache(schema, options);

            
// Use the Grammar Cache to set the schema and grammar options for EXIReader.
            reader.setGrammarCache(grammarCache);

// Prepare to send the results from the transformer to a StringWriter object.
            transformerHandler.setResult(new StreamResult(stringWriter));
            
// Read the file into a byte array.
//            byte fileContent[] = new byte[(int)inputFile.length()];
            byte[] hi = new byte[binaryEXI.length];
            in.read(hi);
            
// Assign the transformer handler to interpret XML content.
            reader.setContentHandler(transformerHandler);
            
// Parse the file information.
            reader.parse(new InputSource(new ByteArrayInputStream(hi)));

// Get the resulting string, write it to the output file, and flush the buffer contents.
            final String reconstitutedString;
            reconstitutedString = stringWriter.getBuffer().toString();
//            out.write(reconstitutedString);
//            out.flush();
            return reconstitutedString;
        }
// Verify that the input and output files are closed.
        finally {
            if (in != null)
                in.close();
//            if (out != null)
//                out.close();
        }
    }
    
    /**
     * This method decodes binary EXI data using default settings
     * 
     * @param binaryEXI
     * @return decoded string
     */
    public String decodeEXIDefault(byte[] binaryEXI) {

    	try {
    	String alignment = "compress"; // Options are: "compress",
		// "preCompress", "byteAligned", "bitPacked"
		boolean preserveComments = false; // preserve comments
		boolean preservePIs = false; // preserve 'programming instructions'
		boolean preserveDTD = false;
		boolean preserveNamespace = true; // preserve name space
		boolean preserveLexicalValues = true; // preserve lexical values
		int blockSize = 1000000; // default value
		int maxValueLength = -1; // default
		int maxValuePartitions = -1; // default
		
		String schemaFileName = Config.Basic.xsd;
		String exiSchemaFileName = Config.Basic.exig;
		boolean strict = false;
		String useSchema = "EXIG"; // options are "None","XSD","EXIG"
    	

        ByteArrayInputStream in = null;
//        Writer out = null;
        StringWriter stringWriter = new StringWriter();

// The Grammar Cache stores schema and EXI options information. The settings must match when encoding
// and subsequently decoding a data set.
        GrammarCache grammarCache;

// All EXI options can expressed in a single short integer. DEFAULT_OPTIONS=2;
        short options = GrammarOptions.DEFAULT_OPTIONS;

        try {
            
// Standard SAX methods parse content and lexical values.
            SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            TransformerHandler transformerHandler = saxTransformerFactory.newTransformerHandler();

// EXIReader infers and reconstructs the XML file structure.
            EXIReader reader = new EXIReader();
        
// Set alignment and compression
            if (alignment.equals("bitPacked"))
                    reader.setAlignmentType(AlignmentType.bitPacked);
            if (alignment.equals("compress"))
                    reader.setAlignmentType(AlignmentType.compress);
            if (alignment.equals("preCompress"))
                    reader.setAlignmentType(AlignmentType.preCompress);
            if(alignment.equals("byteAligned"))
                    reader.setAlignmentType(AlignmentType.byteAligned);

// If using strict interpretation of the schema, set STRICT_OPTIONS and continue.
            if (strict) {
                options = GrammarOptions.STRICT_OPTIONS;
            }
// Otherwise, check for preservation settings.            
            else 
            {
                if (preserveComments) options = GrammarOptions.addCM(options);
                if (preservePIs) options = GrammarOptions.addPI(options);
                if (preserveDTD) options = GrammarOptions.addDTD(options);
                if (preserveNamespace) options = GrammarOptions.addNS(options);
            }             

            // Set preservation preferences handled directly in the transmogrifier.
            reader.setPreserveLexicalValues(preserveLexicalValues);
            
            // Set the number of elements processed as a block.
            if (blockSize!=1000000) reader.setBlockSize(blockSize);
            
            // Set the maximum length for values stored in the String Table for reuse.
            if (maxValueLength>-1) reader.setValueMaxLength(maxValueLength);  
            
            // Set the maximum number of values stored in the String Table.
            if (maxValuePartitions >-1) 
                reader.setValuePartitionCapacity(maxValuePartitions);
            
            //byte[] bytes = new byte[(int)byteString.getBytes().length];
            in = new ByteArrayInputStream(binaryEXI);
//            out = new OutputStreamWriter(new FileOutputStream(destinationFile), "UTF-8");

// Set the schema and EXI Options in the Grammar Cache.
            FileInputStream fis = null;
            
    // Create a schema and set it to null. If useSchema == "None" it remains null.
            EXISchema schema = null;
            
    // If using an XSD, it must be converted to an EXISchema.
            if(useSchema.equals("XSD")) {
                try {
                    InputSource is = new InputSource(schemaFileName);
                    EXISchemaFactory factory = new EXISchemaFactory();
                    schema = factory.compile(is); 
                }
                finally {
                }
            }
// If using an EXIG, it can be read directly.
            else if (useSchema.equals("EXIG")) {
                try {
                    fis = new FileInputStream(exiSchemaFileName);
                    schema = new EXISchemaReader().parse(fis);
                }
                finally {
                    if (fis != null) fis.close();
                }
            }
            else if (!"None".equals(useSchema))
              assert false;
            grammarCache = new GrammarCache(schema, options);

            
// Use the Grammar Cache to set the schema and grammar options for EXIReader.
            reader.setGrammarCache(grammarCache);

// Prepare to send the results from the transformer to a StringWriter object.
            transformerHandler.setResult(new StreamResult(stringWriter));
            
// Read the file into a byte array.
//            byte fileContent[] = new byte[(int)inputFile.length()];
            byte[] hi = new byte[binaryEXI.length];
            in.read(hi);
            
// Assign the transformer handler to interpret XML content.
            reader.setContentHandler(transformerHandler);
            
// Parse the file information.
            reader.parse(new InputSource(new ByteArrayInputStream(hi)));

// Get the resulting string, write it to the output file, and flush the buffer contents.
            final String reconstitutedString;
            reconstitutedString = stringWriter.getBuffer().toString();
//            out.write(reconstitutedString);
//            out.flush();
            return reconstitutedString;
        }
// Verify that the input and output files are closed.
        finally {
            if (in != null)
                in.close();
//            if (out != null)
//                out.close();
        }
    	}catch(Exception e) {
    		Logger.error(e);
    		return null;
    	}
    }
}
