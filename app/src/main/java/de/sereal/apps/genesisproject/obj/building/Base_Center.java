package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;

public class Base_Center extends Building
{
	public Base_Center(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}
	
	@Override
	public void Build() 
	{
		Log.d("Build","Base Center");
		BuildFromFile(R.raw.building_base_center, 0);
	}

	@Override
	public void DrawEffects()
	{
	}
}
