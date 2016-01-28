package skala;

public interface LadderListener {
	public void onLadderStateTransitionEvent(Ladder origin, Ladder.State from, Ladder.State to);
}
