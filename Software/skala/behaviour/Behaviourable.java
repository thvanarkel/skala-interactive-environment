package skala.behaviour;

import java.util.HashMap;

public interface Behaviourable {

	public HashMap<String, Object> getData();
	
	public Object getData(String key);
	
	public void setData(HashMap<String, Object> data );
	
	public void setData(String key, Object value);
	
}
