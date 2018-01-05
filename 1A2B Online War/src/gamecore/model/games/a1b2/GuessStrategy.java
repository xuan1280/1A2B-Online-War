package gamecore.model.games.a1b2;

public interface GuessStrategy {
	void feedRecord(GuessRecord guessRecord);
	String nextGuess();
}
