package de.sereal.apps.genesisproject.obj;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import de.sereal.apps.genesisproject.GameActivity;
import de.sereal.apps.genesisproject.GameLogic;
import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.PlanetSurfaceRenderer;
import de.sereal.apps.genesisproject.obj.building.Base_Center;
import de.sereal.apps.genesisproject.obj.building.Building;
import de.sereal.apps.genesisproject.obj.building.BuildingDescriptor;
import de.sereal.apps.genesisproject.obj.building.Building_Melter;
import de.sereal.apps.genesisproject.obj.building.Building_Mine_CoreIce;
import de.sereal.apps.genesisproject.obj.building.Building_Mine_He3;
import de.sereal.apps.genesisproject.obj.building.Building_Mine_RareEarth;
import de.sereal.apps.genesisproject.obj.building.Building_Fusion_Plant;
import de.sereal.apps.genesisproject.obj.building.Building_Prod_Oxygen;
import de.sereal.apps.genesisproject.obj.building.Building_Science;
import de.sereal.apps.genesisproject.obj.building.Building_Solar_Panels;
import de.sereal.apps.genesisproject.obj.building.Building_Transport_Station;
import de.sereal.apps.genesisproject.obj.planet.PlanetDescriptor;
import de.sereal.apps.genesisproject.obj.vehicles.Vehicle;
import de.sereal.apps.genesisproject.obj.vehicles.VehicleDescriptor;
import de.sereal.apps.genesisproject.obj.vehicles.VehicleTransport;
import de.sereal.apps.genesisproject.util.Color4f;
import de.sereal.apps.genesisproject.util.Helpers;
import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.MyConstants.Direction;
import de.sereal.apps.genesisproject.util.PathNode;
import de.sereal.apps.genesisproject.util.Position2D;
import de.sereal.apps.genesisproject.util.Ray;
import de.sereal.apps.genesisproject.util.RoadMap;
import de.sereal.apps.genesisproject.util.ShortArrayList;
import de.sereal.apps.genesisproject.util.TerrainGen;
import de.sereal.apps.genesisproject.util.TextureHandler;
import de.sereal.apps.genesisproject.util.Vector3D;
import de.sereal.apps.genesisproject.obj.building.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Created by sereal on 01.08.2016.
 */
public class PlanetSurface {
    private Context context;
    private PlanetSurfaceRenderer planetSurfaceRenderer;

    // set by planetdescriptor
    private int[][] HeightMap;
    private int worldWidthSegments;
    private int worldHeightSegments;
    private PlanetDescriptor planetDescriptor;

    private final int PickUpdateRange = 5;
    private float segmentSize = MyConstants.TILE_SIZE;
    private float segmentHeightStep = MyConstants.HEIGHT_STEP;

    private final static int CHUNK_SEGMENTS_WIDTH = 100;
    private final static int CHUNK_SEGMENTS_HEIGHT = 100;
    private final static Color4f normalColor = new Color4f(0.5f, 0.85f, 0.5f);
    private final static Color4f lightColor = new Color4f(normalColor.r + 0.05f, normalColor.g + 0.05f, normalColor.b + 0.05f);
    private final static Color4f darkColor = new Color4f(normalColor.r - 0.05f, normalColor.g - 0.05f, normalColor.b - 0.05f);
    private final static Color4f seaColor = new Color4f(0.5f, 0.5f, 0.8f);
    
    private List<Chunk> chunks = new ArrayList<>();
    private Map<Chunk, FloatBuffer> verticesByChunk = new HashMap<>();
    private Map<Chunk, FloatBuffer> normalsByChunk = new HashMap<>();
    private Map<Chunk, FloatBuffer> colorsByChunk = new HashMap<>();
    private Map<Chunk, FloatBuffer> texCoordsByChunk = new HashMap<>();
    private Map<Chunk, FloatBuffer> texCoordMapByChunk = new HashMap<>();
    private Map<Chunk, FloatBuffer> shinyByChunk = new HashMap<>();
    private Map<Chunk, List<Tile>> squaresOfWorld = new HashMap<>();
    private Map<Chunk, SparseArray<Indices>> vboIndicesByChunkByTextureId = new HashMap<>();
    private Map<Chunk, ShortBuffer> indicesWireByChunk = new HashMap<>();
    private Map<Chunk, ShortBuffer> indicesConnectionPointsByChunk = new HashMap<>();
    private Map<Chunk, ShortBuffer> indicesPositiveSelectionByChunk = new HashMap<>();
    private Map<Chunk, ShortBuffer> indicesNegativeSelectionByChunk = new HashMap<>();
    private Map<Chunk, Integer> numberOfIndicesByChunk = new HashMap<>();
    private Map<Chunk, Integer> numberOfIndicesPositiveByChunk = new HashMap<>();
    private Map<Chunk, Integer> numberOfIndicesNegativeByChunk = new HashMap<>();
    private Map<Chunk, Integer> numberOfIndicesConnectionByChunk = new HashMap<>();
    private Vector<Integer> keys = new Vector<>();

