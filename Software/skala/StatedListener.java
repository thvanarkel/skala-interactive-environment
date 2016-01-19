package skala;

public interface StatedListener <S> {
	
	public void onStateTransitionEvent(Object origin, S from, S to);
}
