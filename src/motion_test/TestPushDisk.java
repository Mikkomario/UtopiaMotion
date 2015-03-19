package motion_test;

import java.awt.Color;
import java.util.List;

import omega_util.Transformable;
import omega_util.Transformation;
import genesis_event.Actor;
import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_event.MouseEvent;
import genesis_event.MouseEvent.MouseButton;
import genesis_event.MouseEvent.MouseButtonEventType;
import genesis_event.MouseListener;
import genesis_event.StrictEventSelector;
import genesis_util.HelpMath;
import genesis_util.StateOperator;
import genesis_util.Vector2D;

/**
 * This is a special disk that reacts to the mouse and is able to push other disks around
 * @author Mikko Hilpinen
 * @since 19.3.2015
 */
public class TestPushDisk extends TestDisk implements Actor, MouseListener
{
	// ATTRIBUTES	------------------------
	
	private EventSelector<MouseEvent> selector;
	private List<TestDisk> disks;
	
	
	// CONSTRUCTOR	------------------------
	
	/**
	 * Creates a new push disk
	 * @param handlers The handlers that will handle the disk
	 * @param position The position of the disk's center
	 * @param windowSize The size of the program window
	 * @param otherDisks A list that contains the disks this one can collide with
	 */
	public TestPushDisk(HandlerRelay handlers, Vector2D position, Vector2D windowSize, 
			List<TestDisk> otherDisks)
	{
		super(handlers, Color.RED, position, 30, windowSize);
		
		StrictEventSelector<MouseEvent, MouseEvent.Feature> s = 
				MouseEvent.createMouseButtonSelector(MouseButton.LEFT);
		s.addRequiredFeature(MouseButtonEventType.PRESSED);
		
		this.selector = s;
		this.disks = otherDisks;
	}
	
	
	// IMPLEMENTED METHODS	----------------

	@Override
	public StateOperator getListensToMouseEventsOperator()
	{
		return getIsActiveStateOperator();
	}

	@Override
	public EventSelector<MouseEvent> getMouseEventSelector()
	{
		return this.selector;
	}

	@Override
	public boolean isInAreaOfInterest(Vector2D position)
	{
		return HelpMath.pointDistance(position, getPosition()) < getRadius();
	}

	@Override
	public void onMouseEvent(MouseEvent e)
	{
		getMover().applyForce(getPosition().minus(e.getPosition()).times(200));
	}

	@Override
	public void act(double duration)
	{
		// Checks for collisions with the other disks
		for (TestDisk disk : this.disks)
		{
			double distance = HelpMath.pointDistance(getPosition(), disk.getPosition());
			if ( distance < getRadius() + disk.getRadius())
			{
				Vector2D mtv = getPosition().minus(disk.getPosition()).withLength(getRadius() + 
						disk.getRadius() - distance + 1);
				
				Transformable.transform(this, Transformation.transitionTransformation(mtv));
				getMover().handleCollisionWith(disk, duration, mtv);
			}
		}
		
		super.act(duration);
	}
}