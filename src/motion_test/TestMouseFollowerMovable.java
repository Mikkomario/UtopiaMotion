package motion_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import motion_movement.Movable;
import motion_movement.ObjectMover;
import motion_movement.ObjectRotator;
import motion_movement.Rotateable;
import genesis_event.Actor;
import genesis_event.Drawable;
import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_event.MouseEvent;
import genesis_event.MouseListener;
import genesis_util.SimpleHandled;
import genesis_util.Transformation;
import genesis_util.Vector3D;

/**
 * This object moves towards the mouse but stops after a certain point to the right
 * @author Mikko Hilpinen
 * @since 22.12.2014
 */
public class TestMouseFollowerMovable extends SimpleHandled implements
		Movable, MouseListener, Actor, Drawable, Rotateable
{
	// ATTRIBUTES	--------------------------
	
	private Transformation transformation;
	private EventSelector<MouseEvent> selector;
	private ObjectMover mover;
	private Vector3D lastMousePosition;
	private ObjectRotator rotator;
	
	
	// CONSTRUCTOR	---------------------------
	
	/**
	 * Creates a new object
	 * @param handlers The handlers that will handle the object
	 */
	public TestMouseFollowerMovable(HandlerRelay handlers)
	{
		super(handlers);
		
		this.transformation = new Transformation();
		this.selector = MouseEvent.createMouseMoveSelector();
		this.mover = new ObjectMover(this, handlers);
		this.lastMousePosition = Vector3D.zeroVector();
		this.rotator = new ObjectRotator(this, handlers);
	}
	
	
	// IMPLEMENTED METHODS	--------------------

	@Override
	public Transformation getTransformation()
	{
		return this.transformation;
	}

	@Override
	public void setTrasformation(Transformation t)
	{
		this.transformation = t;
	}

	@Override
	public EventSelector<MouseEvent> getMouseEventSelector()
	{
		return this.selector;
	}

	@Override
	public boolean isInAreaOfInterest(Vector3D position)
	{
		return false;
	}

	@Override
	public void onMouseEvent(MouseEvent event)
	{
		this.lastMousePosition = event.getPosition();
	}

	@Override
	public double getMass()
	{
		return 1;
	}

	@Override
	public ObjectMover getMover()
	{
		return this.mover;
	}

	@Override
	public void act(double duration)
	{
		//System.out.println(getTransformation().getPosition());
		// Moves towards the mouse
		if (getMover() != null && this.lastMousePosition != null && 
				getTransformation() != null && getRotator() != null)
		{
			getMover().applyForce(this.lastMousePosition.minus(
					getTransformation().getPosition()).times(duration * 0.002));
		
			getMover().applyForce(new Vector3D(0, duration * 0.01));
			
			// Stops if too far to the right
			if (getTransformation().getPosition().getFirst() > 500)
			{
				
				Vector3D p = getMover().getMomentum(new Vector3D(1, 0));
				Vector3D f = ObjectMover.getForceCausingMomentum(p, duration).reverse();
				
				getMover().applyFriction(0.6, duration, f.getLength(), new Vector3D(0, 1));
				getMover().applyForce(f);
				
				setTrasformation(getTransformation().withPosition(new Vector3D(500, 
						getTransformation().getPosition().getSecond())));
				//getMover().negateDirectionalVelocity(new Vector3D(1, 0));
			}
			
			// Changes the rotation speed
			getRotator().increaseRotation(getMover().getVelocity().getLength() * 0.01);
			getRotator().diminishRotation(0.05);
		}
	}

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		if (getTransformation() != null)
		{
			g2d.setColor(Color.BLACK);
			AffineTransform lastTransform = g2d.getTransform();
			getTransformation().transform(g2d);
			g2d.drawRect(-10, -10, 20, 20);
			//System.out.println("Drawing");
			g2d.setTransform(lastTransform);
		}
	}

	@Override
	public int getDepth()
	{
		return 0;
	}

	@Override
	public ObjectRotator getRotator()
	{
		return this.rotator;
	}

	@Override
	public double getDefaultMomentMass()
	{
		return Rotateable.getRectangleMomentMass(getMass(), 20, 20);
	}
}
