package motion_test;

import genesis_event.HandlerRelay;
import genesis_util.Vector2D;
import genesis_video.GamePanel;
import genesis_video.GameWindow;

/**
 * This class tests the basic moment method in objectRotator
 * @author Mikko Hilpinen
 * @since 21.3.2015
 */
public class MotionStickTest
{
	// CONSTRUCTOR	------------------------
	
	private MotionStickTest()
	{
		// The interface is static
	}

	
	// MAIN METHOD	------------------------
	
	/**
	 * Starts the test
	 * @param args Not used
	 */
	public static void main(String[] args)
	{
		GameWindow window = new GameWindow(new Vector2D(800, 600), "Motion test 3", true, 
				120, 20);
		GamePanel panel = window.getMainPanel().addGamePanel();
		
		HandlerRelay handlers = HandlerRelay.createDefaultHandlerRelay(window, panel);
		
		new TestStick(handlers);
	}
}
