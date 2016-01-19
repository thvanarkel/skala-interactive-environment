package skala;

import edu.ufl.digitalworlds.j4k.DepthMap;
import edu.ufl.digitalworlds.j4k.Skeleton;

public interface KinectListener {

	public void handleDepthFrame(DepthMap depthMap, float[] XYZ);
	public void handleSkeletonEvent(Skeleton skeleton, double[][] realWorldPositions, int id);
	public void handleVideoFrame(byte[] data);
	public void handleIRFrame(byte[] data);	
	
}
