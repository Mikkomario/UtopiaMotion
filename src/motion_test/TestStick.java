package motion_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import genesis_event.Drawable;
import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_event.MouseEvent;
import genesis_event.MouseEvent.MouseButton;
import genesis_event.MouseEvent.MouseButtonEventType;
import genesis_event.MouseListener;
import genesis_event.StrictEventSelector;
import genesis_util.SimpleHandled;
import genesis_util.Transformation;
import genesis_util.Vector3D;
import motion_movement.ObjectRotator;
import motion_movement.Rotateable;

/**
 * This class is used for testing moments
 * @author Mikko Hilpinen
 * @since 21.3.2015
 */
public class TestStick extends SimpleHandled implements Rotateable, Drawable, MouseListener
{
	// ATTRIBUTES	------------------------------
	
	private int length;
	private Transformation transformation;
	private ObjectRotator rotator;
	private StrictEventSelector<MouseEvent, MouseEvent.Feature> selector;
	private Vector3D lastMousePosition, lastEffectPosition;
	
	
	// CONSTRUCTOR	------------------------------
	
	/**
	 * Creates a new test stick
	 * @param handlers The handlers that will handle the stick
	 */
	public TestStick(HandlerRelay handlers)
	{
		super(handlers);
		
		this.length = 200;
		this.transformation = new Transformation(new Vector3D(400, 300));
		this.rotator = new ObjectRotator(this, handlers);
		this.selector = MouseEvent.createButtonEventSelector();
		this.selector.addRequiredFeature(MouseButtonEventType.PRESSED);
		this.lastEffectPosition = Vector3D.zeroVector();
		this.lastMousePosition = Vector3D.zeroVector();
	}
	
	
	// IMPLEMENTED METHODS	----------------------

	@Override
	public double getMass()
	{
		return 3;
	}

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
	public ObjectRotator getRotator()
	{
		return this.rotator;
	}

	@Override
	public double getDefaultMomentMass()
	{
		return Rotateable.getStickMomentMass(getMass(), this.length, true);
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
	public void onMouseEvent(MouseEvent e)
	{
		// On left mouse click, adds moment
		// On right mouse click, changes the rotation axis
		
		// Adds moment to the stick, depending from the mouse position
		Vector3D relativePosition = getTransformation().inverseTransform(e.getPosition());
		
		if (relativePosition.getSecond() < - this.length / 2 || relativePosition.getSecond() > this.length / 2)
			return;
		
		Vector3D effectPosition = relativePosition.vectorProjection(new Vector3D(0, 1));
		
		if (e.getButton() == MouseButton.LEFT)
		{
			Vector3D force = new Vector3D(50, 0);
			if (relativePosition.getFirst() > 0)
				force = force.reverse();
			
			getRotator().applyMoment(force, relativePosition);
			this.lastEffectPosition = effectPosition;
		}
		else if (e.getButton() == MouseButton.RIGHT)
		{
			//if (getRotator().getRotationOrigin().equals(Vector3D.zeroVector()))
			getRotator().setRotationOrigin(effectPosition);
			//else
			//	getRotator().setRotationOrigin(Vector3D.zeroVector());
		}
		
		this.lastMousePosition = relativePosition;
	}

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		if (getTransformation() == null || this.lastMousePosition == null || 
				this.lastEffectPosition == null)
			return;
		
		g2d.setColor(Color.GREEN);
		AffineTransform lastTransform = g2d.getTransform();
		getTransformation().transform(g2d);
		
		g2d.drawLine(0, this.length / 2, 0, -this.length / 2);
		g2d.setColor(Color.RED);
		g2d.drawOval(this.lastMousePosition.getFirstInt() - 2, 
				this.lastMousePosition.getSecondInt() - 2, 4, 4);
		g2d.drawOval(this.lastEffectPosition.getFirstInt() - 2, 
				this.lastEffectPosition.getSecondInt() - 2, 4, 4);
		g2d.setColor(Color.BLUE);
		g2d.drawOval(getRotator().getRotationOrigin().getFirstInt() - 3, 
				getRotator().getRotationOrigin().getSecondInt() - 3, 6, 6);
		
		g2d.setTransform(lastTransform);
	}

	@Override
	public int getDepth()
	{
		return 0;
	}
}
