package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;

public class Building_Transport_Station extends Building
{
	public Building_Transport_Station(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}
	
	@Override
	public void Build() 
	{
		Log.d("Build","Station");
		BuildFromFile(R.raw.building_station, R.raw.building_station_lights);
	}

	@Override
	public void DrawEffects()
	{
	}

}
