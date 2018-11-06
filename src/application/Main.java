package application;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {

	public static ExecutorService threadPool;	//스레드를 제한함
	public static Vector<Client> clients = new Vector<Client>();

	ServerSocket serverSocket;

	//클라이언트의 연결을 기다리는 메소드
	public void stratServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP,port));

		}catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return; 
		}
		//클라이언트가 접속할 때 가지 계속 기다리는 스레드
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("클라이언트 접속"
								+ socket.getRemoteSocketAddress()
								+ ": "
								+ Thread.currentThread().getName());
					}catch(Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);

	}

	//서버 작동 준비
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			//서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//스레드풀 종료
			if(threadPool != null && !serverSocket.isClosed()) {
				threadPool.shutdown();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	//UI생성하고 프로그램 동작시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		
	}


	//프로그램 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
