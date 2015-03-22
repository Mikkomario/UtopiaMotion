package motion_test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import genesis_event.ActorHandler;
import genesis_event.DrawableHandler;
import genesis_event.HandlerRelay;
import genesis_event.MouseListenerHandler;
import genesis_util.Vector3D;
import genesis_video.GamePanel;
import genesis_video.GameWindow;

/**
 * This class tests the basics of momentums
 * @author Mikko Hilpinen
 * @since 19.3.2015
 */
public class MotionDiskTest
{
	// CONSTRUCTOR	--------------------
	
	private MotionDiskTest()
	{
		// The interface is static
	}

	
	// MAIN METHOD	-------------------
	
	/**
	 * Starts the test
	 * @param args Not used
	 */
	public static void main(String[] args)
	{
		Vector3D windowSize = new Vector3D(500, 500);
		
		GameWindow window = new GameWindow(windowSize, "Motion Test 2", true, 120, 20);
		GamePanel panel = window.getMainPanel().addGamePanel();
		
		HandlerRelay handlers = new HandlerRelay();
		handlers.addHandler(new DrawableHandler(false, panel.getDrawer()));
		handlers.addHandler(new ActorHandler(false, window.getHandlerRelay()));
		handlers.addHandler(new MouseListenerHandler(false, window.getHandlerRelay()));
		
		List<TestDisk> disks = new ArrayList<>();
		disks.add(new TestDisk(handlers, Color.BLACK, new Vector3D(100, 200), 10, windowSize));
		disks.add(new TestDisk(handlers, Color.BLACK, new Vector3D(400, 400), 30, windowSize));
		disks.add(new TestDisk(handlers, Color.BLACK, new Vector3D(100, 400), 50, windowSize));
		
		new TestPushDisk(handlers, windowSize.dividedBy(2), windowSize, disks);
	}
}
