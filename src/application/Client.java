package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	Socket socket;
	// ����� ���� ������ �������.

	public Client(Socket socket) {
		this.socket = socket;	
		receive();	
	}

	//Ŭ���̾�Ʈ�κ��� �޼����� ���޹ޱ� ���� �޼ҵ�
	public void receive() {
		Runnable thread = new Runnable() {
			@Override 
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();	//���Ͽ��� �����͸� inputStream���� ������ �޾Ƽ� inputStream�� in��ü�� ����
						byte[] buffer = new byte[512];				//byte�� �迭�� buffer�� �ִ� 512����Ʈ���� ���� �� �ֵ��� ��ü ����.
						
						int length = in.read(buffer);				//inputStream���κ��� �о���� ����Ʈ(������)�� buffer�� �����ϰ� ���� �о���� ����Ʈ ���� �����Ͽ� length�� ����
						if(length == -1) 							//�޼����� �о���� �� ������ �߻��ߴٸ�
							throw new IOException();				//IOExeption������ �߻���Ŵ
						System.out.println("���� ����"					
								+ socket.getRemoteSocketAddress()	//���� ���� �� Ŭ���̾�Ʈ�� ������ ����ϵ��� �Ѵ�.
								+ ":" 
								+ Thread.currentThread().getName());	//���� �������� �̸��� ����ϰ� �Ѵ�.
						String msg = new String(buffer, 0, length, "UTF-8");//buffer���� ���޹��� ������ length��ŭ�� ���̷� UTF-8�� ���ڵ��Ͽ� msg���ڿ��� ����
						
						for(Client client : Main.clients) {	//�ٸ� Ŭ���̾�Ʈ�鿡�� Ŭ���̾�Ʈ���� ���޹��� �޼����� �����ش�
							client.send(msg);
						}
					}
				}catch(Exception e) {
					e.printStackTrace();
					try {
						System.out.println("�޼��� ���� ����"
								+  socket.getRemoteSocketAddress()	
								+ ":"
								+ Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					}catch(Exception e2) {
						e2.printStackTrace();

					}
				}
			}

		};
		Main.threadPool.submit(thread);	//MainŬ�������� ���� threadPool�� ������ ���� thread�� �Ѱܼ� threadPool�� �߰��� 
	}
	
	//Ŭ���̾�Ʈ���� �޼����� �����ϴ� �޼ҵ�
	public void send(String msg) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();	//socket���� �޼����� �������� ���� OutputStream��ü ����
					byte[] buffer = msg.getBytes("UTF-8");			//���ڿ� ��ü�� msg�� UTF-8�� ���ڵ��ϰ� byte�������� ��ȯ�Ͽ� buffer�迭 ���� 
					out.write(buffer);								//buffer�� �ִ� ���̸�ŭ�� ����Ʈ�� OutputStream�� ����.
					out.flush();									//�׸��� OutputStream�� �ִ� ���۸� ����ش�.
				}catch(Exception e){
					try {
						System.out.println("�޼��� �۽� ����"
								+ socket.getRemoteSocketAddress()
								+ ":"
								+ Thread.currentThread().getName());
						Main.clients.remove(Client.this);			//������ �߻��� Ŭ���̾�Ʈ�� ������Ű��
						socket.close();								//������ �߻��� Ŭ���̾�Ʈ�� ���� ������ �ݾƹ�����.
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
}


