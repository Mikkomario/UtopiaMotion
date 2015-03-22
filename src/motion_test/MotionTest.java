package motion_test;

import omega_util.Transformation;
import genesis_event.ActorHandler;
import genesis_event.DrawableHandler;
import genesis_event.HandlerRelay;
import genesis_event.MouseListenerHandler;
import genesis_util.Vector3D;
import genesis_video.GamePanel;
import genesis_video.GameWindow;

/**
 * This class tests the basic features introduced in this module
 * @author Mikko Hilpinen
 * @since 22.12.2014
 */
public class MotionTest
{
	// CONSTRUCTOR	--------------------------
	
	private MotionTest()
	{
		// The constructor is hidden since the interface is static
	}

	
	// MAIN METHOD	-------------------------
	
	/**
	 * Starts the test
	 * @param args not used
	 */
	public static void main(String[] args)
	{
		// Creates the window & panel
		GameWindow window = new GameWindow(new Vector3D(800, 600), "Motion test", true, 
				120, 20);
		GamePanel panel = window.getMainPanel().addGamePanel();
		
		// Creates the handlers
		HandlerRelay handlers = new HandlerRelay();
		handlers.addHandler(new DrawableHandler(false, panel.getDrawer()));
		handlers.addHandler(new ActorHandler(false, window.getStepHandler()));
		handlers.addHandler(new MouseListenerHandler(false, window.getHandlerRelay()));
		
		// Creates the test object(s)
		new TestMouseFollowerMovable(handlers).setTrasformation(
				Transformation.transitionTransformation(new Vector3D(400, 300)));
	}
}
