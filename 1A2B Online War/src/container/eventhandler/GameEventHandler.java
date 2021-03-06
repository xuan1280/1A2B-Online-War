package container.eventhandler;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import container.core.Client;
import container.protocol.Protocol;
import container.protocol.ProtocolFactory;
import gamecore.GameCore;
import gamecore.entity.GameRoom;
import gamecore.entity.Player;
import gamecore.model.ErrorMessage;
import gamecore.model.RequestStatus;

public abstract class GameEventHandler<In, Out> implements EventHandler{
	private static Logger log = LogManager.getLogger(GameEventHandler.class);
	private OnRespondingListener onRespondingListener;
	private ProtocolFactory protocolFactory;
	private Client client;
	private GameCore gameCore;
	private Protocol request;
	
	/**
	 * The handler deals with each specific request from the client socket.
	 * @param client the client socket the request from.
	 * @param request the request with (event, status, data) message complying with the certain protocol sent by the client.
	 * @param gameCore the online game core where to handle all the logics with the games.
	 * @param protocolFactory the factory which help you create a certain protocol with the specific messages.
	 */
	public GameEventHandler(Client client, 
							Protocol request, 
							GameCore gameCore, 
							ProtocolFactory protocolFactory) {
		this.client = client;
		this.protocolFactory = protocolFactory;
		this.request = request;
		this.gameCore = gameCore;
	}
	
	@Override
	public void setOnRespondingListener(OnRespondingListener onRespondingListener) {
		this.onRespondingListener = onRespondingListener;
	}
	@Override
	public void handle() {
		In data = parseData(request.getData());
		Response response = onHandling(data);
		response.handleTheResponse();
	}
	
	protected abstract String dataToString(Object data);
	protected abstract In parseData(String data);
	
	
	/**
	 * The method which mainly handle the request from the client. You need to validate the data to
	 * deal with all potential exceptions. If the data is invalid, just simply throw an exception. 
	 * The exception will be binded to the method where has an annotation @
	 * @return the response contains a successful message or a failed message complying with the certain protocol 
	 */
	protected abstract Response onHandling(In data);
	protected abstract void onRespondSuccessfulProtocol(Protocol responseProtocol);
	
	
	@Override
	public Protocol request() {
		return request;
	}

	protected ProtocolFactory protocolFactory() {
		return protocolFactory;
	}

	protected Client client() {
		return client;
	}

	protected GameCore gameCore() {
		return gameCore;
	}
	
	protected Response success(Out data){
		Protocol response = protocolFactory().createProtocol(request().getEvent(), RequestStatus.success.toString(),
				dataToString(data));
		return new SuccessResponse(response);
	}
	
	/**
	 * If you invoke this method, first the error will be converted as a protocol message thereby sent to the client as default.
	 * @param code the code stands for a thrown exception
	 * @param exception the exception
	 */
	protected Response error(int code, Exception exception){
		ErrorMessage errorMessage = createErrorMessage(code, exception);
		Protocol response = protocolFactory().createProtocol(request().getEvent(), RequestStatus.failed.toString(), 
				dataToString(errorMessage));
		return new ErrorResponse(response);
	}
	
	protected ErrorMessage createErrorMessage(int code, Exception exception){
		return new ErrorMessage(code, exception.getMessage());
	}

	protected interface Response{
		Protocol getResponse();
		void handleTheResponse();
	}
	
	protected class SuccessResponse implements Response{
		private Protocol response;
		public SuccessResponse(Protocol response) {
			this.response = response;
		}
		@Override
		public Protocol getResponse() {
			return response;
		}
		@Override
		public void handleTheResponse() {
			// invoke the handler's method to respond as a successful way.
			if (onRespondingListener != null)
				onRespondingListener.onSuccessResponding(response);
			onRespondSuccessfulProtocol(response);
		}
	}
	
	protected class ErrorResponse implements Response{
		private Protocol response;
		
		public ErrorResponse(Protocol response) {
			this.response = response;
		}
		@Override
		public Protocol getResponse() {		
			return response;
		}
		@Override
		public void handleTheResponse() {
			if (onRespondingListener != null)
				onRespondingListener.onErrorResponding(response);
			log.debug("Error responding: " + response);
			client().broadcast(response);
		}
	}
	
	protected boolean hasThePlayerJoinedAnotherRoom(Player player){
		for(GameRoom gameRoom : gameCore().getGameRooms())
			if (gameRoom.containsPlayer(player))
				return true;
		return false;
	}
}
