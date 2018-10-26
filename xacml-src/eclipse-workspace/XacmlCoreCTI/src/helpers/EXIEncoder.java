package helpers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.openexi.proc.common.AlignmentType;
import org.openexi.proc.common.EXIOptionsException;
import org.openexi.proc.common.GrammarOptions;
import org.openexi.proc.grammars.GrammarCache;
import org.openexi.sax.Transmogrifier;
import org.openexi.sax.TransmogrifierException;
import org.openexi.schema.EXISchema;
import org.openexi.scomp.EXISchemaFactory;
import org.openexi.scomp.EXISchemaFactoryException;
import org.openexi.scomp.EXISchemaReader;
import org.pmw.tinylog.Logger;
import org.xml.sax.InputSource;

import core.Config;

/**
 * @author Nagasena
 * See http://openexi.sourceforge.net/ for information about EXI
 * Most of this source code was taken from their open source project, OpenEXI/Nagasena
 */
public class EXIEncoder {
	/**
     * Default constructor
     */
	public EXIEncoder() {
		super();
	}

	/**
	 * This is the main function for encoding EXI. In short, this takes in a String of XML, and returns
     * a byte[] of the EXI encoded String 
	 * 
	 * @param stringToBeEncoded String of XML to be encoded
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
     * @return byte[] consisting of the encoded XML
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws TransmogrifierException
	 * @throws EXIOptionsException
	 * @throws EXISchemaFactoryException
	 */
	

	public byte[] encodeEXI(
			String stringToBeEncoded,
			// String destinationFile,
			String alignment,

			// Preservation options
			Boolean preserveComments, Boolean preservePIs, Boolean preserveDTD,
			Boolean preserveNamespace, Boolean preserveLexicalValues,
			Boolean preserveWhitespace, int blockSize, int maxValueLength,
			int maxValuePartitions,
			// Schema options
			String schemaFileName, String exiSchemaFileName, Boolean strict,
			String useSchema) throws FileNotFoundException, IOException,
			ClassNotFoundException, TransmogrifierException,
			EXIOptionsException, EXISchemaFactoryException {

		// Instantiate input/output streams and grammar cache
		ByteArrayInputStream in = null;
		ByteArrayOutputStream out = null;
		GrammarCache grammarCache;
		byte[] bin = null;
		// All EXI options can be stored in a single short integer.
		// DEFAULT_OPTIONS=2.
		short options = GrammarOptions.DEFAULT_OPTIONS;
		try {
			// Encoding always requires the same steps.

			// 1. Instantiate a Transmogrifier
			Transmogrifier transmogrifier = new Transmogrifier();

			// Set alignment and compression
			if (alignment.equals("bitPacked"))
				transmogrifier.setAlignmentType(AlignmentType.bitPacked);
			if (alignment.equals("compress"))
				transmogrifier.setAlignmentType(AlignmentType.compress);
			if (alignment.equals("preCompress"))
				transmogrifier.setAlignmentType(AlignmentType.preCompress);
			if (alignment.equals("byteAligned"))
				transmogrifier.setAlignmentType(AlignmentType.byteAligned);

			// If using strict schema interpretation, set the options to
			// STRICT_OPTIONS (1)
			// and move on. Other options are ignored.
			if (strict) {
				options = GrammarOptions.STRICT_OPTIONS;
			} else {
				// Otherwise, set preservation preferences in Grammar Options
				options = GrammarOptions.DEFAULT_OPTIONS;
				if (preserveComments)
					options = GrammarOptions.addCM(options);
				if (preservePIs)
					options = GrammarOptions.addPI(options);
				if (preserveDTD)
					options = GrammarOptions.addDTD(options);
				if (preserveNamespace)
					options = GrammarOptions.addNS(options);
			}

			// Set preservation preferences handled directly in the
			// transmogrifier.
			transmogrifier.setPreserveLexicalValues(preserveLexicalValues);
			transmogrifier.setPreserveWhitespaces(preserveWhitespace);

			// Set the number of elements processed as a block.
			if (blockSize != 1000000)
				transmogrifier.setBlockSize(blockSize);

			// Set the maximum length for values stored in the String Table for
			// reuse.
			if (maxValueLength > -1)
				transmogrifier.setValueMaxLength(maxValueLength);

			// Set the maximum number of values stored in the String Table.
			if (maxValuePartitions > -1)
				transmogrifier.setValuePartitionCapacity(maxValuePartitions);

			in = new ByteArrayInputStream(
					stringToBeEncoded.getBytes(StandardCharsets.UTF_8));
			out = new ByteArrayOutputStream();

			// 3. Set the schema and EXI options in the Grammar Cache.
			FileInputStream fis = null;

			// Create a schema and set it to null. If useSchema == "None" it
			// remains null.
			EXISchema schema = null;

			// If using an XSD, it must be converted as an EXISchema.
			if (useSchema.equals("XSD")) {
				try {
					InputSource is = new InputSource(schemaFileName);
					EXISchemaFactory factory = new EXISchemaFactory();
					schema = factory.compile(is);
				} finally {
				}
			}
			// If using an EXIG, it can be read directly.
			else if (useSchema.equals("EXIG")) {
				try {
					fis = new FileInputStream(exiSchemaFileName);
					schema = new EXISchemaReader().parse(fis);
				} finally {
					if (fis != null)
						fis.close();
				}
			} else if (!"None".equals(useSchema))
				assert false;
			grammarCache = new GrammarCache(schema, options);

			// 4. Set the configuration options in the Transmogrifier.
			transmogrifier.setGrammarCache(grammarCache);

			// 5. Set the output stream.
			transmogrifier.setOutputStream(out);

			// 6. Encode the input stream.
			transmogrifier.encode(new InputSource(in));

		} finally {

			if (in != null) {
				in.close();
			}
			if (out != null) {
				bin = out.toByteArray();
				out.close();
			}
		}
		return bin;
	}
	
