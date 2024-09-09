package de.sereal.apps.genesisproject.event;

public interface TouchEvent 
{
	public abstract void OnRotation(float angle);
	public abstract void OnScale(float scale);
	public abstract void OnDrag(int newScreenX, int newScreenY);
	public abstract void OnTilt(float distance);
	public abstract void OnMove(float distanceX, float distanceY);
}
