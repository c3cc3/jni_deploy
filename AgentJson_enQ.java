// 큐 헨드러가 동작하고 있는 서버에 데이터를 enQ 하는 예제소스
// serverAddress 를 확인하고 수정 후 컴파일하세요.
// enQ 하는 데이터는 현재시간을 보냅니다.

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
//
// 현재시간획득
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AgentJson_enQ {
	private static String sessionId = null;
    public static void main(String[] args) {
		System.out.println("args.length=" + args.length);
		for( int i=0; i<args.length; i++) {
			System.out.println(String.format("Command line argument %d is %s.", i , args[i]));
		}

		if( args.length != 2 ) {
			System.out.println("Usage  : $ java [server_ip] [port] <enter>\n");
			System.out.println("Example: $ java 172.30.9.34 7777 <enter>\n");
			return;
		}

		String serverAddress = args[0];
        Integer serverPort = Integer.valueOf(args[1]);
        // String serverAddress = "172.30.9.34"; // 서버 주소
        // String serverAddress = "172.30.9.25"; // 서버 주소
        // int serverPort = 7777; // 서버 포트

		Integer testCount=0;

        try (Socket socket = new Socket(serverAddress, serverPort);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {


			 socket.setKeepAlive(true);

			// Make a Link and requeue JSON
            sendRequest(out, createInitialRequest());

            // 서버 응답 받기
            JSONObject initialResponse = receiveResponse(in);
            String result = initialResponse.getString("RESULT");

            if ("OK".equals(result)) {
                System.out.println("서버와의 초기 연결이 성공했습니다.");
				// 세션 ID 저장
                sessionId = initialResponse.getString("SESSION_ID");

                // 이후 데이터 주고받기 작업 반복
				while(true) {
					if (socket.isClosed() ) {
					    System.out.println("연결된 소켓이 닫혔습니다. 프로그램을 종료합니다.");
						break;
					}

                    // 데이터 만들고, 요청 보내기
                    sendRequest(out, createDataRequest());

                    // 서버 응답 받기
                    JSONObject dataResponse = receiveResponse(in);
                    System.out.println("서버로부터 받은 응답: " + dataResponse.toString(2));
					
					// 결과 처리
                    int rc = processResponse(dataResponse);
					if(rc == 1) {
						// 재요청 대기
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				    System.out.println(testCount++ + "-th");
                }
            } else {
                System.out.println("서버 응답이 실패했습니다.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
	/*
	private static void sendRequest(OutputStream out, JSONObject requestJson) throws IOException {
		// 세션 ID를 요청에 추가
        if (sessionId != null) {
            requestJson.put("SESSION_ID", sessionId);
        }

        // JSON 객체를 문자열로 변환
        String requestJsonString = requestJson.toString();
        byte[] requestBytes = requestJsonString.getBytes();

        // 헤더에 바디 길이 추가
        byte[] header = ByteBuffer.allocate(4).putInt(requestBytes.length).array();

        // 서버로 헤더와 바디 전송
        out.write(header);
        out.write(requestBytes);
        out.flush();
    }
	*/
	private static void sendRequest(OutputStream out, JSONObject requestJson) throws IOException {
        if (sessionId != null) {
            requestJson.put("SESSION_ID", sessionId);
        }

        byte[] requestBytes = requestJson.toString().getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(4 + requestBytes.length);
        buffer.putInt(requestBytes.length);
        buffer.put(requestBytes);

        out.write(buffer.array());
        out.flush();
    }



	/*
    private static JSONObject receiveResponse(InputStream in) throws IOException {
        // 서버로부터의 응답을 읽기
        byte[] responseHeader = new byte[4];
        in.read(responseHeader);
        int responseBodyLength = ByteBuffer.wrap(responseHeader).getInt();
        byte[] responseBody = new byte[responseBodyLength];
        in.read(responseBody);
        String responseJsonString = new String(responseBody);

        // JSON 문자열을 JSON 객체로 변환
        return new JSONObject(responseJsonString);
    }
	*/
	private static JSONObject receiveResponse(InputStream in) throws IOException {
        byte[] responseHeader = new byte[4];
        in.read(responseHeader);
        int responseBodyLength = ByteBuffer.wrap(responseHeader).getInt();
        byte[] responseBody = new byte[responseBodyLength];
        in.read(responseBody);
        return new JSONObject(new String(responseBody));
    }


    private static JSONObject createInitialRequest() {
		// JSON 객체 생성
		JSONObject requestJson = new JSONObject();

		requestJson.put("FQP_VERSION", "10");
		requestJson.put("SESSION_ID", "");
		requestJson.put("QUEUE_PATH", "/umsdata1/enmq");
		requestJson.put("QUEUE_NAME", "TST");
		requestJson.put("ACK_MODE", "Y");
		requestJson.put("ACTION", "LINK");
		requestJson.put("MSG_LENGTH", 0);
		requestJson.put("MESSAGE", "");

		// requestJson.put("nested", new JSONObject().put("nestedKey", "nestedValue"));
        return requestJson;
    }

    private static JSONObject createDataRequest() {
		// JSON 객체 생성
		JSONObject requestJson = new JSONObject();

		requestJson.put("FQP_VERSION", "10");
		requestJson.put("QUEUE_PATH", "/umsdata1/enmq");
		requestJson.put("QUEUE_NAME", "TST");
		requestJson.put("ACK_MODE", "Y");
		// requestJson.put("ACTION", "DEQU");
		requestJson.put("ACTION", "ENQU");


		// 현재 날짜와 시간을 가져옴
        LocalDateTime now = LocalDateTime.now();
        // DateTimeFormatter를 사용하여 형식 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        // 형식에 맞게 포맷팅
        String formattedDateTime = now.format(formatter);
        System.out.println("현재 날짜와 시간: " + formattedDateTime);

		requestJson.put("MESSAGE", formattedDateTime);

		int data_length = formattedDateTime.length();

		// MSG_LENGTH 에는 json number 형식으로 넣는다.
		requestJson.put("MSG_LENGTH", data_length);

		// requestJson.put("nested", new JSONObject().put("nestedKey", "nestedValue"));
        return requestJson;
    }

	private static int processResponse(JSONObject response) {
        String result = response.getString("RESULT");

		System.out.println("RESULT:" + result);

        if ("OK".equals(result)) {
            System.out.println("큐에 저장을 성공했습니다.");
			return 0;
        } else if ("FULL".equals(result)) {
            System.out.println("큐에 데이터가 꽉 차 있습니다(full). 1초 대기 후 재요청합니다.");
			return 1;
        } else {
            System.out.println("서버에 enQ 과정이 실패했습니다. 클라이언트 종료합니다.");
            System.exit(0);
        }
		return 1;
    }
}