    private float[] colorWire = {0.0f, 0.0f, 0.0f, 0.2f};
    private float[] colorWireDark = {0.0f, 0.0f, 0.0f, 0.5f};
    private float[] colorPositiveSelection = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] colorNegativeSelection = {1.0f, 0.0f, 0.0f, 1.0f};
    private float[] colorConnectionPoints = {0.0f, 0.0f, 1.0f, 1.0f};

    public boolean showConnectionPoints = false;
    private int pickType = 1;
    // Log
    // picking
    private int FirstPickResultW = 0;
    private int FirstPickResultH = 0;
    private int LastPickResultW = 0;
    private int LastPickResultH = 0;

    private CopyOnWriteArrayList<Building> Buildings = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Vehicle> Vehicles = new CopyOnWriteArrayList<>();
    private Direction BuildDirection = Direction.SOUTH;
    private Vector3D SelectedGridStartPoint = new Vector3D();

    private int TextureHandle_RareEarth = -1;
    private int TextureHandle_CoreIce = -1;
    private MyConstants.ResourceMapStyle CurrentResourceMapStyle = MyConstants.ResourceMapStyle.NORMAL;

    public PlanetSurface(Context context, PlanetSurfaceRenderer renderer) {
        this.context = context;
        this.planetSurfaceRenderer = renderer;
        GameActivity.MyGameLogic.SetPlanetSurface(this);
        
        prepareTexturesSet();
    }

    public void setPlanetDescription(final PlanetDescriptor planetDescriptor) {
        this.planetDescriptor = planetDescriptor;
        this.HeightMap = planetDescriptor.getHeightmap();
        this.worldWidthSegments = planetDescriptor.getWidthSegments();
        this.worldHeightSegments = planetDescriptor.getHeightSegments();

        prepareChunks();
        prepareBuffers();

        Log.d("planetSurface", "getRoadmap" + planetDescriptor.getRoadMap());
        if (planetDescriptor.getRoadMap() == null) {
            planetDescriptor.setRoadMap(new RoadMap(worldWidthSegments, worldHeightSegments));
        }

        for (final BuildingDescriptor bd : planetDescriptor.getBuildings()) {
            AddBuilding(bd);

            final VehicleDescriptor transportVehicle = bd.getTransportVehicle();
            if (transportVehicle != null) {
                transportVehicle.vehicle = new VehicleTransport(context, segmentSize);
                transportVehicle.vehicle.setPosition(transportVehicle.position.x, transportVehicle.position.y, transportVehicle.position.z);
                Vehicles.add(transportVehicle.vehicle);
            }

            final VehicleDescriptor deliveryVehicle = bd.getDeliveryVehicle();
            if (deliveryVehicle != null) {
                deliveryVehicle.vehicle = new VehicleTransport(context, segmentSize);
                deliveryVehicle.vehicle.setPosition(deliveryVehicle.position.x, deliveryVehicle.position.y, deliveryVehicle.position.z);
                Vehicles.add(deliveryVehicle.vehicle);
            }
        }

        BuildSurfaceFromHeightMap();
        UpdatePathfinder();
        //RebuildTextureMap();
    }

    private void prepareTexturesSet() {
        keys.add(0);
        keys.add(R.raw.street1n);
        keys.add(R.raw.street1e);
        keys.add(R.raw.street1s);
        keys.add(R.raw.street1w);
        keys.add(R.raw.street2ns);
        keys.add(R.raw.street2ew);
        keys.add(R.raw.streetln);
        keys.add(R.raw.streetle);
        keys.add(R.raw.streetls);
        keys.add(R.raw.streetlw);
        keys.add(R.raw.street3n);
        keys.add(R.raw.street3e);
        keys.add(R.raw.street3s);
        keys.add(R.raw.street3w);
        keys.add(R.raw.street4);
    }
    
    private void prepareChunks() {
        int x = 0;
        for (int xi=0; xi < worldWidthSegments; xi += CHUNK_SEGMENTS_WIDTH) {
            int y = 0;
            for (int yi=0; yi < worldHeightSegments; yi += CHUNK_SEGMENTS_HEIGHT) {
                final int w = Math.min(worldWidthSegments - xi, CHUNK_SEGMENTS_WIDTH);
                final int h = Math.min(worldHeightSegments - yi, CHUNK_SEGMENTS_HEIGHT);
                final Chunk chunk = new Chunk(x, y, w, h);
                chunks.add(chunk);
                y++;
            }
            x++;
        }
    }

    private void prepareBuffers() {
        
        for(final Chunk chunk : chunks) {
            final int chunkVertexCount = chunk.getWidth() * chunk.getHeight() * MyConstants.NumVerticesPerQuad;
            final FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(chunkVertexCount * MyConstants.NumFloatPerVertex * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
            verticesByChunk.put(chunk, vertexBuffer);
            
            final FloatBuffer normalBuffer = ByteBuffer.allocateDirect(chunkVertexCount * MyConstants.NumFloatPerNormal * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
            normalsByChunk.put(chunk, normalBuffer);
            
            final FloatBuffer colorBuffer = ByteBuffer.allocateDirect(chunkVertexCount * MyConstants.NumFloatPerColor * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
            colorsByChunk.put(chunk, colorBuffer);
            
            final FloatBuffer texCoordBuffer = ByteBuffer.allocateDirect(chunkVertexCount * MyConstants.NumFloatPerTexCoord * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
            texCoordsByChunk.put(chunk, texCoordBuffer);
            
            final FloatBuffer texCoordBufferMap = ByteBuffer.allocateDirect(chunkVertexCount * MyConstants.NumFloatPerTexCoord * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
            texCoordMapByChunk.put(chunk, texCoordBufferMap);
            
            final FloatBuffer shinyBuffer = ByteBuffer.allocateDirect(chunkVertexCount * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
            shinyByChunk.put(chunk, shinyBuffer);
            
            final ShortBuffer indexBufferWire = ByteBuffer.allocateDirect(chunk.getWidth() * chunk.getHeight() * 8 * MyConstants.NumBytesPerShort).order(ByteOrder.nativeOrder()).asShortBuffer();
            indicesWireByChunk.put(chunk, indexBufferWire);
            
            // max 100 quads 10 x 10
            final ShortBuffer indexBufferPositiveSelection = ByteBuffer.allocateDirect(100 * 8 * MyConstants.NumBytesPerShort).order(ByteOrder.nativeOrder()).asShortBuffer();
            indexBufferPositiveSelection.position(0);
            indicesPositiveSelectionByChunk.put(chunk, indexBufferPositiveSelection);
            
            final ShortBuffer indexBufferNegativeSelection = ByteBuffer.allocateDirect(100 * 8 * MyConstants.NumBytesPerShort).order(ByteOrder.nativeOrder()).asShortBuffer();
            indexBufferNegativeSelection.position(0);
            indicesNegativeSelectionByChunk.put(chunk, indexBufferNegativeSelection);
            
            // TODO: limited to 100 Connectionspoints!!
            final ShortBuffer indexBufferConnectionPoints = ByteBuffer.allocateDirect(100 * 8 * MyConstants.NumBytesPerShort).order(ByteOrder.nativeOrder()).asShortBuffer();
            indexBufferConnectionPoints.position(0);
            indicesConnectionPointsByChunk.put(chunk, indexBufferConnectionPoints);
            
            numberOfIndicesByChunk.put(chunk, 0);
            numberOfIndicesPositiveByChunk.put(chunk, 0);
            numberOfIndicesNegativeByChunk.put(chunk, 0);
            numberOfIndicesConnectionByChunk.put(chunk, 0);
            vboIndicesByChunkByTextureId.put(chunk, new SparseArray<Indices>());
        }
    }

    private void BuildSurfaceFromHeightMap() {
        final float halfWorldHeight = segmentSize * (float) (worldHeightSegments) / 2.0f;
        final float halfWidth = segmentSize * (float) (worldWidthSegments) / 2.0f;
        
        float x, z;
        int[] hm = new int[4];
        
        for (final Chunk chunk : chunks) {
            final List<Tile> squareList = new ArrayList<>();
            squaresOfWorld.put(chunk, squareList);

            for (int h = 0; h < chunk.getHeight(); h++) {
                for (int w = 0; w < chunk.getWidth(); w++) {
                    int xi = chunk.getX() * CHUNK_SEGMENTS_WIDTH + w;
                    int yi = chunk.getY() * CHUNK_SEGMENTS_HEIGHT + h;
                    
                    hm[0] = HeightMap[yi][xi];
                    hm[1] = HeightMap[yi + 1][xi];
                    hm[2] = HeightMap[yi + 1][xi + 1];
                    hm[3] = HeightMap[yi][xi + 1];

                    x = (float) xi * segmentSize - halfWidth;
                    z = (float) yi * segmentSize - halfWorldHeight;

                    final Tile square = new Tile(x, z, segmentSize, segmentSize, segmentHeightStep, hm);
                    square.SetColor(normalColor, lightColor, darkColor, seaColor, hm);
                    squareList.add(square);
                }
            }

            float[] vertices = new float[squareList.size() * 6 * MyConstants.NumFloatPerVertex];
            float[] colors = new float[squareList.size() * 6 * MyConstants.NumFloatPerColor];
            float[] shiny = new float[squareList.size() * 6];
            float[] normals = new float[squareList.size() * 6 * MyConstants.NumFloatPerVertex];
            short[] indicesWire = new short[squareList.size() * 8];
            float[] texCoords = new float[squareList.size() * 6 * MyConstants.NumFloatPerTexCoord];
            float[] texCoordsMap = new float[squareList.size() * 6 * MyConstants.NumFloatPerTexCoord];

            Vector3D normalA, normalB;
            int index = 0;
            int w, h;

            for (int sq = 0; sq < squareList.size(); sq++) {
                final Tile square = squareList.get(sq);
                w = (chunk.getX() * CHUNK_SEGMENTS_WIDTH) + (sq % chunk.getWidth());
                h = (chunk.getY() * CHUNK_SEGMENTS_HEIGHT) + (sq / chunk.getHeight());

                vertices[sq * 18 + 0] = square.P0.x;
                vertices[sq * 18 + 1] = square.P0.y;
                vertices[sq * 18 + 2] = square.P0.z;

                vertices[sq * 18 + 3] = square.P1.x;
                vertices[sq * 18 + 4] = square.P1.y;
                vertices[sq * 18 + 5] = square.P1.z;

                vertices[sq * 18 + 6] = square.P2.x;
                vertices[sq * 18 + 7] = square.P2.y;
                vertices[sq * 18 + 8] = square.P2.z;

                vertices[sq * 18 + 9] = square.P3.x;
                vertices[sq * 18 + 10] = square.P3.y;
                vertices[sq * 18 + 11] = square.P3.z;

                shiny[sq * 6 + 0] = square.P0.y > 0.0f ? 0.0f : 100.0f;
                shiny[sq * 6 + 1] = square.P1.y > 0.0f ? 0.0f : 100.0f;
                shiny[sq * 6 + 2] = square.P2.y > 0.0f ? 0.0f : 100.0f;
                shiny[sq * 6 + 3] = square.P3.y > 0.0f ? 0.0f : 100.0f;

                colors[sq * 24 + 0] = square.C0.r;
                colors[sq * 24 + 1] = square.C0.g;
                colors[sq * 24 + 2] = square.C0.b;
                colors[sq * 24 + 3] = square.C0.a;

                colors[sq * 24 + 4] = square.C1.r;
                colors[sq * 24 + 5] = square.C1.g;
                colors[sq * 24 + 6] = square.C1.b;
                colors[sq * 24 + 7] = square.C1.a;

                colors[sq * 24 + 8] = square.C2.r;
                colors[sq * 24 + 9] = square.C2.g;
                colors[sq * 24 + 10] = square.C2.b;
                colors[sq * 24 + 11] = square.C2.a;

                colors[sq * 24 + 12] = square.C3.r;
                colors[sq * 24 + 13] = square.C3.g;
                colors[sq * 24 + 14] = square.C3.b;
                colors[sq * 24 + 15] = square.C3.a;

                texCoords[sq * 12] = 0.0f;
                texCoords[sq * 12 + 1] = 0.0f;
                texCoords[sq * 12 + 2] = 0.0f;
                texCoords[sq * 12 + 3] = 1.0f;
                texCoords[sq * 12 + 4] = 1.0f;
                texCoords[sq * 12 + 5] = 1.0f;
                texCoords[sq * 12 + 6] = 1.0f;
                texCoords[sq * 12 + 7] = 0.0f;

                texCoordsMap[sq * 12] = ((float) w / (float) worldWidthSegments);
                texCoordsMap[sq * 12 + 1] = ((float) h / (float) worldHeightSegments);
                texCoordsMap[sq * 12 + 2] = ((float) w / (float) worldWidthSegments);
                texCoordsMap[sq * 12 + 3] = ((float) (h + 1) / (float) worldHeightSegments);
                texCoordsMap[sq * 12 + 4] = ((float) (w + 1) / (float) worldWidthSegments);
                texCoordsMap[sq * 12 + 5] = ((float) (h + 1) / (float) worldHeightSegments);
                texCoordsMap[sq * 12 + 6] = ((float) (w + 1) / (float) worldWidthSegments);
                texCoordsMap[sq * 12 + 7] = ((float) h / (float) worldHeightSegments);

                colors[sq * 24 + 16] = square.C4.r;
                colors[sq * 24 + 17] = square.C4.g;
                colors[sq * 24 + 18] = square.C4.b;
                colors[sq * 24 + 19] = square.C4.a;

                colors[sq * 24 + 20] = square.C5.r;
                colors[sq * 24 + 21] = square.C5.g;
                colors[sq * 24 + 22] = square.C5.b;
                colors[sq * 24 + 23] = square.C5.a;

                if (!square.mirrored) {
                    shiny[sq * 6 + 4] = square.P0.y > 0.0f ? 0.0f : 100.0f;
                    shiny[sq * 6 + 5] = square.P2.y > 0.0f ? 0.0f : 100.0f;

                    vertices[sq * 18 + 12] = square.P0.x;
                    vertices[sq * 18 + 13] = square.P0.y;
                    vertices[sq * 18 + 14] = square.P0.z;

                    vertices[sq * 18 + 15] = square.P2.x;
                    vertices[sq * 18 + 16] = square.P2.y;
                    vertices[sq * 18 + 17] = square.P2.z;

                    texCoords[sq * 12 + 8] = 0.0f;
                    texCoords[sq * 12 + 9] = 0.0f;
                    texCoords[sq * 12 + 10] = 1.0f;
                    texCoords[sq * 12 + 11] = 1.0f;

                    texCoordsMap[sq * 12 + 8] = ((float) w / (float) worldWidthSegments);
                    texCoordsMap[sq * 12 + 9] = ((float) h / (float) worldHeightSegments);
                    texCoordsMap[sq * 12 + 10] = ((float) (w + 1) / (float) worldWidthSegments);
                    texCoordsMap[sq * 12 + 11] = ((float) (h + 1) / (float) worldHeightSegments);

                    normalA = Vector3D.GetNormal(square.P0, square.P1, square.P2);
                    normalB = Vector3D.GetNormal(square.P3, square.P0, square.P2);

                    normals[sq * 18 + 6] = normalA.x;
                    normals[sq * 18 + 7] = normalA.y;
                    normals[sq * 18 + 8] = normalA.z;
                    normals[sq * 18 + 9] = normalB.x;
                    normals[sq * 18 + 10] = normalB.y;
                    normals[sq * 18 + 11] = normalB.z;
                } else {
                    shiny[sq * 6 + 4] = square.P1.y > 0.0f ? 0.0f : 100.0f;
                    shiny[sq * 6 + 5] = square.P3.y > 0.0f ? 0.0f : 100.0f;

                    vertices[sq * 18 + 12] = square.P1.x;
                    vertices[sq * 18 + 13] = square.P1.y;
                    vertices[sq * 18 + 14] = square.P1.z;

                    vertices[sq * 18 + 15] = square.P3.x;
                    vertices[sq * 18 + 16] = square.P3.y;
                    vertices[sq * 18 + 17] = square.P3.z;

                    texCoords[sq * 12 + 8] = 0.0f;
                    texCoords[sq * 12 + 9] = 1.0f;
                    texCoords[sq * 12 + 10] = 1.0f;
                    texCoords[sq * 12 + 11] = 0.0f;

                    texCoordsMap[sq * 12 + 8] = ((float) w / (float) worldWidthSegments);
                    texCoordsMap[sq * 12 + 9] = ((float) (h + 1) / (float) worldHeightSegments);
                    texCoordsMap[sq * 12 + 10] = ((float) (w + 1) / (float) worldWidthSegments);
                    texCoordsMap[sq * 12 + 11] = ((float) h / (float) worldHeightSegments);

                    normalA = Vector3D.GetNormal(square.P0, square.P1, square.P3);
                    normalB = Vector3D.GetNormal(square.P2, square.P3, square.P1);

                    normals[sq * 18 + 6] = normalB.x;
                    normals[sq * 18 + 7] = normalB.y;
                    normals[sq * 18 + 8] = normalB.z;
                    normals[sq * 18 + 9] = normalA.x;
                    normals[sq * 18 + 10] = normalA.y;
                    normals[sq * 18 + 11] = normalA.z;
                }

                normals[sq * 18] = normalA.x;
                normals[sq * 18 + 1] = normalA.y;
                normals[sq * 18 + 2] = normalA.z;
                normals[sq * 18 + 3] = normalA.x;
                normals[sq * 18 + 4] = normalA.y;
                normals[sq * 18 + 5] = normalA.z;

                normals[sq * 18 + 12] = normalB.x;
                normals[sq * 18 + 13] = normalB.y;
                normals[sq * 18 + 14] = normalB.z;
                normals[sq * 18 + 15] = normalB.x;
                normals[sq * 18 + 16] = normalB.y;
                normals[sq * 18 + 17] = normalB.z;

                indicesWire[sq * 8] = (short) (index);
                indicesWire[sq * 8 + 1] = (short) (index + 1);
                indicesWire[sq * 8 + 2] = (short) (index + 1);
                indicesWire[sq * 8 + 3] = (short) (index + 2);
                indicesWire[sq * 8 + 4] = (short) (index + 2);
                indicesWire[sq * 8 + 5] = (short) (index + 3);
                indicesWire[sq * 8 + 6] = (short) (index + 3);
                indicesWire[sq * 8 + 7] = (short) (index);

                index += 6;
            }
            
            numberOfIndicesByChunk.put(chunk, indicesWire.length);
            verticesByChunk.get(chunk).put(vertices);
            verticesByChunk.get(chunk).position(0);
        
            shinyByChunk.get(chunk).put(shiny);
            shinyByChunk.get(chunk).position(0);
            
            colorsByChunk.get(chunk).put(colors);
            colorsByChunk.get(chunk).position(0);

            normalsByChunk.get(chunk).put(normals);
            normalsByChunk.get(chunk).position(0);
            
            texCoordsByChunk.get(chunk).put(texCoords);
            texCoordsByChunk.get(chunk).position(0);

            texCoordMapByChunk.get(chunk).put(texCoordsMap);
            texCoordMapByChunk.get(chunk).position(0);
            
            indicesWireByChunk.get(chunk).put(indicesWire);
            indicesWireByChunk.get(chunk).position(0);
        }
    }

    public void GenerateMaterials() {
        TextureHandle_RareEarth = TextureHandler.GenerateMaterialTexture(planetDescriptor.getResource_RareEarth(), new Color4f(1.0f, 0.5f, 0.0f));
        if (TextureHandle_RareEarth == -1)
            Log.d("Failed generating", "RareEarth");

        TextureHandle_CoreIce = TextureHandler.GenerateMaterialTexture(planetDescriptor.getResource_CoreIce(), new Color4f(0.0f, 0.0f, 1.0f));
        if (TextureHandle_CoreIce == -1)
            Log.d("Failed generating", "CoreIce");

        RebuildTextureMap();
    }

    public void ChangeLandscape(boolean raise) {
        Log.d("RaiseLandscape", FirstPickResultW + "/" + FirstPickResultH);
        int width = Math.min(Math.abs(LastPickResultW - FirstPickResultW), 10);
        int height = Math.min(Math.abs(LastPickResultH - FirstPickResultH), 10);
        int w = (LastPickResultW >= FirstPickResultW) ? FirstPickResultW : FirstPickResultW - width;
        int h = (LastPickResultH >= FirstPickResultH) ? FirstPickResultH : FirstPickResultH - height;
        int a, b;

        int minH = 1000;
        int maxH = 0;
        for (a = 0; a <= height; a++) {
            for (b = 0; b <= width; b++) {
                minH = Math.min(HeightMap[h + a][w + b], minH);
                maxH = Math.max(HeightMap[h + a][w + b], maxH);
            }
        }

        Log.d("Min/Max Level:", minH + "/" + maxH);
        if ((raise && minH >= TerrainGen.OverSeaLevels) || (!raise && maxH <= 0)) {
            Log.d("ChangeLandscape", "canceling, already at max/min");
            return;
        }


        for (a = 0; a <= height; a++) {
            for (b = 0; b <= width; b++) {
                if (raise) {
                    if (HeightMap[h + a][w + b] == minH) {
                        HeightMap[h + a][w + b]++;
                    }
                } else {
                    if (HeightMap[h + a][w + b] == maxH) {
                        HeightMap[h + a][w + b]--;
                    }
                }
            }
        }

        // we changed the map, this might have consequences ...
        TerrainGen.RecheckTerrainAt(HeightMap, w, h, width, height);
        BuildSurfaceFromHeightMap();
        RebuildTextureMap();
    }

    public void setPickType(int type) {
        pickType = type;
    }


    public void SetResourceMapStyle(MyConstants.ResourceMapStyle style) {
        CurrentResourceMapStyle = style;
    }

    private void RebuildTextureMap() {
        Log.d("Rebuild", "TextureMap");

        
        ShortArrayList current;
        Tile square;
        int index;
        short vIndex;
        Indices idx;

        final RoadMap roadMap = planetDescriptor.getRoadMap();
        Log.d("Rebuilding", Arrays.toString(roadMap.getFlatRoadMap()));

        for (final Chunk chunk : chunks) {
            SparseArray<ShortArrayList> newVBOindicesByTextureId = new SparseArray<ShortArrayList>();
            for (int key : keys) {
                
                current = newVBOindicesByTextureId.get(key);
                if (current == null) {
                    current = new ShortArrayList();
                    newVBOindicesByTextureId.append(key, current);
                }
            
                for (int h = 0; h < chunk.getHeight(); h++) {
                    for (int w = 0; w < chunk.getWidth(); w++) {

                        int xi = chunk.getX() * CHUNK_SEGMENTS_WIDTH + w;
                        int yi = chunk.getY() * CHUNK_SEGMENTS_HEIGHT + h;
                        if (roadMap.GetTextureId(xi, yi) != key) {
                            continue;
                        }

                        index = h * chunk.getWidth() + w;
                        vIndex = (short) (index * MyConstants.NumVerticesPerQuad);

                        final List<Tile> squareList = squaresOfWorld.get(chunk);
                        square = squareList.get(index);

                        if (!square.mirrored) {
                            current.Add(vIndex);
                            current.Add(vIndex + 1);
                            current.Add(vIndex + 2);

                            current.Add(vIndex + 5);
                            current.Add(vIndex + 3);
                            current.Add(vIndex + 4);
                        } else {
                            current.Add(vIndex + 3);
                            current.Add(vIndex);
                            current.Add(vIndex + 1);

                            current.Add(vIndex + 4);
                            current.Add(vIndex + 2);
                            current.Add(vIndex + 5);
                        }
                    }
                }
                
                final SparseArray<Indices> vboIndicesByTextureId = vboIndicesByChunkByTextureId.get(chunk);
                // switch to new indexbuffer
                idx = vboIndicesByTextureId.get(key);
                if (idx == null) {
                    idx = new Indices();
                    vboIndicesByTextureId.append(key, idx);
                }
                
                if (key != 0 && idx.textureHandle1 <= 0) {
                    idx.textureHandle1 = TextureHandler.GetTextureHandle(key);
                }
                
                short[] elems = current.ToArray();
                ShortBuffer tmpS = ByteBuffer.allocateDirect(elems.length * MyConstants.NumBytesPerShort).order(ByteOrder.nativeOrder()).asShortBuffer();
                tmpS.put(elems);
                tmpS.position(0);
                idx.indexBuffer = tmpS;
                idx.numberOfIndices = elems.length;
                
            }

        }
    }

    private void UpdateConnectionPoints() {
        final CopyOnWriteArrayList<BuildingDescriptor> buildingDescriptors = planetDescriptor.getBuildings();
        
        for(final Chunk chunk : chunks) {
            numberOfIndicesConnectionByChunk.put(chunk, 0);
        }

        for (final BuildingDescriptor bd : buildingDescriptors) {
            for (final Position2D p : bd.ConnectionPoints) {
                AddToConnectionPoints(p.X, p.Y);
            }
        }
    }

    public Direction getBuildDirection() {
        return BuildDirection;
    }

    public Vector3D getBuildPosition() {
        return SelectedGridStartPoint;
    }

    public Position2D getPickingPoint() {
        return new Position2D(FirstPickResultW, FirstPickResultH);
    }

    public boolean AddBuilding(final BuildingDescriptor buildingDescription) {
        final String buildingKey = buildingDescription.getBuildingType();
        Building building;

        Log.d("PlanetSurface", "AddBuilding" + buildingKey + " > " + buildingDescription.getBuildingDirection());
        if (buildingKey.equalsIgnoreCase("BASE_CENTER")) {
            building = new Base_Center(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("STATION")) {
            building = new Building_Transport_Station(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("RE_MINE")) {
            building = new Building_Mine_RareEarth(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("MINE_CORE_ICE")) {
            building = new Building_Mine_CoreIce(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("HE3_MINE")) {
            building = new Building_Mine_He3(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("SOLAR_PANELS")) {
            building = new Building_Solar_Panels(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("FUSION_PLANT")) {
            building = new Building_Fusion_Plant(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("MELTER")) {
            building = new Building_Melter(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("PROD_OXYGEN")) {
            building = new Building_Prod_Oxygen(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("SCIENCE_CENTER")) {
            building = new Building_Science(context, segmentSize, buildingKey);
        } else if (buildingKey.equalsIgnoreCase("CENTER")) {
            building = new Building_Center(context, segmentSize, buildingKey);
        } else {
            return false;
        }

        building.setBuildingDescriptor(buildingDescription);
        building.SetProduction();

        building.SetDirection(buildingDescription.getBuildingDirection(), buildingDescription.getBuildingGrid());
        final Vector3D buildingPosition = buildingDescription.getBuildingPosition();
        Log.d("AddBuidling", buildingPosition.x + "//" + buildingPosition.z);
        Log.d("AddBuidlingPick", buildingDescription.getPickingPoint().X + "//" + buildingDescription.getPickingPoint().Y);
        building.SetPosition(buildingPosition.x, buildingPosition.y, buildingPosition.z);
        buildingDescription.UpdateConnectionPoints();


        Buildings.add(building);
        //   planetDescriptor.addBuilding(buildingDescription);
        UpdateConnectionPoints();

        return true;
    }

    public void RemoveVehicle(Vehicle vh) {
        Vehicles.remove(vh);
    }

    public Vehicle addVehicle(final String vehicleDefKey, int w, int h) {
        Vehicle vehicle;

        Log.d("PlanetSurface", "AddVehicle " + vehicleDefKey);
        if (vehicleDefKey.equalsIgnoreCase(VehicleTransport.KEY)) {
            vehicle = new VehicleTransport(context, segmentSize);
        } else {
            return null;
        }
        
        
        final Chunk chunk = getChunkOfWorldCoordinates(w, h);
        final int xi = w % CHUNK_SEGMENTS_WIDTH;
        final int yi = h % CHUNK_SEGMENTS_HEIGHT;
        final FloatBuffer vertexBuffer = verticesByChunk.get(chunk);
        final int vertexIndex = (yi * chunk.getWidth() + xi) * MyConstants.NumVerticesPerQuad * MyConstants.NumFloatPerVertex;
        float x = vertexBuffer.get(vertexIndex);
        float y = vertexBuffer.get(vertexIndex + 1);
        float z = vertexBuffer.get(vertexIndex + 2);
        vehicle.setPosition(x, y, z);
        Vehicles.add(vehicle);
        return vehicle;
    }
    
    private Chunk getChunkOfWorldCoordinates(final int w, final int h) {
        final int x = w / CHUNK_SEGMENTS_WIDTH;
        final int y = h / CHUNK_SEGMENTS_HEIGHT;
        
        for (final Chunk chunk : chunks) {
            if (chunk.getX() == x && chunk.getY() == y) {
                return chunk;
            }
        }
        return null;
    }

    private void AddToRoadMap(int w, int h, byte dir) {
        
        if (planetDescriptor.getRoadMap().Add(w, h, dir)) {
            // remove
        }
    }

    public void AddTransport() {
        Log.d("AddTransport", FirstPickResultW + "/" + FirstPickResultH + "   " + LastPickResultW + "/" + LastPickResultH);
        int widthDelta = LastPickResultW - FirstPickResultW;
        int heightDelta = LastPickResultH - FirstPickResultH;
        int dw, dh, totalChange;
        int w = FirstPickResultW;
        int h = FirstPickResultH;

        if (Math.abs(widthDelta) >= Math.abs(heightDelta)) {
            if (widthDelta == 0)
                return;
            // go the width direction first
            totalChange = Math.abs(widthDelta);
            dw = widthDelta / totalChange;

            // start point
            byte dir = RoadMap.E;
            if (dw < 0) dir = RoadMap.W;
            AddToRoadMap(w, h, dir);
            dir = (byte) (RoadMap.E | RoadMap.W);
            w += dw;

            // middle points
            for (int i = 1; i < (totalChange - 1); i++) {
                AddToRoadMap(w, h, dir);
                w += dw;
            }
            if (dw < 0) dir = RoadMap.E;
            else dir = RoadMap.W;

            totalChange = Math.abs(heightDelta);
            if (totalChange <= 0) {
                AddToRoadMap(w, h, dir);
            } else {
                dh = heightDelta / totalChange;
                // curve
                if (dh < 0) dir |= RoadMap.N;
                else dir |= RoadMap.S;
                AddToRoadMap(w, h, dir);

                dir = (byte) (RoadMap.N | RoadMap.S);
                h += dh;
                // middle
                for (int i = 1; i < totalChange; i++) {
                    AddToRoadMap(w, h, dir);
                    h += dh;
                }
                // last
                if (dh < 0) dir = RoadMap.S;
                else dir = RoadMap.N;
                AddToRoadMap(w, h, dir);
            }

        } else {

            // go the height direction first
            totalChange = Math.abs(heightDelta);
            dh = heightDelta / totalChange;

            // startpoint
            byte dir = RoadMap.S;
            if (dh < 0) dir = RoadMap.N;
            AddToRoadMap(w, h, dir);
            dir = (byte) (RoadMap.S | RoadMap.N);
            h += dh;

            // middle points
            for (int i = 1; i < (totalChange - 1); i++) {
                AddToRoadMap(w, h, dir);
                h += dh;
            }

            if (dh < 0) dir = RoadMap.S;
            else dir = RoadMap.N;
            totalChange = Math.abs(widthDelta);

            if (totalChange <= 0) {
                AddToRoadMap(w, h, dir);
            } else {
                dw = widthDelta / totalChange;
                // curve
                if (dw < 0) dir |= RoadMap.W;
                else dir |= RoadMap.E;
                AddToRoadMap(w, h, dir);

                dir = (byte) (RoadMap.E | RoadMap.W);
                w += dw;
                // middle
                for (int i = 1; i < totalChange; i++) {
                    AddToRoadMap(w, h, dir);
                    w += dw;
                }
                // last
                if (dw < 0) dir = RoadMap.E;
                else dir = RoadMap.W;
                AddToRoadMap(w, h, dir);
            }
        }
        RebuildTextureMap();
        UpdatePathfinder();
    }

    private void UpdatePathfinder() {
        CopyOnWriteArrayList<BuildingDescriptor> buildingDescriptors = planetDescriptor.getBuildings();
        for (final BuildingDescriptor bd : buildingDescriptors) {
            Log.d("UpdatePathfinder", "bd.NeedsTransportation" + bd.NeedsTransportation);
            if (bd.NeedsTransportation) {
                List<PathNode> BestPath = null;

                for (Position2D connPoint : bd.ConnectionPoints) {
                    List<PathNode> currentBestPath = FindPathToTransportCenter(connPoint);
                    if (currentBestPath != null) {
                        if (BestPath == null || BestPath.size() > currentBestPath.size()) {
                            BestPath = currentBestPath;
                        }
                    }
                }

                Log.d("UpdatePathfinder", "BestPath" + BestPath);
                if (BestPath != null)
                    Log.d("UpdatePathfinder", "BestPathSize" + BestPath.size());

                // now we have the best connectionpath for ALL connectionpoints, if there's any
                bd.SetPathToTransportCenter(BestPath);
            }
        }
    }

    private List<PathNode> FindPathToTransportCenter(Position2D startPoint) {
        final Vector<Position2D> targetPoints = new Vector<Position2D>();
        final CopyOnWriteArrayList<BuildingDescriptor> buildingDescriptors = planetDescriptor.getBuildings();
        for (final BuildingDescriptor bd : buildingDescriptors) {
            if (GameActivity.MyGameLogic.GameRules.isTransportCenter(bd.getBuildingType())) {
                targetPoints.addAll(bd.ConnectionPoints);
            }
        }
        // now we have all available targetpoints
        Log.d("Find path from ", startPoint.X + "/" + startPoint.Y);

        PathNode start = new PathNode(startPoint.X, startPoint.Y);
        List<PathNode> BestPath = null;
        for (Position2D pos : targetPoints) {
            Log.d("to", pos.X + "/" + pos.Y);
            PathNode goal = new PathNode(pos.X, pos.Y);

            final List<PathNode> path = planetDescriptor.getRoadMap().FindPathAStar(start, goal);
            Log.d("Found path?", "" + (path.size() > 0));
            if (path.size() > 0) {
                if (BestPath == null || BestPath.size() > path.size()) {
                    BestPath = path;
                }
            }
        }

        int i, EW, NS;

        if (BestPath != null)
            for (PathNode pn : BestPath) {
                final Chunk chunk = getChunkOfWorldCoordinates(pn.x, pn.y);
                final int xi = pn.x % CHUNK_SEGMENTS_WIDTH;
                final int yi = pn.y % CHUNK_SEGMENTS_HEIGHT;
                final List<Tile> squares = squaresOfWorld.get(chunk);
                
                i = yi * chunk.getWidth() + xi;
                EW = 0;
                NS = 0;
                switch (squares.get(i).TileType) {
                    case Tile.TILE_TYPE_RAMP_N:
                        NS = 1;
                        break;
                    case Tile.TILE_TYPE_RAMP_S:
                        NS = -1;
                        break;
                    case Tile.TILE_TYPE_RAMP_E:
                        EW = 1;
                        break;
                    case Tile.TILE_TYPE_RAMP_W:
                        EW = -1;
                        break;
                }
                pn.EW = EW;
                pn.NS = NS;
            }
        return BestPath;
    }

    public void Animate(int deltaMs) {
        synchronized (Buildings) {
            for (final Building bd : Buildings) {
                bd.Animate(deltaMs);
            }
        }
    }

    private boolean HasHit(Ray ray, int w, int h) {
        if ((w < 0 || w >= worldWidthSegments) || (h < 0 || h >= worldHeightSegments)) {
            return false;
        }
        
        final Triangle triangle = new Triangle();
        final Chunk chunk = getChunkOfWorldCoordinates(w, h);
        final int xi = w % CHUNK_SEGMENTS_WIDTH;
        final int yi = h % CHUNK_SEGMENTS_HEIGHT;
        final int index = ((yi * chunk.getWidth() + xi) * MyConstants.NumVerticesPerQuad) * MyConstants.NumFloatPerVertex;
        final FloatBuffer vertexBuffer = verticesByChunk.get(chunk);
        
        try {
            triangle.P0.x = vertexBuffer.get(index + 0);
            triangle.P0.y = vertexBuffer.get(index + 1);
            triangle.P0.z = vertexBuffer.get(index + 2);
            triangle.P1.x = vertexBuffer.get(index + 3);
            triangle.P1.y = vertexBuffer.get(index + 4);
            triangle.P1.z = vertexBuffer.get(index + 5);
            triangle.P2.x = vertexBuffer.get(index + 6);
            triangle.P2.y = vertexBuffer.get(index + 7);
            triangle.P2.z = vertexBuffer.get(index + 8);

            if (Helpers.RayTriangleIntersect(ray, triangle)) {
                return true;
            }

            triangle.P1.x = triangle.P2.x;
            triangle.P1.y = triangle.P2.y;
            triangle.P1.z = triangle.P2.z;

            triangle.P2.x = vertexBuffer.get(index + 9);
            triangle.P2.y = vertexBuffer.get(index + 10);
            triangle.P2.z = vertexBuffer.get(index + 11);

            if (Helpers.RayTriangleIntersect(ray, triangle)) {
                return true;
            }
        } catch (Exception e) {
            Log.e("Boom", h + "/" + w + "/" + index, e);
        }
        return false;
    }


    private void UpdateSelectionByDirection(boolean init) {
        int x, y;
        int h, w;
        int tmp;
        boolean positive;

        Deselect();
        int[][] grid = GameActivity.MyGameLogic.SelectionGrid;
        h = (grid != null) ? grid.length : 0;
        w = (h > 0) ? grid[0].length : 0;

        // no grid? nothing to do
        if (h == 0 || w == 0)
            return;

        // turn grid by direction
        switch (BuildDirection) {
            case WEST:
                grid = Helpers.MatrixRotate90CW(GameActivity.MyGameLogic.SelectionGrid);
                tmp = h;
                h = w;
                w = tmp;
                break;

            case NORTH:
                grid = Helpers.MatrixRotate180(GameActivity.MyGameLogic.SelectionGrid);
                Log.d("dragging", "north");
                break;

            case EAST:
                grid = Helpers.MatrixRotate180(GameActivity.MyGameLogic.SelectionGrid);
                grid = Helpers.MatrixRotate90CW(grid);
                tmp = h;
                h = w;
                w = tmp;
                break;

            default:
            case SOUTH:
                // no change
                break;
        }

        for (int hh = 0; hh < h; hh++) {
            for (int ww = 0; ww < w; ww++) {
                y = FirstPickResultH - (h / 2) + hh;
                x = FirstPickResultW - (w / 2) + ww;

                if (hh == 0 && ww == 0) {
                    final Chunk chunk = getChunkOfWorldCoordinates(x, y);
                    final FloatBuffer vertexBuffer = verticesByChunk.get(chunk);
                    final int xi = x % CHUNK_SEGMENTS_WIDTH;
                    final int yi = y % CHUNK_SEGMENTS_HEIGHT;
                    final int vertexIndex = ((yi * chunk.getWidth() + xi) * MyConstants.NumVerticesPerQuad) * MyConstants.NumFloatPerVertex;
                    
                    SelectedGridStartPoint.x = vertexBuffer.get(vertexIndex);
                    SelectedGridStartPoint.y = vertexBuffer.get(vertexIndex + 1);
                    SelectedGridStartPoint.z = vertexBuffer.get(vertexIndex + 2);
                }

                if (grid[hh][ww] == 0)
                    continue;

                // flat?
                positive = HeightMap[y][x] > 0;
                positive &= (HeightMap[y][x] == HeightMap[y + 1][x]);
                positive &= (HeightMap[y + 1][x] == HeightMap[y][x + 1]);
                positive &= (HeightMap[y][x + 1] == HeightMap[y + 1][x + 1]);

                if (positive) {
                    AddToPositiveSelection(x, y);
                } else {
                    AddToNegativeSelection(x, y);
                }
            }
        }

    }


    public void Pick(Ray ray) {
        boolean found = false;

        if (pickType == 1) {
            for (final Building bd : Buildings) {
                if (Helpers.HasBoundingboxHit(ray, bd.BoundingBox)) {
                    GameActivity.MyGameLogic.BuildingClicked(bd);
                    return;
                }
            }
        }


        for (int h = 0; h < worldHeightSegments && !found; h++) {
            for (int w = 0; w < worldWidthSegments; w++) {
                if (HasHit(ray, w, h)) {
                    found = true;
                    FirstPickResultW = w;
                    FirstPickResultH = h;
                    LastPickResultW = FirstPickResultW;
                    LastPickResultH = FirstPickResultH;
                    break;
                }
            }
        }

        if (found) {
            UpdateSelectionByDirection(true);
            SetResourcesAtLocation();
            Log.d("PlanetSurface", "Pick" + getNegativeSelectionCount());
            GameActivity.MyGameLogic.SetReadyToBuild(getNegativeSelectionCount() == 0);
        }
    }

    public void PickUpdate(Ray ray) {
        boolean found = false;
        int w = 0, h;

        for (h = -PickUpdateRange; h < PickUpdateRange && !found; h++) {
            for (w = -PickUpdateRange; w < PickUpdateRange; w++) {
                if ((LastPickResultW + w > 0 && LastPickResultW + w <= worldWidthSegments) && (LastPickResultH + h > 0 && LastPickResultH + h <= worldHeightSegments))
                    if (HasHit(ray, LastPickResultW + w, LastPickResultH + h)) {
                        found = true;
                        LastPickResultW += w;
                        LastPickResultH += h;

                        // still at the starting point? no need to update
                        if (LastPickResultH == FirstPickResultH && LastPickResultW == FirstPickResultW)
                            return;

//          Log.d(FirstPickResultW+"first"+FirstPickResultH,LastPickResultW+"last"+LastPickResultH);
                        break;
                    }
            }
        }

        if (found) {
            int widthDelta = LastPickResultW - FirstPickResultW;
            int heightDelta = LastPickResultH - FirstPickResultH;
            int dw, dh, totalChange;

            if (widthDelta != 0 && Math.abs(widthDelta) >= Math.abs(heightDelta)) {
                // go the width direction
                totalChange = Math.abs(widthDelta);
                dw = widthDelta / totalChange;
                dh = 0;
                BuildDirection = dw >= 0 ? Direction.EAST : Direction.WEST;
            } else if (heightDelta != 0) {
                // go the height direction
                totalChange = Math.abs(heightDelta);
                dw = 0;
                dh = heightDelta / totalChange;
                BuildDirection = dh >= 0 ? Direction.SOUTH : Direction.NORTH;
            } else {
                Log.e("PickUpdate", "deltas zero");
                BuildDirection = Direction.SOUTH;
            }


            if (GameActivity.MyGameLogic.MouseMoveAction == GameLogic.MOUSE_DRAG) {
                Deselect();
                w = FirstPickResultW;
                h = FirstPickResultH;

                boolean positive;
                if (Math.abs(widthDelta) >= Math.abs(heightDelta)) {
                    totalChange = Math.abs(widthDelta);
                    dw = widthDelta / totalChange;
                    for (int a = 0; a < totalChange; a++) {
                        positive = HeightMap[h][w] > 0;
                        positive &= (HeightMap[h][w] == HeightMap[h + 1][w]);
                        positive &= (HeightMap[h][w + 1] == HeightMap[h + 1][w + 1]);

                        if (positive) {
                            AddToPositiveSelection(w, h);
                        } else {
                            AddToNegativeSelection(w, h);
                        }
                        if (a + 1 < totalChange) w += dw;
                    }

                    totalChange = Math.abs(heightDelta);
                    Log.d("heightdelta", "" + totalChange);
                    if (totalChange > 0) {
                        dh = heightDelta / totalChange;
                        h += dh;
                        for (int a = 1; a <= totalChange; a++) {
                            positive = (HeightMap[h][w] == HeightMap[h][w + 1]);
                            positive &= (HeightMap[h + 1][w] == HeightMap[h + 1][w + 1]);

                            if (positive) {
                                AddToPositiveSelection(w, h);
                            } else {
                                AddToNegativeSelection(w, h);
                            }
                            h += dh;
                        }
                    }
                } else {
                    totalChange = Math.abs(heightDelta);
                    dh = heightDelta / totalChange;
                    for (int a = 0; a < totalChange; a++) {
                        positive = (HeightMap[h][w] == HeightMap[h][w + 1]);
                        positive &= (HeightMap[h + 1][w] == HeightMap[h + 1][w + 1]);

                        if (positive) {
                            AddToPositiveSelection(w, h);
                        } else {
                            AddToNegativeSelection(w, h);
                        }
                        if (a + 1 < totalChange) h += dh;
                    }

                    totalChange = Math.abs(widthDelta);
                    if (totalChange > 0) {
                        dw = widthDelta / totalChange;
                        w += dw;
                        for (int a = 1; a <= totalChange; a++) {

                            positive = HeightMap[h][w] > 0;
                            positive &= (HeightMap[h][w] == HeightMap[h + 1][w]);
                            positive &= (HeightMap[h][w + 1] == HeightMap[h + 1][w + 1]);

                            if (positive) {
                                AddToPositiveSelection(w, h);
                            } else {
                                AddToNegativeSelection(w, h);
                            }
                            w += dw;
                        }
                    }
                }

                SetResourcesAtLocation();
                GameActivity.MyGameLogic.SetReadyToBuild(getNegativeSelectionCount() == 0);
            } else if (GameActivity.MyGameLogic.MouseMoveAction == GameLogic.MOUSE_DRAG_INTO_DIRECTION) {
                UpdateSelectionByDirection(false);

                SetResourcesAtLocation();
                GameActivity.MyGameLogic.SetReadyToBuild(getNegativeSelectionCount() == 0);
            } else if (GameActivity.MyGameLogic.MouseMoveAction == GameLogic.MOUSE_DRAG_FOR_PLANE) {
                Deselect();

                int lpw = LastPickResultW;
                if (Math.abs(widthDelta) >= 10)
                    lpw = FirstPickResultW + (widthDelta < 0 ? -10 : 10);
                int lph = LastPickResultH;
                if (Math.abs(heightDelta) >= 10)
                    lph = FirstPickResultH + (heightDelta < 0 ? -10 : 10);

                w = (widthDelta < 0) ? lpw : FirstPickResultW;
                for (int a = 0; a < Math.abs(widthDelta) && a < 10; a++, w++) {
                    h = (heightDelta < 0) ? lph : FirstPickResultH;
                    for (int b = 0; b < Math.abs(heightDelta) && b < 10; b++, h++) {
                        AddToPositiveSelection(w, h);
                    }
                }


            }
        }
    }
    
    private int getNegativeSelectionCount() {
        int sum = 0;
        for(final Integer i : numberOfIndicesNegativeByChunk.values()){
            sum += i.intValue();
        }
        return sum;
    }

    private float GetMaterialAtLocation(String materialKey) {
        switch (materialKey) {
            case "MATERIAL_RARE_EARTH":
                return ((float) (planetDescriptor.getResource_RareEarth()[FirstPickResultH][FirstPickResultW] & 0xFF) / 255.0f);
            case "MATERIAL_CORE_ICE":
                return ((float) (planetDescriptor.getResource_CoreIce()[FirstPickResultH][FirstPickResultW] & 0xFF) / 255.0f);
        }
        return 0.0f;
    }

    private void SetResourcesAtLocation() {
        GameActivity.MyGameLogic.SetMaterialAtLocation("MATERIAL_RARE_EARTH", getNegativeSelectionCount() == 0 ? GetMaterialAtLocation("MATERIAL_RARE_EARTH") : 0.0f);
        GameActivity.MyGameLogic.SetMaterialAtLocation("MATERIAL_CORE_ICE", getNegativeSelectionCount() == 0 ? GetMaterialAtLocation("MATERIAL_CORE_ICE") : 0.0f);
    }

    public void Deselect() {
        for(final Chunk chunk : chunks) {
            numberOfIndicesPositiveByChunk.put(chunk, 0);
            numberOfIndicesNegativeByChunk.put(chunk, 0);
        }
    }

    private void AddToPositiveSelection(int w, int h) {

        final int xi = w % CHUNK_SEGMENTS_WIDTH;
        final int yi = h % CHUNK_SEGMENTS_HEIGHT;
        final Chunk chunk = getChunkOfWorldCoordinates(w, h);
        
        final ShortBuffer indexBufferPositiveSelection = indicesPositiveSelectionByChunk.get(chunk);
        int numberPositiveSelectedIndices = numberOfIndicesPositiveByChunk.get(chunk);
        
        int index = (yi * chunk.getWidth() + xi) * MyConstants.NumVerticesPerQuad;
        indexBufferPositiveSelection.put(numberPositiveSelectedIndices++, (short) (index));
        indexBufferPositiveSelection.put(numberPositiveSelectedIndices++, (short) (index + 1));

        indexBufferPositiveSelection.put(numberPositiveSelectedIndices++, (short) (index + 1));
        indexBufferPositiveSelection.put(numberPositiveSelectedIndices++, (short) (index + 2));
        
        indexBufferPositiveSelection.put(numberPositiveSelectedIndices++, (short) (index + 2));
        indexBufferPositiveSelection.put(numberPositiveSelectedIndices++, (short) (index + 3));
        
        indexBufferPositiveSelection.put(numberPositiveSelectedIndices++, (short) (index + 3));
        indexBufferPositiveSelection.put(numberPositiveSelectedIndices++, (short) (index));
        
        numberOfIndicesPositiveByChunk.put(chunk, numberPositiveSelectedIndices);
    }

    private void AddToNegativeSelection(int w, int h) {
        final int xi = w % CHUNK_SEGMENTS_WIDTH;
        final int yi = h % CHUNK_SEGMENTS_HEIGHT;
        final Chunk chunk = getChunkOfWorldCoordinates(w, h);
        final ShortBuffer indexBufferNegativeSelection = indicesNegativeSelectionByChunk.get(chunk);
        int numberNegativeSelectedIndices = numberOfIndicesNegativeByChunk.get(chunk);
        
        int index = (yi * chunk.getWidth() + xi) * MyConstants.NumVerticesPerQuad;
        indexBufferNegativeSelection.put(numberNegativeSelectedIndices++, (short) (index));
        indexBufferNegativeSelection.put(numberNegativeSelectedIndices++, (short) (index + 1));

        indexBufferNegativeSelection.put(numberNegativeSelectedIndices++, (short) (index + 1));
        indexBufferNegativeSelection.put(numberNegativeSelectedIndices++, (short) (index + 2));

        indexBufferNegativeSelection.put(numberNegativeSelectedIndices++, (short) (index + 2));
        indexBufferNegativeSelection.put(numberNegativeSelectedIndices++, (short) (index + 3));

        indexBufferNegativeSelection.put(numberNegativeSelectedIndices++, (short) (index + 3));
        indexBufferNegativeSelection.put(numberNegativeSelectedIndices++, (short) (index));
   
        numberOfIndicesNegativeByChunk.put(chunk, numberNegativeSelectedIndices);
    }

    private void AddToConnectionPoints(int w, int h) {
        final int xi = w % CHUNK_SEGMENTS_WIDTH;
        final int yi = h % CHUNK_SEGMENTS_HEIGHT;
        final Chunk chunk = getChunkOfWorldCoordinates(w, h);
        final ShortBuffer indexBufferConnectionPoints = indicesConnectionPointsByChunk.get(chunk);
        int numberConnectionPointsIndices = numberOfIndicesConnectionByChunk.get(chunk);
        int index = (yi * chunk.getWidth() + xi) * MyConstants.NumVerticesPerQuad;
        
        indexBufferConnectionPoints.put(numberConnectionPointsIndices++, (short) (index));
        indexBufferConnectionPoints.put(numberConnectionPointsIndices++, (short) (index + 1));

        indexBufferConnectionPoints.put(numberConnectionPointsIndices++, (short) (index + 1));
        indexBufferConnectionPoints.put(numberConnectionPointsIndices++, (short) (index + 2));

        indexBufferConnectionPoints.put(numberConnectionPointsIndices++, (short) (index + 2));
        indexBufferConnectionPoints.put(numberConnectionPointsIndices++, (short) (index + 3));

        indexBufferConnectionPoints.put(numberConnectionPointsIndices++, (short) (index + 3));
        indexBufferConnectionPoints.put(numberConnectionPointsIndices++, (short) (index));
        
        numberOfIndicesConnectionByChunk.put(chunk, numberConnectionPointsIndices);
    }


    //  public void Draw(boolean positionOnly, boolean nightTime)
    public void DrawPositionOnly(int positionHandle) {
        try {
            for (final Building bd : Buildings) {
                bd.DrawPositionOnly(positionHandle);
            }
        } catch (ConcurrentModificationException e) {
        }
    }

    public void Draw(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glUniform1i(textureHandle, 1);    // use GL_TEXTURE1
        
        
        for (final Chunk chunk : chunks) {
            final FloatBuffer vertexBuffer = verticesByChunk.get(chunk);
            final FloatBuffer colorBuffer = colorsByChunk.get(chunk);
            final FloatBuffer normalBuffer = normalsByChunk.get(chunk);
            final FloatBuffer shinyBuffer = shinyByChunk.get(chunk);
            final FloatBuffer texCoordBuffer = texCoordsByChunk.get(chunk);
            final FloatBuffer texCoordBufferMap = texCoordMapByChunk.get(chunk);
            final SparseArray<Indices> vboIndicesByTextureId = vboIndicesByChunkByTextureId.get(chunk);
            
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
            GLES20.glEnableVertexAttribArray(positionHandle);

            GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
            GLES20.glEnableVertexAttribArray(colorHandle);

            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);
            GLES20.glEnableVertexAttribArray(normalHandle);

            GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mShinyHandleDaytime, 1, GLES20.GL_FLOAT, false, 0, shinyBuffer);
            GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mShinyHandleDaytime);

            if (CurrentResourceMapStyle != MyConstants.ResourceMapStyle.NORMAL) {
                GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBufferMap);
            } else {
                GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
            }
            GLES20.glEnableVertexAttribArray(texCoordHandle);

            Indices idx;
            for (int key : keys) {
                idx = vboIndicesByTextureId.get(key);
                switch (CurrentResourceMapStyle) {
                    case RARE_EARTH:
                        GLES20.glUniform1i(texIDHandle, TextureHandle_RareEarth);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureHandle_RareEarth);
                        break;

                    case CORE_ICE:
                        GLES20.glUniform1i(texIDHandle, TextureHandle_CoreIce);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureHandle_CoreIce);
                        break;

                    default:
                    case NORMAL:
                        GLES20.glUniform1i(texIDHandle, key);        // tell the shader, we want to use texture
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, idx.textureHandle1);                // set the handle of the texture from LoadTexture
                        break;
                }
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, idx.numberOfIndices, GLES20.GL_UNSIGNED_SHORT, idx.indexBuffer);
            }
        }

        GLES20.glUniform1i(texIDHandle, 0);        // tell the shader, we're done using the texture and return to color mode

        GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mShinyHandleDaytime);
        GLES20.glDisableVertexAttribArray(colorHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);

        try {

            for (final Building bd : Buildings) {
                bd.Draw(positionHandle, colorHandle, normalHandle, texIDHandle, textureHandle, texCoordHandle);
            }

            for (Vehicle vh : Vehicles) {
                vh.Draw(positionHandle, colorHandle, normalHandle, texIDHandle, textureHandle, texCoordHandle);
            }

        } catch (ConcurrentModificationException e) {
            // in case we modified the list while drawing, the exception will be raised and we skip drawing until the next frame
        }
    }

    public void DrawWire() {

        // selected grid
        {
            ;
            ;
            GLES20.glLineWidth(3.0f);
            
            for(final Chunk chunk : chunks) {
                final ShortBuffer indexBufferConnectionPoints = indicesConnectionPointsByChunk.get(chunk);
                final ShortBuffer indexBufferPositiveSelection = indicesPositiveSelectionByChunk.get(chunk);
                final ShortBuffer indexBufferNegativeSelection = indicesNegativeSelectionByChunk.get(chunk);
                final FloatBuffer vertexBuffer = verticesByChunk.get(chunk);
                
                GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mPositionWireHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
                GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mPositionWireHandle);
                
                if (showConnectionPoints) {
                    GLES20.glVertexAttrib4fv(PlanetSurfaceRenderer.mColorWireHandle, colorConnectionPoints, 0);
                    GLES20.glDrawElements(GLES20.GL_LINES, numberOfIndicesConnectionByChunk.get(chunk), GLES20.GL_UNSIGNED_SHORT, indexBufferConnectionPoints);
                }
                
                GLES20.glVertexAttrib4fv(PlanetSurfaceRenderer.mColorWireHandle, colorPositiveSelection, 0);
                GLES20.glDrawElements(GLES20.GL_LINES, numberOfIndicesPositiveByChunk.get(chunk), GLES20.GL_UNSIGNED_SHORT, indexBufferPositiveSelection);
                
                GLES20.glVertexAttrib4fv(PlanetSurfaceRenderer.mColorWireHandle, colorNegativeSelection, 0);
                GLES20.glDrawElements(GLES20.GL_LINES, numberOfIndicesNegativeByChunk.get(chunk), GLES20.GL_UNSIGNED_SHORT, indexBufferNegativeSelection);  
            }
            
            GLES20.glLineWidth(1.0f);
            GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mPositionWireHandle);
        }


//		GLES20.glVertexAttribPointer(SurfaceRenderer.mPositionWireHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBufferNorm);
//		GLES20.glEnableVertexAttribArray(SurfaceRenderer.mPositionWireHandle);
//
//		GLES20.glVertexAttrib4fv(SurfaceRenderer.mColorWireHandle, colorWire, 0);
//		GLES20.glDrawElements(GLES20.GL_LINES, numberOfIndicesNorm, GLES20.GL_UNSIGNED_SHORT, indexBufferWireNorm);
//


        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        if (CurrentResourceMapStyle == MyConstants.ResourceMapStyle.NORMAL) {
            GLES20.glVertexAttrib4fv(PlanetSurfaceRenderer.mColorWireHandle, colorWire, 0);
        } else {
            GLES20.glVertexAttrib4fv(PlanetSurfaceRenderer.mColorWireHandle, colorWireDark, 0);
        }
        
        for(final Chunk chunk : chunks) {
            final ShortBuffer indexBufferWire = indicesWireByChunk.get(chunk);
            final FloatBuffer vertexBuffer = verticesByChunk.get(chunk);
            final int numberOfIndicesWire = numberOfIndicesByChunk.get(chunk);
            
            GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mPositionWireHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
            GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mPositionWireHandle);

            GLES20.glDrawElements(GLES20.GL_LINES, numberOfIndicesWire, GLES20.GL_UNSIGNED_SHORT, indexBufferWire);
        }
        
        GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mPositionWireHandle);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public void DrawParticles() {
        GLES20.glUseProgram(PlanetSurfaceRenderer.programHandleParticle);
//    GLES20.glUniform1f(SurfaceRenderer.uPointSizeHandle, (10.0f - glCamera.getZoom()) * 50.0f);

        GLES20.glUniform3f(PlanetSurfaceRenderer.uCameraPosParticleHandle,
                planetSurfaceRenderer.cameraPlanetSurface.position.x,
                planetSurfaceRenderer.cameraPlanetSurface.position.y,
                planetSurfaceRenderer.cameraPlanetSurface.position.z);

        GLES20.glUniform1f(PlanetSurfaceRenderer.uPointSizeHandle, 50.0f);
        GLES20.glUniformMatrix4fv(PlanetSurfaceRenderer.mMVPMatrixParticleHandle, 1, false, PlanetSurfaceRenderer.mMVPMatrix, 0);

        for (final Building bd : Buildings) {
            bd.DrawEffects();
        }
    }


}
