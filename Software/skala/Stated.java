package skala;

public interface Stated <S>{
	
	public S getState();
	public void setState(S state);
	
	void fireStateTransitionEvent(S from, S to);
}
