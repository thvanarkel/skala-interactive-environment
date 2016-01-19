package skala;

public interface SkalaListener {

	public void onSkalaStateTransition(Skala skala, Skala.State from, Skala.State to);
	
}
