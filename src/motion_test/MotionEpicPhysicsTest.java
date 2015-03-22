package motion_test;

import conflict_collision.CollisionHandler;
import genesis_event.ActorHandler;
import genesis_event.DrawableHandler;
import genesis_event.HandlerRelay;
import genesis_event.MouseListenerHandler;
import genesis_util.Vector3D;
import genesis_video.GamePanel;
import genesis_video.GameWindow;

/**
 * This test tests the most advanced physics stuff this module has
 * @author Mikko Hilpinen
 * @since 22.3.2015
 */
public class MotionEpicPhysicsTest
{
	// CONSTRUCTOR	-----------------------
	
	private MotionEpicPhysicsTest()
	{
		// The interface is static
	}

	
	// MAIN METHOD	-----------------------
	
	/**
	 * Starts the test
	 * @param args Not used
	 */
	public static void main(String[] args)
	{
		Vector3D windowSize = new Vector3D(800, 600);
		GameWindow window = new GameWindow(windowSize, "Motion test 4", true, 120, 
				20);
		GamePanel panel = window.getMainPanel().addGamePanel();
		
		HandlerRelay handlers = new HandlerRelay();
		handlers.addHandler(new ActorHandler(false, window.getHandlerRelay()));
		handlers.addHandler(new MouseListenerHandler(false, window.getHandlerRelay()));
		handlers.addHandler(new DrawableHandler(false, panel.getDrawer()));
		new CollisionHandler(false, window.getHandlerRelay(), handlers);
		
		new TestPhysicObject(handlers, new Vector3D(150, 300), windowSize);
		new TestPhysicObject(handlers, new Vector3D(650, 300), windowSize);
	}
}