	/**
	 * This method uses default settings for EXI encoding.
	 * 
	 * @param stringToBeEncoded
	 * @return byte[] of EXI encoded string
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws TransmogrifierException
	 * @throws EXIOptionsException
	 * @throws EXISchemaFactoryException
	 */
	public byte[] encodeEXIDefault(String stringToBeEncoded){
    	try {
    	String alignment = "compress"; // Options are: "compress",
		// "preCompress", "byteAligned", "bitPacked"
		boolean preserveComments = false; // preserve comments
		boolean preservePIs = false; // preserve 'programming instructions'
		boolean preserveDTD = false;
		boolean preserveNamespace = true; // preserve name space
		boolean preserveLexicalValues = true; // preserve lexical values
		boolean preserveWhitespace = false; // preserve whitespace
		int blockSize = 1000000; // default value
		int maxValueLength = -1; // default
		int maxValuePartitions = -1; // default
		
		
		String schemaFileName = Config.Basic.xsd;
		String exiSchemaFileName = Config.Basic.exig;
		boolean strict = false;
		String useSchema = "EXIG"; // options are "None","XSD","EXIG"
		
		// Instantiate input/output streams and grammar cache
		ByteArrayInputStream in = null;
		ByteArrayOutputStream out = null;
		GrammarCache grammarCache;
		byte[] bin = null;
		// All EXI options can be stored in a single short integer.
		// DEFAULT_OPTIONS=2.
		short options = GrammarOptions.DEFAULT_OPTIONS;
		try {

			// Encoding always requires the same steps.

			// 1. Instantiate a Transmogrifier
			Transmogrifier transmogrifier = new Transmogrifier();

			// Set alignment and compression
			if (alignment.equals("bitPacked"))
				transmogrifier.setAlignmentType(AlignmentType.bitPacked);
			if (alignment.equals("compress"))
				transmogrifier.setAlignmentType(AlignmentType.compress);
			if (alignment.equals("preCompress"))
				transmogrifier.setAlignmentType(AlignmentType.preCompress);
			if (alignment.equals("byteAligned"))
				transmogrifier.setAlignmentType(AlignmentType.byteAligned);

			// If using strict schema interpretation, set the options to
			// STRICT_OPTIONS (1)
			// and move on. Other options are ignored.
			if (strict) {
				options = GrammarOptions.STRICT_OPTIONS;
			} else {
				// Otherwise, set preservation preferences in Grammar Options
				options = GrammarOptions.DEFAULT_OPTIONS;
				if (preserveComments)
					options = GrammarOptions.addCM(options);
				if (preservePIs)
					options = GrammarOptions.addPI(options);
				if (preserveDTD)
					options = GrammarOptions.addDTD(options);
				if (preserveNamespace)
					options = GrammarOptions.addNS(options);
			}

			// Set preservation preferences handled directly in the
			// transmogrifier.
			transmogrifier.setPreserveLexicalValues(preserveLexicalValues);
			transmogrifier.setPreserveWhitespaces(preserveWhitespace);

			// Set the number of elements processed as a block.
			if (blockSize != 1000000)
				transmogrifier.setBlockSize(blockSize);

			// Set the maximum length for values stored in the String Table for
			// reuse.
			if (maxValueLength > -1)
				transmogrifier.setValueMaxLength(maxValueLength);

			// Set the maximum number of values stored in the String Table.
			if (maxValuePartitions > -1)
				transmogrifier.setValuePartitionCapacity(maxValuePartitions);

			in = new ByteArrayInputStream(
					stringToBeEncoded.getBytes(StandardCharsets.UTF_8));
			out = new ByteArrayOutputStream();

			// 3. Set the schema and EXI options in the Grammar Cache.
			FileInputStream fis = null;

			// Create a schema and set it to null. If useSchema == "None" it
			// remains null.
			EXISchema schema = null;

			// If using an XSD, it must be converted as an EXISchema.
			if (useSchema.equals("XSD")) {
				try {
					InputSource is = new InputSource(schemaFileName);
					EXISchemaFactory factory = new EXISchemaFactory();
					schema = factory.compile(is);
				} finally {
				}
			}
			// If using an EXIG, it can be read directly.
			else if (useSchema.equals("EXIG")) {
				try {
					fis = new FileInputStream(exiSchemaFileName);
					schema = new EXISchemaReader().parse(fis);
				} finally {
					if (fis != null)
						fis.close();
				}
			} else if (!"None".equals(useSchema))
				assert false;
			grammarCache = new GrammarCache(schema, options);

			// 4. Set the configuration options in the Transmogrifier.
			transmogrifier.setGrammarCache(grammarCache);

			// 5. Set the output stream.
			transmogrifier.setOutputStream(out);

			// 6. Encode the input stream.
			transmogrifier.encode(new InputSource(in));

		} finally {

			if (in != null) {
				in.close();
			}
			if (out != null) {
				bin = out.toByteArray();
				out.close();
			}
		}
		return bin;
    	}catch(Exception e) {
    		Logger.error(e);
    		return new byte[0];
    	}
	}
	
}
