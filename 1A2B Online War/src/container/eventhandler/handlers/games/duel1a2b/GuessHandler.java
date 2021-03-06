package container.eventhandler.handlers.games.duel1a2b;

import container.core.Client;
import container.eventhandler.handlers.GsonEventHandler;
import container.protocol.Protocol;
import container.protocol.ProtocolFactory;
import gamecore.GameCore;
import gamecore.entity.GameRoom;
import gamecore.model.ContentModel;
import gamecore.model.RoomStatus;
import gamecore.model.games.ProcessInvalidException;
import gamecore.model.games.a1b2.core.NumberNotValidException;
import gamecore.model.games.a1b2.duel.imp.Duel1A2BGame;

/**
 * @author Waterball
 * Input: Guess model.
 * Output: (Client) Guess model.
 */
public class GuessHandler extends GsonEventHandler<ContentModel, ContentModel>{
	
	public GuessHandler(Client client, Protocol request, GameCore gameCore, ProtocolFactory protocolFactory) {
		super(client, request, gameCore, protocolFactory);
	}

	@Override
	protected Class<ContentModel> getDataClass() {
		return ContentModel.class;
	}

	@Override
	protected Response onHandling(ContentModel guessModel) {
		try{
			GameRoom gameRoom = gameCore().getGameRoom(guessModel.getRoomId());
			Duel1A2BGame gameModel = (Duel1A2BGame) gameRoom.getGame();
			gameModel.guess(guessModel.getPlayerId(), guessModel.getContent());
			return success(guessModel);
		}catch (NumberNotValidException|ProcessInvalidException e) {
			return error(400, e);
		}
	}

	@Override
	protected void onRespondSuccessfulProtocol(Protocol responseProtocol) {
		client().broadcast(responseProtocol);
	}

}
