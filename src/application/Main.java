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

	public static ExecutorService threadPool;	//ExecutorService�� �̿��ؼ� ������Ǯ�� �ΰ� �����带 ������
	//�������� ���� ������ �α� ������ Ŭ���̾�Ʈ�� ���ڱ� ���������� ������ ���� ������ �α� ������ ���� ���ϸ� ���� �� �ִ�.
	
	public static Vector<Client> clients = new Vector<Client>();
	//������ Ŭ���̾�Ʈ�� ���� Ŭ������ ���� �����ϵ��� ��
	//VectorŬ������ ������ �迭 Ŭ������ ArrayList�� LinkedListó�� ����� �� ������, ���� Collection�������̽��� ��ӹ޾� �����ϰ��ִ�.

	ServerSocket serverSocket;
	

	//Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();	//�������� ��ü�� �����,
			serverSocket.bind(new InetSocketAddress(IP,port));	//�������Ͽ� Ư�� IP�� port��ȣ�� �����Ͽ� ���ε��Ų��

		}catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {	//������ �߻����� �� ������ �����ִ� ���°� �ƴ϶��
				stopServer();				//������ �ݴ´�.
			}
			return;
		}
		
		//Ŭ���̾�Ʈ�� ������ �� ���� ��� ��ٸ��� ������
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();	//Ŭ���̾�Ʈ�� ���� ������ ����ؼ� ������ �����Ѵ�.
						clients.add(new Client(socket));		//Ŭ���̾�Ʈ�� VectorŬ������ ���� clients�迭�� �߰��Ͽ� �����ϰ� �Ѵ�.
						System.out.println("Ŭ���̾�Ʈ ����"
								+ socket.getRemoteSocketAddress()	//������ Ŭ���̾�Ʈ�� �ּҸ� ����
								+ ": "
								+ Thread.currentThread().getName());	//�ش� Ŭ���̾�Ʈ�� �̸��� ���
					}catch(Exception e) {
						if(!serverSocket.isClosed()) {		//������ �߻����� �� ������ �����ִ� ���°� �ƴ϶��
							stopServer();					//������ �ݴ´�.
						}
						break;
					}
				}
			}
		};
		
		threadPool = Executors.newCachedThreadPool();	//threadPool�� �ʱ�ȭ�� ��
		threadPool.submit(thread);						//threadPool�� ������ ���� �����带 �߰������ش�.

	}

	//���� �۵� ����
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();	//VectorŬ������ ���� clients�� �����ϱ� ���� iterator�� ����
			while(iterator.hasNext()) {						//�ϳ��ϳ��� Ŭ���̾�Ʈ�� �����Ͽ�
				Client client = iterator.next();			
				client.socket.close();						//Ŭ���̾�Ʈ�� ������ �ݾƹ�����
				iterator.remove();							//iterator���� ����������.
			}
			
			//���� ���� ��ü �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {	//���������� null���� �ƴϰ� ���������� �����ִ� ���¶�� 
				serverSocket.close();								//���������� �ݴ´�.
			}
			//������Ǯ ����
			if(threadPool != null && !serverSocket.isClosed()) {	//������Ǯ�� ��� �����嵵 ���� ���������� �����ִ� ���¶��
				threadPool.shutdown();								//������Ǯ���� ������� �۾��� ��� �����ϰ� ������Ǯ�� �ݳ��Ѵ�.
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	//UI�����ϰ� ���α׷� ���۽�Ű�� �޼ҵ�
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("����", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";	//localhost�ּ� 
		int port = 8008;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String msg = String.format("[���� ����]\n", IP, port);
					textArea.appendText(msg);
					toggleButton.setText("����");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String msg = String.format("[���� ����]\n", IP, port);
					textArea.appendText(msg);
					toggleButton.setText("����");
				});
			}
		});
		
		
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("ä�� ����");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}


	//���α׷� ������
	public static void main(String[] args) {
		launch(args);
	}
}
