package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;

public class Building_Center extends Building
{
	public Building_Center(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}


	@Override
	public void Build() 
	{
		BuildFromWavefrontObj(R.raw.moo_obj);
        //BuildFromFile(R.raw.building_base_center, 0);
	}

	@Override
	public void DrawEffects()
	{
	}
}
