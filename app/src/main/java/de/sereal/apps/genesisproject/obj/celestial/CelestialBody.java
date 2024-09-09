package de.sereal.apps.genesisproject.obj.celestial;

import java.util.Vector;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.obj.Orbit;
import de.sereal.apps.genesisproject.obj.Sphere;
import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.Vector3D;

/**
 * Created by sereal on 03.03.2017.
 */
public class CelestialBody
{
  public Sphere body;
  public Sphere cloudCover;
  public Orbit orbit;
  public Vector<Vector3D> BoundingBox = new Vector<>();

  public float OrbitalRadius = 0.0f;
  public float BodyRadius = 0.0f;
  public float RotationY = 0.0f;
  public float RotationZ = 0.0f;
  public Vector3D center = new Vector3D();

  public CelestialBody(float orbitalRadius, float orbitRotation, float bodyRadius)
  {
    OrbitalRadius = orbitalRadius;
    RotationY = orbitRotation;
    BodyRadius = bodyRadius;

    int bodyRes = 10;
    if(bodyRadius >= 1.0f && bodyRadius < 2.0f) bodyRes = 20; else
    if(bodyRadius >= 2.0f && bodyRadius < 3.0f) bodyRes = 25; else
    if(bodyRadius >= 3.0f) bodyRes = 30;

    body = new Sphere(bodyRadius, bodyRes, bodyRes);

    center.z = orbitalRadius * ((float)Math.cos((RotationY / 360f) * MyConstants.TwoPi) * (float)Math.cos((RotationZ / 360f) * MyConstants.TwoPi));
    center.x = orbitalRadius * ((float)Math.sin((RotationY / 360f) * MyConstants.TwoPi) * (float)Math.cos((RotationZ / 360f) * MyConstants.TwoPi));
    center.y = orbitalRadius * ((float)Math.sin((RotationZ / 180f) * (float)Math.PI));

    if(orbitalRadius > 0.0f) orbit = new Orbit(orbitalRadius, bodyRes * 3);
    CreateBoundingBox(bodyRadius);
  }

  public void AddCloudCover()
  {
    int bodyRes = 10;
    if(BodyRadius >= 1.0f && BodyRadius < 2.0f) bodyRes = 20; else
    if(BodyRadius >= 2.0f && BodyRadius < 3.0f) bodyRes = 25; else
    if(BodyRadius >= 3.0f) bodyRes = 30;

    cloudCover = new Sphere(BodyRadius + 0.05f, bodyRes, bodyRes);
    int CloudCoverTextureID = cloudCover.GenerateTexture(1.0f);
    body.SetShadowTexture(CloudCoverTextureID);
    cloudCover.Create(false);
  }

  private void CreateBoundingBox(float radius)
  {
    BoundingBox.addElement(new Vector3D(-radius, radius, -radius).Add(center)); // top points
    BoundingBox.addElement(new Vector3D( radius, radius, -radius).Add(center));
    BoundingBox.addElement(new Vector3D( radius, radius, radius).Add(center));
    BoundingBox.addElement(new Vector3D(-radius, radius, radius).Add(center));

    BoundingBox.addElement(new Vector3D(-radius, -radius, -radius).Add(center)); // bottom points
    BoundingBox.addElement(new Vector3D( radius, -radius, -radius).Add(center));
    BoundingBox.addElement(new Vector3D( radius, -radius, radius).Add(center));
    BoundingBox.addElement(new Vector3D(-radius, -radius, radius).Add(center));
  }
}
