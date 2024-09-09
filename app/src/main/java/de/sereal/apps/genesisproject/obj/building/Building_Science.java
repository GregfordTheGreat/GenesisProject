package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;

public class Building_Science extends Building
{
	public Building_Science(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}


	@Override
	public void Build() 
	{
		BuildFromWavefrontObj(R.raw.science_obj);
	}

	@Override
	public void DrawEffects()
	{
	}
}
