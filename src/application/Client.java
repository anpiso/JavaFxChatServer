package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	Socket socket;
	// 통신을 위한 소켓을 만들었다.

	public Client(Socket socket) {
		this.socket = socket;	
		receive();	
	}

	//클라이언트로부터 메세지를 전달받기 위한 메소드
	public void receive() {
		Runnable thread = new Runnable() {
			@Override 
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();	//소켓에서 데이터를 inputStream으로 리턴을 받아서 inputStream형 in객체를 생성
						byte[] buffer = new byte[512];				//byte형 배열인 buffer를 최대 512바이트까지 받을 수 있도록 객체 생성.
						
						int length = in.read(buffer);				//inputStream으로부터 읽어들인 바이트(데이터)를 buffer에 저장하고 실제 읽어들인 바이트 수를 리턴하여 length에 저장
						if(length == -1) 							//메세지를 읽어들일 때 오류가 발생했다면
							throw new IOException();				//IOExeption오류를 발생시킴
						System.out.println("수신 성공"					
								+ socket.getRemoteSocketAddress()	//현재 접속 한 클라이언트의 정보를 출력하도록 한다.
								+ ":" 
								+ Thread.currentThread().getName());	//현재 스레드의 이름을 출력하게 한다.
						String msg = new String(buffer, 0, length, "UTF-8");//buffer에서 전달받은 내용을 length만큼의 길이로 UTF-8로 인코딩하여 msg문자열을 생성
						
						for(Client client : Main.clients) {	//다른 클라이언트들에게 클라이언트에서 전달받은 메세지를 보여준다
							client.send(msg);
						}
					}
				}catch(Exception e) {
					e.printStackTrace();
					try {
						System.out.println("메세지 수신 오류"
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
		Main.threadPool.submit(thread);	//Main클래스에서 만든 threadPool에 위에서 만든 thread를 넘겨서 threadPool에 추가함 
	}
	
	//클라이언트에게 메세지를 전송하는 메소드
	public void send(String msg) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();	//socket에서 메세지를 내보내기 위한 OutputStream객체 생성
					byte[] buffer = msg.getBytes("UTF-8");			//문자열 객체인 msg를 UTF-8로 인코딩하고 byte형식으로 변환하여 buffer배열 생성 
					out.write(buffer);								//buffer에 있는 길이만큼의 바이트를 OutputStream에 쓴다.
					out.flush();									//그리고 OutputStream에 있는 버퍼를 비워준다.
				}catch(Exception e){
					try {
						System.out.println("메세지 송신 오류"
								+ socket.getRemoteSocketAddress()
								+ ":"
								+ Thread.currentThread().getName());
						Main.clients.remove(Client.this);			//오류가 발생한 클라이언트를 삭제시키고
						socket.close();								//오류가 발생한 클라이언트에 대한 소켓을 닫아버린다.
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
}


