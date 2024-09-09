package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.obj.particle.ParticleEffect;
import de.sereal.apps.genesisproject.obj.particle.Smoke;
import de.sereal.apps.genesisproject.rules.MaterialValue;

public class Building_Melter extends Building
{
	public Building_Melter(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}

  @Override
	public void Build() 
	{
		BuildFromFile(R.raw.building_melter, 0);

    ParticleEffect smoke = new Smoke();
    smoke.SetOrigin(4.5f * unitSize, 1.7f * unitSize, 0.5f * unitSize);
    particleEffects.add(smoke);
	}

  @Override
  public void DrawEffects()
  {
    for (ParticleEffect pEff : particleEffects)
    {
      pEff.Draw();
    }
  }
}
