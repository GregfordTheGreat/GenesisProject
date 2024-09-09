package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;

public class Building_Mine_He3 extends Building
{
	public Building_Mine_He3(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}


	@Override
	public void Build() 
	{
		Log.d("Build","Mine HE3");
		BuildFromFile(R.raw.building_he3, 0);
		//BuildFromWavefrontObj(R.raw.cat_obj);
	}

	@Override
	public void DrawEffects()
	{
	}
}
