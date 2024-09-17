package de.sereal.apps.genesisproject.rules;

import java.util.Vector;

import de.sereal.apps.genesisproject.util.MaterialConversion;

public class BuildingDef
{
	private String Key;
	private String Icon;
	private String Mesh;
	private String Name;
	private Vector<MaterialValue> ConstructionsCosts = new Vector<>();
	private Vector<MaterialValue> ProductionValues = new Vector<>();
	private Vector<MaterialValue> resourceCosts = new Vector<>();
  private Vector<MaterialValue> resourceProductions = new Vector<>();
  private Vector<MaterialValue> storageCapacities = new Vector<>();
	private MaterialConversion ManufacturingTable = new MaterialConversion();

	private int[][] grid;

	public BuildingDef(String key, String icon, String mesh, String name, int[][] grid)
	{
		this.Key = key;
		this.Icon = icon;
		this.Mesh = mesh;
		this.Name = name;
		this.grid = grid;
	}
	
	public String getKey() {
		return Key;
	}

	public String getIcon() {
		return Icon;
	}

	public String getMesh() {
		return Mesh;
	}

	public String getName() {
		return Name;
	}

	public int[][] getGrid() {
		return grid;
	}

	public void SetConstructionCosts(Vector<MaterialValue> constructionsCosts)
	{
		ConstructionsCosts = constructionsCosts;
	}

	public Vector<MaterialValue> GetConstructionCosts(){ return ConstructionsCosts; }

	public void SetProductionValues(Vector<MaterialValue> productionValues)
	{
		ProductionValues = productionValues;
	}
	public Vector<MaterialValue> GetProductionValues(){ return ProductionValues; }

	public void setResourceCosts(Vector<MaterialValue> resourceValues) {
		resourceCosts = resourceValues;
	}
	public Vector<MaterialValue> getResourceCosts() { return resourceCosts; }

  public void setResourceProductions(Vector<MaterialValue> resourceValues) {
      resourceProductions = resourceValues;
  }
  public Vector<MaterialValue> getResourceProductions(){ return resourceProductions; }
  
	public void SetManufacturingTable(MaterialConversion manufacturingTable)
	{
		ManufacturingTable = manufacturingTable;
	}
	public MaterialConversion GetManufacturingTable(){ return ManufacturingTable; }
  
  public void setStorageCapacities(final Vector<MaterialValue> capacities) { this.storageCapacities = capacities; }
  public Vector<MaterialValue> getStorageCapacities() { return this.storageCapacities; }
}
