package motion_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import genesis_event.Actor;
import genesis_event.Drawable;
import genesis_event.HandlerRelay;
import genesis_util.StateOperator;
import genesis_util.Vector3D;
import motion_movement.Movable;
import motion_movement.ObjectMover;
import omega_util.SimpleGameObject;
import omega_util.Transformation;

/**
 * This class is used for testing momentums. It's a bit grude for the lack of conflict module.
 * 
 * @author Mikko Hilpinen
 * @since 19.3.2015
 */
public class TestDisk extends SimpleGameObject implements Movable, Drawable, Actor
{
	// ATTRIBUTES	-----------------------
	
	private Color color;
	private Transformation t;
	private int radius;
	private ObjectMover mover;
	private Vector3D windowSize;
	
	
	// CONSTRUCTOR	-----------------------
	
	/**
	 * Creates a new disk
	 * @param handlers The handlers that will handle the disk
	 * @param color The color of the disk
	 * @param position The position of the disk's origin
	 * @param radius The radius of the disk
	 * @param windowSize The size of the program window
	 */
	public TestDisk(HandlerRelay handlers, Color color, Vector3D position, int radius, 
			Vector3D windowSize)
	{
		super(handlers);
		
		this.color = color;
		this.t = new Transformation(position);
		this.radius = radius;
		this.mover = new ObjectMover(this, handlers);
		this.windowSize = windowSize;
		
		// TODO: Add friction
	}
	
	
	// IMPLEMENTED METHODS	---------------

	@Override
	public Transformation getTransformation()
	{
		return this.t;
	}

	@Override
	public void setTrasformation(Transformation t)
	{
		this.t = t;
	}

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		g2d.setColor(this.color);
		AffineTransform lastTransform = g2d.getTransform();
		getTransformation().transform(g2d);
		g2d.fillOval(-getRadius(), -getRadius(), getRadius() * 2, getRadius() * 2);
		g2d.setColor(Color.BLACK);
		g2d.drawOval(-getRadius(), -getRadius(), getRadius() * 2, getRadius() * 2);
		g2d.setTransform(lastTransform);
	}

	@Override
	public int getDepth()
	{
		return 0;
	}

	@Override
	public StateOperator getIsVisibleStateOperator()
	{
		return getIsActiveStateOperator();
	}

	@Override
	public double getMass()
	{
		// A = pi * r^2 (depth = 1, density = 1)
		return Math.PI * Math.pow(getRadius(), 2);
	}

	@Override
	public ObjectMover getMover()
	{
		return this.mover;
	}
	
	@Override
	public void act(double duration)
	{
		if (getTransformation() == null || this.windowSize == null)
			return;
		
		// The disks cannot go outside the window boundaries
		if (getPosition().getFirst() < 0)
			getMover().setDirectionalVelocity(new Vector3D(Math.abs(getMover().getVelocity().getFirst()), 0));
		if (getPosition().getSecond() < 0)
			getMover().setDirectionalVelocity(new Vector3D(0, Math.abs(getMover().getVelocity().getSecond())));
		if (getPosition().getFirst() > this.windowSize.getFirst())
			getMover().setDirectionalVelocity(new Vector3D(-Math.abs(getMover().getVelocity().getFirst()), 0));
		if (getPosition().getSecond() > this.windowSize.getSecond())
			getMover().setDirectionalVelocity(new Vector3D(0, -Math.abs(getMover().getVelocity().getSecond())));
		
		// Also calculates friction
		getMover().applyFriction(0.3, duration, 0.098);
	}

	
	// GETTERS & SETTERS	----------------------
	
	/**
	 * @return The radius of the disk in pixels
	 */
	public int getRadius()
	{
		return this.radius;
	}
	
	/**
	 * @return The position of the disk's origin
	 */
	public Vector3D getPosition()
	{
		return getTransformation().getPosition();
	}
}
