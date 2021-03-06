package gamecore.model.games;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.junit.runner.Request;

import com.google.gson.Gson;

import container.core.Constants.Events.Games;
import container.core.MyLogger;
import container.protocol.Protocol;
import container.protocol.ProtocolFactory;
import gamecore.entity.GameRoom;
import gamecore.model.ClientPlayer;
import gamecore.model.GameMode;
import gamecore.model.MockLogger;
import gamecore.model.RequestStatus;
import utils.ForServer;

public abstract class Game{
	protected static transient Gson gson = new Gson();
	protected transient MyLogger log = new MockLogger();
	protected transient ProtocolFactory protocolFactory;
	protected transient Timer timer;
	protected transient GameLifecycleListener listener;
	protected transient GameEnteringWaitingBox enteringWaitingBox;
	protected transient GameRoom gameRoom;
	private transient Map<Integer, TimeListener> timeListeners = new TreeMap<>();
	protected GameMode gameMode;
	protected Date launchDate = new Date();
	protected String roomId;
	protected long gameDuration = 2000;
	protected boolean gameStarted = false;
	
	public Game(ProtocolFactory protocolFactory, GameMode gameMode, String roomId) {
		this.gameMode = gameMode;
		this.roomId = roomId;
		this.protocolFactory = protocolFactory;
		timer = new Timer(gameMode.toString());
	}

 	public Game(ProtocolFactory protocolFactory, GameMode gameMode, String roomId, Date luanchDate) {
 		this(protocolFactory, gameMode, roomId);
 		this.launchDate = luanchDate;
	}

 	@ForServer
 	public final synchronized void enterGame(ClientPlayer clientPlayer){
 		if (gameStarted)
 			throw new IllegalStateException("The game is started.");
 		log.trace("The player " + clientPlayer.getPlayerName() + " entered.");
 		enteringWaitingBox.enter(clientPlayer);
 	}
 	
 	public void setEnteringWaitingBox(GameEnteringWaitingBox enteringWaitingBox) {
		this.enteringWaitingBox = enteringWaitingBox;
	}

 	@ForServer
 	public final synchronized void startGame() {
 		onInitGame();
 		listener.onGameStarted(this, onCreateGameStartedProtocol());
 		log.trace("Game " + gameMode.toString() + " started.");
 		gameStarted = true;
 		startTimer();
 	}

 	protected void onInitGame(){/*hook*/};
 	
 	protected Protocol onCreateGameStartedProtocol(){
 		return protocolFactory.createProtocol(Games.GAMESTARTED, RequestStatus.success.toString(), null);
 	};
 	
 	
 	public void setLog(MyLogger log) {
		this.log = log;
	}
 	
	private void startTimer() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				gameDuration += 1000;
				if (timeListeners.containsKey(gameDuration))
					timeListeners.get(gameDuration).onTime(gameDuration);
			}
		}, 2000, 1000);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		log.trace("Game with the room id: " + roomId + " finalized.");
		timer.cancel();
	}
	
	protected synchronized void validateGameStarted(){
		if (!isGameStarted())
			throw new ProcessInvalidException("The game is not started.");
	}
	
	public interface TimeListener{
		public void onTime(long gameDuration);
	}
	
	public interface GameLifecycleListener {
		public void onGameStarted(Game game, Protocol gameStartedProcol);
		public void onGameInterrupted(Game game, ClientPlayer noResponsePlayer);
		public void onGameOver(Game game);
	}

	public Date getLaunchDate() {
		return launchDate;
	}
	
	public String getRoomId() {
		return roomId;
	}
	
	public GameMode getGameMode() {
		return gameMode;
	}
	
	public long getGameDuration() {
		return gameDuration;
	}
	
	public void setGameLifecycleListener(GameLifecycleListener lifecycleListener){
		this.listener = lifecycleListener;
	}
	
	public boolean isGameStarted() {
		return gameStarted;
	}
	
}
