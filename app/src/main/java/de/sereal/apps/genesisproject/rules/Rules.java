package de.sereal.apps.genesisproject.rules;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.util.MaterialConversion;
import de.sereal.apps.genesisproject.util.ShortArrayList;

public class Rules {
    public enum ItemType {
        UNKNOWN,
        BUILDING,
        VEHICLE,
        MATERIAL,
        RESOURCE
    }

    private HashMap<String, BuildingDef> buildingDefinitions = new HashMap<>();
    private HashMap<String, VehicleDef> vehicleDefinitions = new HashMap<>();
    private HashMap<String, MaterialDef> MaterialDefinitions = new HashMap<>();
    private HashMap<String, ResourceDef> ResourceDefinitions = new HashMap<>();

    public boolean LoadRules(Context context) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.rules)));
            String line;
            ItemType type = ItemType.UNKNOWN;
            String[] tokens;
            Vector<ShortArrayList> grid = new Vector<ShortArrayList>();

            String key = "", icon = "", mesh = "", name = "";
            String costType = "";
            Vector<MaterialValue> constructionCosts = new Vector<>();
            Vector<MaterialValue> productionValues = new Vector<>();
            Vector<MaterialValue> resourceCosts = new Vector<>();
            Vector<MaterialValue> resourceProductions = new Vector<>();
            Vector<MaterialValue> storageCapacities = new Vector<>();
            MaterialConversion manufacturingTable = new MaterialConversion();


            while (true) {
                line = reader.readLine();
                if (line == null || line.trim().isEmpty()) {
                    Log.d("Add", "" + line);
                    switch (type) {
                        case MATERIAL:
                            MaterialDefinitions.put(key, new MaterialDef(key, name, icon));
                            break;

                        case RESOURCE:
                            ResourceDefinitions.put(key, new ResourceDef(key, name, icon));
                            break;

                        case BUILDING:
                            int h = grid.size();
                            int w = grid.get(0).GetSize();
                            int[][] selGrid = new int[h][w];

                            for (int a = 0; a < h; a++) {
                                for (int b = 0; b < w; b++) {
                                    selGrid[a][b] = grid.get(a).Get(b);
                                }
                            }

                            BuildingDef def = new BuildingDef(key, icon, mesh, name, selGrid);
                            def.SetConstructionCosts(constructionCosts);
                            def.SetProductionValues(productionValues);
                            def.setResourceCosts(resourceCosts);
                            def.setResourceProductions(resourceProductions);
                            def.SetManufacturingTable(manufacturingTable);
                            def.setStorageCapacities(storageCapacities);
                            buildingDefinitions.put(key, def);

                            grid = new Vector<>();
                            constructionCosts = new Vector<>();
                            productionValues = new Vector<>();
                            resourceCosts = new Vector<>();
                            storageCapacities = new Vector<>();
                            manufacturingTable = new MaterialConversion();
                            break;

                        case VEHICLE:
                            vehicleDefinitions.put(key, new VehicleDef(key, mesh, name));
                            break;

                        default:
                            break;
                    }
                    type = ItemType.UNKNOWN;

                    if (line == null)
                        break;
                    else
                        continue;
                }

                if (line.startsWith("[")) {
                    key = line.substring(1, line.indexOf("]"));
                } else if (line.contains("=")) {
                    tokens = line.split("=");
                    if (tokens[0].equalsIgnoreCase("Type")) {
                        type = GetItemTypeByStr(tokens[1]);
                    } else if (tokens[0].equalsIgnoreCase("Icon")) {
                        icon = tokens[1];
                    } else if (tokens[0].equalsIgnoreCase("Name")) {
                        name = tokens[1];
                    } else if (tokens[0].equalsIgnoreCase("Mesh")) {
                        mesh = tokens[1];
                    } else if (tokens[0].startsWith("Grid")) {
                        ShortArrayList sal = new ShortArrayList();
                        String[] gridLine = tokens[1].split(" ", -1);
                        for (String s : gridLine) {
//							Log.d("Adding",s+"");
                            sal.Add(Integer.parseInt(s));
                        }
                        grid.addElement(sal);
//						Log.d("AddingArray",sal.GetSize()+"");
                    } else if (tokens[0].startsWith("CostType")) {
                        costType = tokens[1].substring(tokens[1].indexOf("[") + 1, tokens[1].indexOf("]"));
                    } else if (tokens[0].startsWith("CostValue")) {
                        if (MaterialDefinitions.containsKey(costType)) {
                            constructionCosts.addElement(new MaterialValue(costType, Long.parseLong(tokens[1])));
                        }
                    } else if (tokens[0].startsWith("ProdType")) {
                        costType = tokens[1].substring(tokens[1].indexOf("[") + 1, tokens[1].indexOf("]"));
                    } else if (tokens[0].startsWith("ProdValue")) {
                        if (MaterialDefinitions.containsKey(costType)) {
                            productionValues.addElement(new MaterialValue(costType, Long.parseLong(tokens[1])));
                        }
                    } else if (tokens[0].startsWith("ResourceCost")) {
                        final String item = tokens[1].substring(tokens[1].indexOf("[") + 1, tokens[1].indexOf(","));
                        final long value = Long.parseLong(tokens[1].substring(tokens[1].indexOf(",") + 1, tokens[1].indexOf("]")));
                        resourceCosts.add(new MaterialValue(item, value));
                    } else if (tokens[0].startsWith("ResourceProductions")) {
                        final String item = tokens[1].substring(tokens[1].indexOf("[") + 1, tokens[1].indexOf(","));
                        final long value = Long.parseLong(tokens[1].substring(tokens[1].indexOf(",") + 1, tokens[1].indexOf("]")));
                        resourceProductions.add(new MaterialValue(item, value));
                    } else if (tokens[0].startsWith("ReqMatType")) {
                        costType = tokens[1].substring(tokens[1].indexOf("[") + 1, tokens[1].indexOf("]"));
                    } else if (tokens[0].startsWith("ReqMatValue")) {
                        if (MaterialDefinitions.containsKey(costType)) {
                            manufacturingTable.InputMaterials.addElement(new MaterialValue(costType, Long.parseLong(tokens[1])));
                        }
                    } else if (tokens[0].startsWith("StorageCapacity")) {
                        final String item = tokens[1].substring(tokens[1].indexOf("[") + 1, tokens[1].indexOf(","));
                        final long value = Long.parseLong(tokens[1].substring(tokens[1].indexOf(",") + 1, tokens[1].indexOf("]")));
                        storageCapacities.addElement(new MaterialValue(item, value));
                    } else if (tokens[0].startsWith("ManufactureType")) {
                        costType = tokens[1].substring(tokens[1].indexOf("[") + 1, tokens[1].indexOf("]"));
                    } else if (tokens[0].startsWith("ManufactureValue")) {
                        if (MaterialDefinitions.containsKey(costType)) {
                            manufacturingTable.Output = new MaterialValue(costType, Long.parseLong(tokens[1]));
                        }
                    }

                }
            }
        } catch (Exception e) {
            Log.e("parsing rules failed", ":(", e);
        }
        return false;
    }

    public HashMap<String, BuildingDef> GetBuildingDefinitions() {
        return buildingDefinitions;
    }

    public BuildingDef getBuildingDefinition(final String key) {
        return buildingDefinitions.get(key);
    }

    public boolean isTransportCenter(final String key) {
        return buildingDefinitions.containsKey(key) && buildingDefinitions.get(key).getKey().equalsIgnoreCase("STATION");
    }

    public VehicleDef getVehicleDefinition(final String key) {
        return vehicleDefinitions.get(key);
    }

    public MaterialDef GetMaterialDefinition(String key) {
        return MaterialDefinitions.get(key);
    }

    public ResourceDef GetResourceDefinition(String key) {
        return ResourceDefinitions.get(key);
    }

    private ItemType GetItemTypeByStr(String type) {
        if (type.equalsIgnoreCase("building")) {
            return ItemType.BUILDING;
        } else if (type.equalsIgnoreCase("vehicle")) {
            return ItemType.VEHICLE;
        } else if (type.equalsIgnoreCase("material")) {
            return ItemType.MATERIAL;
        } else if (type.equalsIgnoreCase("resource")) {
            return ItemType.RESOURCE;
        }
        return ItemType.UNKNOWN;
    }


}
