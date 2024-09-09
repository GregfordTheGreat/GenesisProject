package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;

public class Building_Mine_CoreIce extends Building
{
	public Building_Mine_CoreIce(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}


	@Override
	public void Build() 
	{
		Log.d("Build","Mine CoreIce");
		BuildFromFile(R.raw.building_mine_coreice, 0);
	}

	@Override
	public void DrawEffects()
	{
	}
}
