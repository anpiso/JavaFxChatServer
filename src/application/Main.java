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

	public static ExecutorService threadPool;	//�����带 ������
	public static Vector<Client> clients = new Vector<Client>();

	ServerSocket serverSocket;

	//Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
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
		//Ŭ���̾�Ʈ�� ������ �� ���� ��� ��ٸ��� ������
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("Ŭ���̾�Ʈ ����"
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

	//���� �۵� �غ�
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			//���� ���� ��ü �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//������Ǯ ����
			if(threadPool != null && !serverSocket.isClosed()) {
				threadPool.shutdown();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	//UI�����ϰ� ���α׷� ���۽�Ű�� �޼ҵ�
	@Override
	public void start(Stage primaryStage) {
		
	}


	//���α׷� ������
	public static void main(String[] args) {
		launch(args);
	}
}
