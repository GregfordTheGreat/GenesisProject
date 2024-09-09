package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;

public class Building_Solar_Panels extends Building
{
	public Building_Solar_Panels(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}
	
	@Override
	public void Build() 
	{
		BuildFromFile(R.raw.building_solar_panels, 0);
	}

	@Override
	public void DrawEffects()
	{
	}

}
