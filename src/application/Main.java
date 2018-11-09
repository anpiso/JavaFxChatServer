package application;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;


public class Main extends Application {

	public static ExecutorService threadPool;	//ExecutorService를 이용해서 스레드풀을 두고 스레드를 제한함
	//스레드의 수에 제한을 두기 때문에 클라이언트가 갑자기 많아지더라도 스레드 수에 제한을 두기 때문에 성능 저하를 막을 수 있다.
	
	public static Vector<Client> clients = new Vector<Client>();
	//접속한 클라이언트를 벡터 클래스를 통해 관리하도록 함
	//Vector클래스는 일종의 배열 클래스로 ArrayList나 LinkedList처럼 사용할 수 있으며, 같은 Collection인터페이스를 상속받아 구현하고있다.

	ServerSocket serverSocket;
	

	//클라이언트의 연결을 기다리는 메소드
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();	//서버소켓 객체를 만들고,
			serverSocket.bind(new InetSocketAddress(IP,port));	//서버소켓에 특정 IP와 port번호를 생성하여 바인드시킨다

		}catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {	//오류가 발생했을 때 서버가 닫혀있는 상태가 아니라면
				stopServer();				//서버를 닫는다.
			}
			return;
		}
		
		//클라이언트가 접속할 때 까지 계속 기다리는 스레드
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();	//클라이언트의 서버 접속을 대기해서 소켓을 생성한다.
						clients.add(new Client(socket));		//클라이언트를 Vector클래스로 만든 clients배열에 추가하여 관리하게 한다.
						System.out.println("클라이언트 접속"
								+ socket.getRemoteSocketAddress()	//접속한 클라이언트의 주소를 출혁
								+ ": "
								+ Thread.currentThread().getName());	//해당 클라이언트의 이름을 출력
					}catch(Exception e) {
						if(!serverSocket.isClosed()) {		//오류가 발생했을 때 서버가 닫혀있는 상태가 아니라면
							stopServer();					//서버를 닫는다.
						}
						break;
					}
				}
			}
		};
		
		threadPool = Executors.newCachedThreadPool();	//threadPool을 초기화한 뒤
		threadPool.submit(thread);						//threadPool에 위에서 만든 스레드를 추가시켜준다.

	}

	//서버 작동 중지
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();	//Vector클래스로 만든 clients에 접근하기 위한 iterator를 생성
			while(iterator.hasNext()) {						//하나하나의 클라이언트에 접근하여
				Client client = iterator.next();			
				client.socket.close();						//클라이언트의 소켓을 닫아버리고
				iterator.remove();							//iterator에서 지워버린다.
			}
			
			//서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {	//서버소켓이 null값이 아니고 서버소켓이 열려있는 상태라면 
				serverSocket.close();								//서버소켓을 닫는다.
			}
			//스레드풀 종료
			if(threadPool != null && !serverSocket.isClosed()) {	//스레드풀에 어떠한 스레드도 없고 서버소켓이 열려있는 상태라면
				threadPool.shutdown();								//스레드풀에서 대기중인 작업을 모두 중지하고 스레드풀을 반납한다.
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	//UI생성하고 프로그램 동작시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("굴림", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";	//localhost주소 
		int port = 8008;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String msg = String.format("[서버 시작]\n", IP, port);
					textArea.appendText(msg);
					toggleButton.setText("종료");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String msg = String.format("[서버 종료]\n", IP, port);
					textArea.appendText(msg);
					toggleButton.setText("시작");
				});
			}
		});
		
		
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("채팅 서버");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}


	//프로그램 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
