package socket;

import communication.Message;
import communication.RequestParser;
import factory.GameFactory;
import gamecore.entity.Entity;

public class SocketService implements UserService{
	private GameFactory factory;
	private RequestParser requestParser;
	

	public SocketService(GameFactory factory) {
		this.factory = factory;
		requestParser = factory.createRequestParser();
	}


	@Override
	public void run() {
		// TODO ��ťI/O
		
	}
	
	@Override
	public void respond(Message<? super Entity> message) {
		// TODO Auto-generated method stub
		
	}

}
