package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.rules.MaterialValue;

public class Building_Prod_Oxygen extends Building
{
	public Building_Prod_Oxygen(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}

	@Override
	public void Build() 
	{
		Log.d("Build","Mine HE3");
		BuildFromFile(R.raw.building_prod_oxygen, 0);
	}

	@Override
	public void DrawEffects()
	{
	}
}
