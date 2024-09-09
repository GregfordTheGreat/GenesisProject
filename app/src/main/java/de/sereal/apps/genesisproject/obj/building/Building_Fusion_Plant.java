package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;

import de.sereal.apps.genesisproject.R;

public class Building_Fusion_Plant extends Building
{
	public Building_Fusion_Plant(Context context, float unitSize, final String buildingDefinitionKey)
	{
		super(context, unitSize, buildingDefinitionKey);
	}
	
	@Override
	public void Build() 
	{
		BuildFromFile(R.raw.building_fusion_plant, 0);
	}

	@Override
	public void DrawEffects()
	{
	}
}
