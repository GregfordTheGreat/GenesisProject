package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.rules.MaterialValue;

public class Building_Mine_RareEarth extends Building
{
	public Building_Mine_RareEarth(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
    
	}

	@Override
	public void Build() 
	{
		Log.d("Build","Mine RE");
		BuildFromFile(R.raw.building_mine_re, 0);
	}

	@Override
	public void DrawEffects()
	{
	}
}
