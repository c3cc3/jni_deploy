import com.clang.fq.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class TestEnQ_loop {
	static {
   		System.loadLibrary("jfq"); // Load native library at runtime
		System.loadLibrary("fq"); // Load FileQueue Shared Library
	}
	private static String qpath;
    private static String qname;
    private static String qid;
    private static String logLevel;
    private static String logFileName;
    private static String testCount;
 
	public static void main(String[] args) {
		int rc;

		if (args.length != 1) {
            printUsage();
            return;
        }
        String configFilePath = args[0];

		System.out.println("args.length=" + args.length);
		for( int i=0; i<args.length; i++) {
			System.out.println(String.format("Command line argument %d is %s.", i , args[i]));
		}

		try {
			parseConfigFile(configFilePath); 
		}
		catch (Exception e) {
            e.printStackTrace();
        }

		int	count_int = Integer.parseInt(testCount);

		FileQueueJNI fq = new FileQueueJNI( Integer.parseInt(logLevel), logFileName, Integer.parseInt(qid), qpath, qname);

		rc = fq.open();
		if( rc < 0 ) {
			System.out.println("open failed: " + fq.logname + "," + fq.path + "," + fq.qname + "," + " rc: " + rc);
			return;
		}
		System.out.println("open success: " + fq.logname + "," + fq.path + "," + fq.qname + "," + " rc: " + rc);

		count_int = Integer.parseInt(testCount);

		for(int i=0; i < count_int; i++) { 
			String kind_media = "SM";
			String phone_no = "01072021516";
			String send_msg = "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567";
			String template = "Hello! my name is FileQueue";
			String var_data = "Choi|Gwisang";

			String SendData = kind_media+phone_no+send_msg+template+var_data;

			rc = fq.write( SendData );

			if( rc < 0 ) {
				System.out.println("Write(enQ) failed: " + fq.path + "," + fq.qname + "," + " rc: " + rc);
				fq.close();
				return;
			}
			else if( rc == 0 ) { // When queue is full, We retry enQ after 1 second.
				System.out.println("Queue is full: " + fq.path + "," + fq.qname + "," + " rc: " + rc);
				try {
					Thread.sleep(1); // Pause for 1 second (1000)
				}
				catch(InterruptedException ex) {
				        Thread.currentThread().interrupt();
			    }
				continue;
			}
			else { // Succes.
				long out_run_time = fq.get_out_run_time();

				System.out.println("Write(enQ) success: rc=" +  rc);

				try {
					Thread.sleep(1); // Pause for 1 second (1000)
				}
				catch(InterruptedException ex) {
				        Thread.currentThread().interrupt();
			    }

				continue;
			}
		} // for loop block
	}  // main() end block.

	private static void parseConfigFile(String configFilePath) throws Exception {
        File configFile = new File(configFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(configFile);
        doc.getDocumentElement().normalize();

        Element rootElement = doc.getDocumentElement();
        qpath = getElementValue(rootElement, "qpath");
        qname = getElementValue(rootElement, "qname");
        qid = getElementValue(rootElement, "qid");
        logLevel = getElementValue(rootElement, "log_level");
        logFileName = getElementValue(rootElement, "log_filename");
        testCount = getElementValue(rootElement, "test_count");

        printConfigValues();
    }

    private static void printConfigValues() {
        System.out.println("qpath: " + qpath);
        System.out.println("qname: " + qname);
        System.out.println("qid: " + qid);
        System.out.println("Log Level: " + logLevel);
        System.out.println("Log filename: " + logFileName);
        System.out.println("Test count: " + testCount);
    }

    private static void printUsage() {
        System.out.println("Usage: java TestDeQXA <configFilePath>");
    }

	private static String getElementValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        Node node = nodeList.item(0);
        return node.getTextContent();
    }
} // class block end.
