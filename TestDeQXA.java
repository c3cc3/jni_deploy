import com.clang.fq.*; // FileQueue library
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/*
** Warning: max buffer size is 65536
*/
public class TestDeQXA {
	static {
		System.loadLibrary("jfq"); // Load native library at runtime
		System.loadLibrary("fq");  // Load FileQueue Shared Library
	}
	private static String qpath;
    private static String qname;
    private static String qid;
    private static String logLevel;
    private static String logFileName;
    private static String workingTime;
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

		int user_working_time_int = Integer.parseInt(workingTime);
		int	count_int = Integer.parseInt(testCount);
			
		// make a FileQueueJNI instance with naming test.
		// 3-th argument is loglevel. (0: trace, 1: debug, 2: info, 3: Warning, 4: error, 5: emerg, 6: request)
		// Use 1 on dev or 4 on real.
		//
		FileQueueJNI fq = new FileQueueJNI( Integer.parseInt(logLevel), logFileName, Integer.parseInt(qid), qpath, qname);

		rc = fq.open(); // Open File Queue
		if( rc < 0 ) {
			System.out.println("open failed: " + fq.logname + "," + qpath + "," + qname + "," + " rc: " + rc);
			return;
		}
		System.out.println("open success: " + fq.logname + "," + fq.path + "," + fq.qname + "," + " rc: " + rc);

		int deQ_count=0;
		for(;;) { // polling file queue.

			rc = fq.readXA(); // XA read 
			if( rc < 0 ) {
				if( rc == -99 ) {
					System.out.println("Manager Stop: " + fq.path + "," + fq.qname);
					try {
						Thread.sleep(1000); // Pause for 1 second
					}
					catch(InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
					continue;
				}
				else {
					System.out.println("readXA failed: " + fq.path + "," + fq.qname + "," + " rc: " + rc);
					break;
				}
			}
			else if( rc == 0 ) {
				System.out.println("empty: " + fq.path + "," + fq.qname + "," + " rc: " + rc);

				try {
                    Thread.sleep(1000); // Pause for 1 second
                }
                catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
				if( deQ_count >= count_int ) break;
				else continue;
			}
			else {
				String data = fq.get_out_msg();
				long out_seq = fq.get_out_seq();
				String out_unlink_filename = fq.get_out_unlink_filename();
				long out_run_time = fq.get_out_run_time();

				//System.out.println("read success: " +  fq.path + "," + fq.qname + "," + " rc: " + rc + " msg: " + data + " seq: " + out_seq + " unlink_filename: " + out_unlink_filename + " run_timme(micro seconds): " + out_run_time);

				// input your jobs in here
				//
				// We print data, length, sequence
				System.out.println("deQ_XA result:  success!");
				System.out.println("data: " + data);
				System.out.println("length: " + rc);
				System.out.println("sequence: " + out_seq);

				if( user_working_time_int > 0 ) {
					try {
						Thread.sleep(user_working_time_int); // Pause for 1 second
					}
					catch(InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}

				int your_job_result = 1;
				if( your_job_result == 1 ) {
					rc = fq.commitXA();
					System.out.println("commit success: rc: " + rc);
				}
				else { // abnormal data
					rc = fq.cancelXA();
					System.out.println("cancel success: rc: " + rc);
				}
				deQ_count++;
				continue;
			}
		}

		fq.close(); // Close only when the process terminates.
		return;
	} 
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
        workingTime = getElementValue(rootElement, "working_time");
        testCount = getElementValue(rootElement, "test_count");

        printConfigValues();
    }

    private static void printConfigValues() {
        System.out.println("qpath: " + qpath);
        System.out.println("qname: " + qname);
        System.out.println("qid: " + qid);
        System.out.println("Log Level: " + logLevel);
        System.out.println("Log filename: " + logFileName);
        System.out.println("working time(mili seconds): " + workingTime);
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
